/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.CacheSettings
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectException
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.DataSource
import com.google.firebase.dataconnect.QueryRef
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import platform.Foundation.NSError
import swiftPMImport.dev.gitlive.firebase.dataconnect.FDCBridge
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * The integers of the wrapper's API (see apple/FirebaseDataConnectObjC/Sources/DataConnectBridge.swift).
 */
private const val CACHE_STORAGE_NONE = 0L
private const val CACHE_STORAGE_MEMORY = 1L
private const val CACHE_STORAGE_PERSISTENT = 2L
private const val FETCH_POLICY_PREFER_CACHE = 0L
private const val FETCH_POLICY_CACHE_ONLY = 1L
private const val FETCH_POLICY_SERVER_ONLY = 2L
private const val DATA_SOURCE_CACHE = 1L
private const val ERROR_OPERATION_FAILED = 1L
private const val ERRORS_KEY = "errors"
private const val DATA_KEY = "data"

/**
 * The Firebase iOS Data Connect SDK, through the Objective-C wrapper package in `apple/FirebaseDataConnectObjC` (the SDK
 * is Swift-only). The SDK caches its instances per app, connector and settings and has no way to release one, so
 * [close] only lets the Kotlin instance go.
 */
internal actual class NativeDataConnect actual constructor(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings) {

    private val ios = FDCBridge(
        appName = app.name,
        serviceId = config.serviceId,
        location = config.location,
        connector = config.connector,
        host = settings.host,
        sslEnabled = settings.sslEnabled,
        cacheStorage = when (settings.cacheSettings?.storage) {
            null -> CACHE_STORAGE_NONE
            CacheSettings.Storage.MEMORY -> CACHE_STORAGE_MEMORY
            CacheSettings.Storage.PERSISTENT -> CACHE_STORAGE_PERSISTENT
        },
        cacheMaxAgeSeconds = settings.cacheSettings?.maxAgeSeconds ?: 0.0,
    )

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulator(host, port.toLong())
    }

    actual suspend fun executeQuery(operationName: String, variables: JsonObject, fetchPolicy: QueryRef.FetchPolicy): NativeQueryResult = suspendCancellableCoroutine { continuation ->
        val policy = when (fetchPolicy) {
            QueryRef.FetchPolicy.PREFER_CACHE -> FETCH_POLICY_PREFER_CACHE
            QueryRef.FetchPolicy.CACHE_ONLY -> FETCH_POLICY_CACHE_ONLY
            QueryRef.FetchPolicy.SERVER_ONLY -> FETCH_POLICY_SERVER_ONLY
        }
        ios.executeQuery(operationName, variables.toString(), policy) { data, source, error ->
            if (error != null) {
                continuation.resumeWithException(error.toFailure())
            } else {
                continuation.resume(NativeQueryResult(data.toJsonElement(), if (source == DATA_SOURCE_CACHE) DataSource.CACHE else DataSource.SERVER))
            }
        }
    }

    actual suspend fun executeMutation(operationName: String, variables: JsonObject): JsonElement = suspendCancellableCoroutine { continuation ->
        ios.executeMutation(operationName, variables.toString()) { data, error ->
            if (error != null) {
                continuation.resumeWithException(error.toFailure())
            } else {
                continuation.resume(data.toJsonElement())
            }
        }
    }

    actual fun close() {}
}

private fun String?.toJsonElement(): JsonElement = if (this == null) JsonNull else Json.parseToJsonElement(this)

/** The exception of an error the wrapper reports, see the wrapper's error codes. */
private fun NSError.toFailure(): Throwable = if (code == ERROR_OPERATION_FAILED) {
    NativeOperationFailure(
        message = localizedDescription,
        rawData = (userInfo[DATA_KEY] as? String)?.toJsonElement(),
        errors = parseErrorInfos((userInfo[ERRORS_KEY] as? String)?.toJsonElement()),
    )
} else {
    DataConnectException(localizedDescription)
}
