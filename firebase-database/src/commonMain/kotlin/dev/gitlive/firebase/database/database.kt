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
public expect val Firebase.database: FirebaseDatabase

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
public expect fun Firebase.database(url: String): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
public expect fun Firebase.database(app: FirebaseApp): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
public expect fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase

/**
 * The entry point for accessing a Firebase Database. You can get an instance by calling [Firebase.database]. To access a location in the database and read or write data, use [FirebaseDatabase.reference].
 */
public expect class FirebaseDatabase {
    /**
     * Gets a DatabaseReference for the provided path.
     *
     * @param path Path to a location in your FirebaseDatabase.
     * @return A DatabaseReference pointing to the specified path.
     */
    public fun reference(path: String): DatabaseReference

    /**
     * Gets a DatabaseReference for the database root node.
     *
     * @return A DatabaseReference pointing to the root node.
     */
    public fun reference(): DatabaseReference
    public fun setLoggingEnabled(enabled: Boolean)

    /**
     * The Firebase Database client will cache synchronized data and keep track of all writes you've
     * initiated while your application is running. It seamlessly handles intermittent network
     * connections and re-sends write operations when the network connection is restored.
     *
     * However by default your write operations and cached data are only stored in-memory and will
     * be lost when your app restarts. By setting this value to `true`, the data will be persisted to
     * on-device (disk) storage and will thus be available again when the app is restarted (even when
     * there is no network connectivity at that time). Note that this method must be called before
     * creating your first Database reference and only needs to be called once per application.
     *
     * @param enabled Set to true to enable disk persistence, set to false to disable it.
     */
    public fun setPersistenceEnabled(enabled: Boolean)

    /**
     * By default Firebase Database will use up to 10MB of disk space to cache data. If the cache
     * grows beyond this size, Firebase Database will start removing data that hasn't been recently
     * used. If you find that your application caches too little or too much data, call this method to
     * change the cache size. This method must be called before creating your first Database reference
     * and only needs to be called once per application.
     *
     * Note that the specified cache size is only an approximation and the size on disk may
     * temporarily exceed it at times. Cache sizes smaller than 1 MB or greater than 100 MB are not
     * supported.
     *
     * @param cacheSizeInBytes The new size of the cache in bytes.
     */
    public fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long)

    /**
     * Modifies this FirebaseDatabase instance to communicate with the Realtime Database emulator.
     *
     * <p>Note: Call this method before using the instance to do any database operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 9000)
     */
    public fun useEmulator(host: String, port: Int)

    /**
     * Shuts down our connection to the Firebase Database backend until [goOnline] is called.
     */
    public fun goOffline()

    /**
     * Resumes our connection to the Firebase Database backend after a previous [goOffline].
     * call.
     */
    public fun goOnline()
}

/**
 * Used to emit events about changes in the child locations of a given [Query] when using the
 * [childEvents] Flow.
 */
public data class ChildEvent internal constructor(
    val snapshot: DataSnapshot,
    val type: Type,
    val previousChildName: String?,
) {
    public enum class Type {
        /**
         * Emitted when a new child is added to the location.
         *
         * @param snapshot An immutable snapshot of the data at the new child location
         * @param previousChildName The key name of sibling location ordered before the new child. This
         * ```
         *     will be null for the first child node of a location.
         * ```
         */
        ADDED,

        /**
         * Emitted when the data at a child location has changed.
         *
         * @param snapshot An immutable snapshot of the data at the new data at the child location
         * @param previousChildName The key name of sibling location ordered before the child. This will
         * ```
         *     be null for the first child node of a location.
         * ```
         */
        CHANGED,

        /**
         * Emitted when a child location's priority changes.
         *
         * @param snapshot An immutable snapshot of the data at the location that moved.
         * @param previousChildName The key name of the sibling location ordered before the child
         * ```
         *     location. This will be null if this location is ordered first.
         * ```
         */
        MOVED,

        /**
         * Emitted when a child is removed from the location.
         *
         * @param snapshot An immutable snapshot of the data at the child that was removed.
         */
        REMOVED,
    }
}

internal expect open class NativeQuery

/**
 * The Query class (and its subclass, [DatabaseReference]) are used for reading data.
 * Listeners are attached, and they will be triggered when the corresponding data changes.
 *
 * Instances of Query are obtained by calling [startAt], [endAt], or [limit] on a [DatabaseReference].
 */
