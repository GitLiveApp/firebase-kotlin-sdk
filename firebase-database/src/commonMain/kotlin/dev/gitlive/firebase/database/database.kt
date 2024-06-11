/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type.ADDED
import dev.gitlive.firebase.database.ChildEvent.Type.CHANGED
import dev.gitlive.firebase.database.ChildEvent.Type.MOVED
import dev.gitlive.firebase.database.ChildEvent.Type.REMOVED
import dev.gitlive.firebase.internal.encode
import dev.gitlive.firebase.internal.encodeAsObject
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
    fun setLoggingEnabled(enabled: Boolean)
    fun setPersistenceEnabled(enabled: Boolean)
    fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long)
    fun useEmulator(host: String, port: Int)

    fun goOffline()

    fun goOnline()
}

data class ChildEvent internal constructor(
    val snapshot: DataSnapshot,
    val type: Type,
    val previousChildName: String?,
) {
    enum class Type {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED,
    }
}

internal expect open class NativeQuery

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

@PublishedApi
internal expect class NativeDatabaseReference : NativeQuery {
    val key: String?
    fun push(): NativeDatabaseReference
    suspend fun setValueEncoded(encodedValue: Any?)
    suspend fun updateEncodedChildren(encodedUpdate: EncodedObject)
    fun child(path: String): NativeDatabaseReference
    fun onDisconnect(): NativeOnDisconnect

    suspend fun removeValue()

    suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit = {}, transactionUpdate: (currentData: T) -> T): DataSnapshot
}

class DatabaseReference internal constructor(@PublishedApi internal val nativeReference: NativeDatabaseReference) : Query(nativeReference) {

    val key: String? = nativeReference.key
    fun push(): DatabaseReference = DatabaseReference(nativeReference.push())
    fun child(path: String): DatabaseReference = DatabaseReference(nativeReference.child(path))
    fun onDisconnect(): OnDisconnect = OnDisconnect(nativeReference.onDisconnect())

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(value) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) =
        setValue(value) {
            this.encodeDefaults = encodeDefaults
        }
    suspend inline fun <reified T> setValue(value: T?, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        nativeReference.setValueEncoded(encode(value, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(strategy, value) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        setValue(strategy, value) {
            this.encodeDefaults = encodeDefaults
        }
    suspend inline fun <T> setValue(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = nativeReference.setValueEncoded(encode(strategy, value, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("updateChildren(update) { this.encodeDefaults = encodeDefaults }"))
    suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) = updateChildren(update) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun updateChildren(update: Map<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = nativeReference.updateEncodedChildren(
        encodeAsObject(update, buildSettings),
    )

    suspend fun removeValue() = nativeReference.removeValue()

    suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit = {}, transactionUpdate: (currentData: T) -> T): DataSnapshot = nativeReference.runTransaction(strategy, buildSettings, transactionUpdate)
}

expect class DataSnapshot {
    val exists: Boolean
    val key: String?
    val ref: DatabaseReference
    val value: Any?
    inline fun <reified T> value(): T
    inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T
    fun child(path: String): DataSnapshot
    val hasChildren: Boolean
    val children: Iterable<DataSnapshot>
}

expect class DatabaseException(message: String?, cause: Throwable?) : RuntimeException

@PublishedApi
internal expect class NativeOnDisconnect {
    suspend fun removeValue()
    suspend fun cancel()
    suspend fun setValue(encodedValue: Any?)
    suspend fun updateEncodedChildren(encodedUpdate: EncodedObject)
}

class OnDisconnect internal constructor(@PublishedApi internal val native: NativeOnDisconnect) {
    suspend fun removeValue() = native.removeValue()
    suspend fun cancel() = native.cancel()

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(value) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) =
        setValue(value) { this.encodeDefaults = encodeDefaults }
    suspend inline fun <reified T> setValue(value: T?, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        native.setValue(encode(value, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(strategy, value) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        setValue(strategy, value) { this.encodeDefaults = encodeDefaults }
    suspend inline fun <T> setValue(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setValue(encode(strategy, value, buildSettings))

    suspend inline fun updateChildren(update: Map<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.updateEncodedChildren(
        encodeAsObject(update, buildSettings),
    )

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("updateChildren(update) { this.encodeDefaults = encodeDefaults }"))
    suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) = updateChildren(update) {
        this.encodeDefaults = encodeDefaults
    }
}
