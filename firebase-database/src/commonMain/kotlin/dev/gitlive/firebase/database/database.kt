/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type.*
import dev.gitlive.firebase.encode
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
expect val Firebase.database: FirebaseDatabase

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
expect fun Firebase.database(url: String): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
expect fun Firebase.database(app: FirebaseApp): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
expect fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase

expect class FirebaseDatabase {
    fun reference(path: String): DatabaseReference
    fun reference(): DatabaseReference
    fun setPersistenceEnabled(enabled: Boolean)
    fun setLoggingEnabled(enabled: Boolean)
    fun useEmulator(host: String, port: Int)
}

data class ChildEvent internal constructor(
    val snapshot: DataSnapshot,
    val type: Type,
    val previousChildName: String?
) {
    enum class Type {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED
    }
}

expect class NativeQuery

expect open class Query internal constructor(nativeQuery: NativeQuery) {
    val valueEvents: Flow<DataSnapshot>
    fun childEvents(vararg types: ChildEvent.Type = arrayOf(ADDED, CHANGED, MOVED, REMOVED)): Flow<ChildEvent>
    fun orderByKey(): Query
    fun orderByValue(): Query
    fun orderByChild(path: String): Query
    fun startAt(value: String, key: String? = null): Query
    fun startAt(value: Double, key: String? = null): Query
    fun startAt(value: Boolean, key: String? = null): Query
    fun endAt(value: String, key: String? = null): Query
    fun endAt(value: Double, key: String? = null): Query
    fun endAt(value: Boolean, key: String? = null): Query
    fun limitToFirst(limit: Int): Query
    fun limitToLast(limit: Int): Query
    fun equalTo(value: String, key: String? = null): Query
    fun equalTo(value: Double, key: String? = null): Query
    fun equalTo(value: Boolean, key: String? = null): Query
}

abstract class BaseDatabaseReference internal constructor(nativeQuery: NativeQuery) : Query(nativeQuery) {
    suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) =
        setValue(value, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend inline fun <reified T> setValue(value: T?, encodeSettings: EncodeSettings = EncodeSettings()) =
        setValueEncoded(encode(value, encodeSettings))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        setValue(strategy, value, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings = EncodeSettings()) = setValueEncoded(encode(strategy, value, encodeSettings))

    abstract suspend fun setValueEncoded(encodedValue: Any?)
    suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) = updateChildren(update, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    abstract suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings())
}

expect class DatabaseReference : BaseDatabaseReference {
    val key: String?
    fun push(): DatabaseReference
    fun child(path: String): DatabaseReference
    fun onDisconnect(): OnDisconnect

    suspend fun removeValue()

    suspend fun <T> runTransaction(strategy: KSerializer<T>, decodeSettings: DecodeSettings = DecodeSettings(), transactionUpdate: (currentData: T) -> T): DataSnapshot
}

expect class DataSnapshot {
    val exists: Boolean
    val key: String?
    val ref: DatabaseReference
    val value: Any?
    inline fun <reified T> value(): T
    fun <T> value(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings = DecodeSettings()): T
    fun child(path: String): DataSnapshot
    val hasChildren: Boolean
    val children: Iterable<DataSnapshot>
}

expect class DatabaseException(message: String?, cause: Throwable?) : RuntimeException

abstract class BaseOnDisconnect internal constructor() {
    suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) =
        setValue(value, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend inline fun <reified T> setValue(value: T?, encodeSettings: EncodeSettings = EncodeSettings()) =
        setValue(encode(value, encodeSettings))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        setValue(strategy, value, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings = EncodeSettings()) = setValue(encode(strategy, value, encodeSettings))
    abstract suspend fun setValue(encodedValue: Any?)

    abstract suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings())
    suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) = updateChildren(update, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
}

expect class OnDisconnect : BaseOnDisconnect {
    suspend fun removeValue()
    suspend fun cancel()
}