public expect open class Query internal constructor(nativeQuery: NativeQuery) {
    public val valueEvents: Flow<DataSnapshot>
    public fun childEvents(vararg types: ChildEvent.Type = arrayOf(ADDED, CHANGED, MOVED, REMOVED)): Flow<ChildEvent>

    /**
     * Creates a query in which child nodes are ordered by their keys.
     *
     * @return A query with the new constraint
     */
    public fun orderByKey(): Query

    /**
     * Creates a query in which nodes are ordered by their value
     *
     * @return A query with the new constraint
     */
    public fun orderByValue(): Query

    /**
     * Creates a query in which child nodes are ordered by the values of the specified path.
     *
     * @param path The path to the child node to use for sorting
     * @return A query with the new constraint
     */
    public fun orderByChild(path: String): Query

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: String, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: Double, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: Boolean, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: String, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: Double, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: Boolean, key: String? = null): Query

    /**
     * Creates a query with limit and anchor it to the start of the window.
     *
     * @param limit The maximum number of child nodes to return
     * @return A query with the new constraint
     */
    public fun limitToFirst(limit: Int): Query

    /**
     * Creates a query with limit and anchor it to the end of the window.
     *
     * @param limit The maximum number of child nodes to return
     * @return A query with the new constraint
     */
    public fun limitToLast(limit: Int): Query

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: String, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: Double, key: String? = null): Query

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: Boolean, key: String? = null): Query
}

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

/**
 * A Firebase reference represents a particular location in your Database and can be used for
 * reading or writing data to that Database location.
 *
 * This class is the starting point for all Database operations. After you've initialized it with
 * a URL, you can use it to read data, write data, and to create new DatabaseReferences.
 */
public class DatabaseReference internal constructor(internal val nativeReference: NativeDatabaseReference) : Query(nativeReference) {
    /**
     * @return The last token in the location pointed to by this reference or null if this reference
     *     points to the database root
     */
    public val key: String? = nativeReference.key

    /**
     * Create a reference to an auto-generated child location. The child key is generated client-side
     * and incorporates an estimate of the server's time for sorting purposes. Locations generated on
     * a single client will be sorted in the order that they are created, and will be sorted
     * approximately in order across all clients.
     *
     * @return A DatabaseReference pointing to the new location
     */
    public fun push(): DatabaseReference = DatabaseReference(nativeReference.push())

    /**
     * Get a reference to location relative to this one
     *
     * @param path The relative path from this reference to the new one that should be created
     * @return A new DatabaseReference to the given path
     */
    public fun child(path: String): DatabaseReference = DatabaseReference(nativeReference.child(path))

    /**
     * Provides access to disconnect operations at this location
     *
     * @return An object for managing disconnect operations at this location
     */
    public fun onDisconnect(): OnDisconnect = OnDisconnect(nativeReference.onDisconnect())

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(value) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) {
        setValue(value) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T> setValue(value: T?, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setValueEncoded(encode(value, buildSettings))
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(strategy, value) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) {
        setValue(strategy, value) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T> setValue(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setValueEncoded(encode(strategy, value, buildSettings))
    }

    @PublishedApi
    internal suspend fun setValueEncoded(encodedValue: Any?) {
        nativeReference.setValueEncoded(encodedValue)
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("updateChildren(update) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) {
        updateChildren(update) {
            this.encodeDefaults = encodeDefaults
        }
    }

    /**
     * Update the specific child keys to the specified values. Passing null in a map to
     * updateChildren() will remove the value at the specified location.
     *
     * @param update The paths to update and their new values
     * @return The {@link Task} for this operation.
     */
    public suspend inline fun updateChildren(update: Map<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncodedChildren(
            encodeAsObject(update, buildSettings),
        )
    }

    @PublishedApi
    internal suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        nativeReference.updateEncodedChildren(encodedUpdate)
    }

    /**
     * Set the value at this location to 'null'
     *
     * @return The {@link Task} for this operation.
     */
    public suspend fun removeValue() {
        nativeReference.removeValue()
    }

    /**
     * Run a transaction on the data at this location.
     *
     * @param handler An object to handle running the transaction
     */
    public suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit = {}, transactionUpdate: (currentData: T) -> T): DataSnapshot = nativeReference.runTransaction(strategy, buildSettings, transactionUpdate)
}

/**
 * A DataSnapshot instance contains data from a Firebase Database location. Any time you read
 * Database data, you receive the data as a DataSnapshot.
 *
 * They are efficiently-generated immutable copies of the data at a Firebase Database location. They
 * can't be modified and will never change. To modify data at a location, use a <br>
 * [DatabaseReference] reference (e.g. with [DatabaseReference.setValue]).
 */
