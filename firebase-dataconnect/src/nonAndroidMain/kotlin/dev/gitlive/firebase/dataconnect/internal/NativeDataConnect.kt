/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.CacheSettings
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectOperationFailureResponse
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.DataSource
import com.google.firebase.dataconnect.QueryRef
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.DurationUnit

/**
 * The platform's Data Connect SDK behind one instance: the Firebase JS SDK's `firebase/data-connect` on JS, and the
 * Firebase iOS Data Connect SDK through this module's Objective-C wrapper on Apple. Variables and data cross as JSON, the
 * form both SDKs serialize to, so the kotlinx-serialization side is shared (see JsonCodec.kt).
 *
 * Failures are thrown as [NativeOperationFailure] when the SDK reports errors for the operation (the operation reference,
 * which knows the deserializer, turns it into `DataConnectOperationException` with the decoded data), and as
 * `DataConnectException` otherwise.
 */
internal expect class NativeDataConnect(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings) {
    /** Points the SDK at the emulator; called before any operation runs. */
    fun useEmulator(host: String, port: Int)

    /** Runs a query with [fetchPolicy]; `CACHE_ONLY` without cached data fails with `DataConnectException`. */
    suspend fun executeQuery(operationName: String, variables: JsonObject, fetchPolicy: QueryRef.FetchPolicy): NativeQueryResult

    /** Runs a mutation and returns its data. */
    suspend fun executeMutation(operationName: String, variables: JsonObject): JsonElement

    /** Releases the SDK's instance, so that the next `getInstance` creates a new one. */
    fun close()
}

/** The data of a query and where it came from. */
internal class NativeQueryResult(val data: JsonElement, val source: DataSource)

/**
 * The cache's maximum age for the platform SDK, in seconds. Every SDK, the Android one included, considers an entry
 * older than this stale, so with the default of zero `PREFER_CACHE` always goes to the server and only `CACHE_ONLY`
 * (which accepts stale entries) is served from the cache.
 */
internal val CacheSettings.maxAgeSeconds: Double get() = maxAge.toDouble(DurationUnit.SECONDS)

/** An operation the SDK reported errors for, with the data it returned nonetheless and the errors, as the SDK sent them. */
internal class NativeOperationFailure(
    message: String,
    val rawData: JsonElement?,
    val errors: List<DataConnectOperationFailureResponse.ErrorInfo>,
    cause: Throwable? = null,
) : Exception(message, cause)
