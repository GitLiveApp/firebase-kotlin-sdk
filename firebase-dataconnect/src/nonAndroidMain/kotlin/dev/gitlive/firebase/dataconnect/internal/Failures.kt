/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataConnectOperationException
import com.google.firebase.dataconnect.DataConnectOperationFailureResponse
import com.google.firebase.dataconnect.DataConnectPathSegment
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

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

/** The errors of a failed operation as the SDKs report them: a JSON array of `{ "message": "...", "path": [ "field", 0 ] }`. */
internal fun parseErrorInfos(errors: JsonElement?): List<ErrorInfoImpl> = (errors as? JsonArray).orEmpty().map { error ->
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

/** The data of a response as plain values, for [DataConnectOperationFailureResponse.rawData]. */
private fun JsonElement?.toRawData(): Map<String, Any?>? = (this as? JsonObject)?.toNaturalValue()?.let {
    @Suppress("UNCHECKED_CAST")
    it as Map<String, Any?>
}

/** Decodes the data of a successful operation, or throws [DataConnectOperationException] describing the decoding failure. */
internal fun <Data> decodeData(operationName: String, data: JsonElement, decode: (JsonElement) -> Data): Data = runCatching { decode(data) }.getOrElse { failure ->
    throw DataConnectOperationException(
        message = "operation $operationName returned data that could not be decoded: ${failure.message}",
        cause = failure,
        response = FailureResponseImpl<Data>(data.toRawData(), emptyList(), null),
    )
}

/** The [DataConnectOperationException] of a failure the SDK reported, with the data decoded when possible. */
internal fun <Data> NativeOperationFailure.toException(operationName: String, decode: (JsonElement) -> Data): DataConnectOperationException {
    val decoded = rawData?.takeIf { it !is JsonNull }?.let { runCatching { decode(it) } }
    return DataConnectOperationException(
        message = if (errors.isEmpty()) message.orEmpty() else "operation $operationName encountered errors during execution: ${errors.joinToString("; ")}",
        cause = decoded?.exceptionOrNull() ?: cause,
        response = FailureResponseImpl(rawData.toRawData(), errors, decoded?.getOrNull()),
    )
}
