/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataConnectException
import com.google.firebase.dataconnect.DataConnectOperationException
import com.google.firebase.dataconnect.DataConnectOperationFailureResponse
import com.google.firebase.dataconnect.DataConnectPathSegment
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/*
 * The Data Connect REST service API, as the Firebase JS SDK uses it:
 *
 *   POST {base}/v1/projects/{project}/locations/{location}/services/{service}/connectors/{connector}:executeQuery
 *   POST ...:executeMutation
 *   body { "name": "projects/.../connectors/...", "operationName": "...", "variables": { ... } }
 *   response { "data": { ... }, "errors": [ { "message": "...", "path": [ "field", 0 ], ... } ] }
 *
 * A failed request (a non-2xx status) carries a google.rpc.Status JSON body ({ "code", "message", "details" }).
 */

/** The result of an operation on the server: its data, and the errors it reported. */
internal class OperationResponse(val data: JsonElement?, val errors: List<ErrorInfoImpl>)

internal class ErrorInfoImpl(override val message: String, override val path: List<DataConnectPathSegment>) : DataConnectOperationFailureResponse.ErrorInfo {
    override fun equals(other: Any?): Boolean = other is ErrorInfoImpl && other.message == message && other.path == path

    override fun hashCode(): Int = listOf(message, path).hashCode()

    override fun toString(): String = "ErrorInfo(message=$message, path=${path.joinToString(".")})"
}

internal class FailureResponseImpl<Data>(
    override val rawData: Map<String, Any?>?,
    override val errors: List<DataConnectOperationFailureResponse.ErrorInfo>,
    override val data: Data?,
) : DataConnectOperationFailureResponse<Data> {
    override fun toString(): String = "DataConnectOperationFailureResponse(rawData=$rawData, errors=$errors, data=$data)"
}

private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal suspend fun FirebaseDataConnectImpl.executeOnServer(operationName: String, variables: JsonObject, mutation: Boolean): OperationResponse {
    val projectId = app.options.projectId?.takeIf { it.isNotBlank() }
        ?: throw DataConnectException("FirebaseApp ${app.name} has no projectId; Data Connect needs one")
    val name = "projects/$projectId/locations/${config.location}/services/${config.serviceId}/connectors/${config.connector}"
    val method = if (mutation) "executeMutation" else "executeQuery"
    val apiKey = app.options.apiKey.takeIf { it.isNotBlank() }
    val url = "$baseUrl/v1/$name:$method" + (apiKey?.let { "?key=$it" } ?: "")
    val headers = buildMap {
        put("Content-Type", "application/json")
        put("X-Goog-Api-Client", "gl-kotlin/${KotlinVersion.CURRENT} fire/$SDK_VERSION")
        app.options.applicationId.takeIf { it.isNotBlank() }?.let { put("x-firebase-gmpid", it) }
        app.idTokenOrNull()?.let { put("X-Firebase-Auth-Token", it) }
    }
    val body = buildJsonObject {
        put("name", name)
        put("operationName", operationName)
        put("variables", variables)
    }
    val response = httpPost(url, headers, body.toString())
    val element = runCatching { lenientJson.parseToJsonElement(response.body) }.getOrNull()
    if (response.status !in 200..299) {
        val message = (element as? JsonObject)?.get("message")?.let { it as? JsonPrimitive }?.contentOrNull
        throw DataConnectException("$operationName failed with HTTP status ${response.status}: ${message ?: response.body.take(500)}")
    }
    val json = element as? JsonObject ?: throw DataConnectException("$operationName returned a response that is not a JSON object: ${response.body.take(500)}")
    val errors = (json["errors"] as? JsonArray).orEmpty().map { error ->
        val errorObject = error as? JsonObject
        ErrorInfoImpl(
            message = errorObject?.get("message")?.let { it as? JsonPrimitive }?.contentOrNull ?: error.toString(),
            path = (errorObject?.get("path") as? JsonArray).orEmpty().map { segment ->
                val primitive = segment.jsonPrimitive
                primitive.intOrNull?.takeIf { !primitive.isString }?.let { DataConnectPathSegment.ListIndex(it) }
                    ?: DataConnectPathSegment.Field(primitive.content)
            },
        )
    }
    return OperationResponse(data = json["data"]?.takeIf { it !is JsonNull }, errors = errors)
}

/** Decodes the data of a response, or throws [DataConnectOperationException] describing the errors and/or the decoding failure. */
internal fun <Data> OperationResponse.decodeOrThrow(operationName: String, decode: (JsonElement) -> Data): Data {
    val rawData = (data as? JsonObject)?.toNaturalValue()?.let {
        @Suppress("UNCHECKED_CAST")
        it as Map<String, Any?>
    }
    val decoded = data?.let { runCatching { decode(it) } }
    if (errors.isNotEmpty()) {
        throw DataConnectOperationException(
            message = "operation $operationName encountered errors during execution: ${errors.joinToString("; ") { it.toString() }}",
            cause = decoded?.exceptionOrNull(),
            response = FailureResponseImpl(rawData, errors, decoded?.getOrNull()),
        )
    }
    if (decoded == null) {
        throw DataConnectOperationException(
            message = "operation $operationName returned no data",
            response = FailureResponseImpl<Data>(rawData, errors, null),
        )
    }
    return decoded.getOrElse { failure ->
        throw DataConnectOperationException(
            message = "operation $operationName returned data that could not be decoded: ${failure.message}",
            cause = failure,
            response = FailureResponseImpl<Data>(rawData, errors, null),
        )
    }
}

private const val SDK_VERSION = "firebase-kotlin-sdk"
