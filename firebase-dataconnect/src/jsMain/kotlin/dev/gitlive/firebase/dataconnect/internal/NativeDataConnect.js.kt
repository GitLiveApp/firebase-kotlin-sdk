/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectException
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.DataSource
import com.google.firebase.dataconnect.QueryRef
import dev.gitlive.firebase.dataconnect.externals.DataConnect
import dev.gitlive.firebase.dataconnect.externals.connectDataConnectEmulator
import dev.gitlive.firebase.dataconnect.externals.executeMutation
import dev.gitlive.firebase.dataconnect.externals.executeQuery
import dev.gitlive.firebase.dataconnect.externals.getDataConnect
import dev.gitlive.firebase.dataconnect.externals.makeMemoryCacheProvider
import dev.gitlive.firebase.dataconnect.externals.mutationRef
import dev.gitlive.firebase.dataconnect.externals.queryRef
import dev.gitlive.firebase.dataconnect.externals.terminate
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlin.js.json

/**
 * The Firebase JS SDK's `firebase/data-connect`. Its instances are cached per app and connector like the Android SDK's,
 * and [close] terminates the instance so that a later `getInstance` creates a new one with possibly different settings.
 * The JS SDK has no host/SSL settings (it always calls the production service unless the emulator is connected), and its
 * only cache storage is memory, which `CacheSettings.Storage.PERSISTENT` maps to as well.
 */
internal actual class NativeDataConnect actual constructor(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings) {

    private val js: DataConnect = getDataConnect(
        app.js,
        json("location" to config.location, "connector" to config.connector, "service" to config.serviceId),
        settings.cacheSettings?.let { cache ->
            json("cacheSettings" to json("cacheProvider" to makeMemoryCacheProvider(), "maxAgeSeconds" to cache.maxAgeSeconds))
        } ?: json(),
    )

    actual fun useEmulator(host: String, port: Int) {
        jsCall { connectDataConnectEmulator(js, host, port, false) }
    }

    actual suspend fun executeQuery(operationName: String, variables: JsonObject, fetchPolicy: QueryRef.FetchPolicy): NativeQueryResult {
        val result = jsCall { executeQuery(queryRef(js, operationName, variables.toJs()), json("fetchPolicy" to fetchPolicy.name)).await() }
        return NativeQueryResult(result.data.toJsonElement(), if (result.source == "CACHE") DataSource.CACHE else DataSource.SERVER)
    }

    actual suspend fun executeMutation(operationName: String, variables: JsonObject): JsonElement = jsCall { executeMutation(mutationRef(js, operationName, variables.toJs())).await() }.data.toJsonElement()

    actual fun close() {
        terminate(js)
    }
}

/** The plain JS object of a JSON object, as the SDK expects variables. */
private fun JsonObject.toJs(): Any = JSON.parse(toString())

/** The JSON of a value the SDK returned. */
private fun Any?.toJsonElement(): JsonElement = if (this == null || this == undefined) JsonNull else Json.parseToJsonElement(JSON.stringify(this))

/** Runs an SDK call, rethrowing its errors as this module's exceptions. */
private inline fun <T> jsCall(block: () -> T): T = try {
    block()
} catch (e: Throwable) {
    throw e.toDataConnectFailure()
} catch (e: dynamic) {
    throw e.unsafeCast<Throwable>().toDataConnectFailure()
}

/**
 * A `DataConnectOperationError` (the operation ran but the server reported errors) becomes [NativeOperationFailure] with
 * its `response`; any other `DataConnectError` becomes `DataConnectException`.
 */
private fun Throwable.toDataConnectFailure(): Throwable {
    val error = asDynamic()
    val response = error.response
    val message = (error.message as? String) ?: toString()
    return when {
        response != null && response != undefined -> NativeOperationFailure(
            message = message,
            rawData = (response.data as Any?).toJsonElement().takeIf { it !is JsonNull },
            errors = parseErrorInfos((response.errors as Any?).toJsonElement()),
            cause = this,
        )
        error.code != null && error.code != undefined -> DataConnectException(message, this)
        else -> this
    }
}