public expect class DataSnapshot {
    /**
     * Returns true if the snapshot contains a non-null value.
     *
     * @return True if the snapshot contains a non-null value, otherwise false
     */
    public val exists: Boolean

    /**
     * @return The key name for the source location of this snapshot or null if this snapshot points
     *     to the database root.
     */
    public val key: String?

    /**
     * Used to obtain a reference to the source location for this snapshot.
     *
     * @return A DatabaseReference corresponding to the location that this snapshot came from
     */
    public val ref: DatabaseReference

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public val value: Any?

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public inline fun <reified T> value(): T

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T

    public fun child(path: String): DataSnapshot

    /**
     * Indicates whether this snapshot has any children
     *
     * @return True if the snapshot has any children, otherwise false
     */
    public val hasChildren: Boolean

    /**
     * Gives access to all of the immediate children of this snapshot. Can be used in native for
     * loops:
     *
     * ```
     * for (DataSnapshot child : parent.getChildren()) {
     *   &nbsp;&nbsp;&nbsp;&nbsp;...
     * }
     * ```
     *
     * @return The immediate children of this snapshot
     */
    public val children: Iterable<DataSnapshot>
}

/**
 * Exception that gets thrown when an operation on Firebase Database fails.
 */
public expect class DatabaseException(message: String?, cause: Throwable?) : RuntimeException

internal expect class NativeOnDisconnect {
    suspend fun removeValue()
    suspend fun cancel()
    suspend fun setEncodedValue(encodedValue: Any?)
    suspend fun updateEncodedChildren(encodedUpdate: EncodedObject)
}

/**
 * The OnDisconnect class is used to manage operations that will be run on the server when this
 * client disconnects. It can be used to add or remove data based on a client's connection status.
 * It is very useful in applications looking for 'presence' functionality.
 *
 * Instances of this class are obtained by calling [DatabaseReference.onDisconnect]
 * on a Firebase Database ref.
 */
public class OnDisconnect internal constructor(internal val native: NativeOnDisconnect) {
    /**
     * Remove the value at this location when the client disconnects
     *
     * @return The {@link Task} for this operation.
     */
    public suspend fun removeValue() {
        native.removeValue()
    }

    /**
     * Cancel any disconnect operations that are queued up at this location
     */
    public suspend fun cancel() {
        native.cancel()
    }

    /**
     * Ensure the data at this location is set to the specified value when the client is disconnected
     * (due to closing the browser, navigating to a new page, or network issues).
     *
     * This method is especially useful for implementing "presence" systems, where a value should be
     * changed or cleared when a user disconnects so that they appear "offline" to other users.
     *
     * @param value The value to be set when a disconnect occurs or null to delete the existing value
     */
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(value) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) {
        setValue(value) { this.encodeDefaults = encodeDefaults }
    }

    /**
     * Ensure the data at this location is set to the specified value when the client is disconnected
     * (due to closing the browser, navigating to a new page, or network issues).
     *
     * This method is especially useful for implementing "presence" systems, where a value should be
     * changed or cleared when a user disconnects so that they appear "offline" to other users.
     *
     * @param value The value to be set when a disconnect occurs or null to delete the existing value
     */
    public suspend inline fun <reified T> setValue(value: T?, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncodedValue(encode(value, buildSettings))
    }

    /**
     * Ensure the data at this location is set to the specified value when the client is disconnected
     * (due to closing the browser, navigating to a new page, or network issues).
     *
     * This method is especially useful for implementing "presence" systems, where a value should be
     * changed or cleared when a user disconnects so that they appear "offline" to other users.
     *
     * @param value The value to be set when a disconnect occurs or null to delete the existing value
     */
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(strategy, value) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) {
        setValue(strategy, value) { this.encodeDefaults = encodeDefaults }
    }

    /**
     * Ensure the data at this location is set to the specified value when the client is disconnected
     * (due to closing the browser, navigating to a new page, or network issues).
     *
     * This method is especially useful for implementing "presence" systems, where a value should be
     * changed or cleared when a user disconnects so that they appear "offline" to other users.
     *
     * @param value The value to be set when a disconnect occurs or null to delete the existing value
     */
    public suspend inline fun <T> setValue(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setValue(encode(strategy, value, buildSettings))
    }

    @PublishedApi
    internal suspend fun setEncodedValue(encodedValue: Any?) {
        native.setEncodedValue(encodedValue)
    }

    /**
     * Ensure the data has the specified child values updated when the client is disconnected
     *
     * @param update The paths to update, along with their desired values
     */
    public suspend inline fun updateChildren(update: Map<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncodedChildren(
            encodeAsObject(update, buildSettings),
        )
    }

    /**
     * Ensure the data has the specified child values updated when the client is disconnected
     *
     * @param update The paths to update, along with their desired values
     */
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("updateChildren(update) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) {
        updateChildren(update) {
            this.encodeDefaults = encodeDefaults
        }
    }

    @PublishedApi
    internal suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        native.updateEncodedChildren(encodedUpdate)
    }
}
