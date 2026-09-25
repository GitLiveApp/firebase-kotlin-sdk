/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import com.google.firebase.database.Logger
import com.google.firebase.database.Transaction
import com.google.firebase.database.childEvents
import com.google.firebase.database.snapshots
import com.google.firebase.database.transactionSuccess
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.database.ChildEvent.Type.ADDED
import dev.gitlive.firebase.database.ChildEvent.Type.CHANGED
import dev.gitlive.firebase.database.ChildEvent.Type.MOVED
import dev.gitlive.firebase.database.ChildEvent.Type.REMOVED
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.encode
import dev.gitlive.firebase.internal.encodeAsObject
import dev.gitlive.firebase.internal.reencodeTransformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import com.google.firebase.database.ChildEvent as CompatChildEvent
import com.google.firebase.database.DataSnapshot as CompatDataSnapshot
import com.google.firebase.database.DatabaseError as CompatDatabaseError
import com.google.firebase.database.DatabaseReference as CompatDatabaseReference
import com.google.firebase.database.FirebaseDatabase as CompatFirebaseDatabase
import com.google.firebase.database.MutableData as CompatMutableData
import com.google.firebase.database.OnDisconnect as CompatOnDisconnect
import com.google.firebase.database.Query as CompatQuery

/**
 * The entry point for accessing a Firebase Database. You can get an instance by calling [Firebase.database]. To access a location in the database and read or write data, use [FirebaseDatabase.reference].
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.database.FirebaseDatabase] this wraps.
 */
public class FirebaseDatabase internal constructor(public val compat: CompatFirebaseDatabase) {
    /**
     * Gets a DatabaseReference for the provided path.
     *
     * @param path Path to a location in your FirebaseDatabase.
     * @return A DatabaseReference pointing to the specified path.
     */
    public fun reference(path: String): DatabaseReference = DatabaseReference(compat.getReference(path))

    /**
     * Gets a DatabaseReference for the database root node.
     *
     * @return A DatabaseReference pointing to the root node.
     */
    public fun reference(): DatabaseReference = DatabaseReference(compat.reference)

    /** Enables or disables the SDK's debug logging. */
    public fun setLoggingEnabled(enabled: Boolean) {
        compat.setLogLevel(if (enabled) Logger.Level.DEBUG else Logger.Level.NONE)
    }

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
    public fun setPersistenceEnabled(enabled: Boolean) {
        compat.setPersistenceEnabled(enabled)
        PersistenceSettings.enabled[compat] = enabled
    }

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
    public fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        compat.setPersistenceCacheSizeBytes(cacheSizeInBytes)
    }

    /**
     * Modifies this FirebaseDatabase instance to communicate with the Realtime Database emulator.
     *
     * <p>Note: Call this method before using the instance to do any database operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 9000)
     */
    public fun useEmulator(host: String, port: Int) {
        compat.useEmulator(host, port)
    }

    /**
     * Shuts down our connection to the Firebase Database backend until [goOnline] is called.
     */
    public fun goOffline() {
        compat.goOffline()
    }

    /**
     * Resumes our connection to the Firebase Database backend after a previous [goOffline].
     * call.
     */
    public fun goOnline() {
        compat.goOnline()
    }

    /**
     * Purges all outstanding writes to the Firebase Database server.
     *
     * The writes will be rolled back locally and the affected events will be raised again with the
     * reverted data.
     */
    public fun purgeOutstandingWrites() {
        compat.purgeOutstandingWrites()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseDatabase && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseDatabase($compat)"
}

/** The persistence setting recorded per database for the deprecated `persistenceEnabled` accessors. */
internal object PersistenceSettings {
    val enabled: MutableMap<CompatFirebaseDatabase, Boolean> = mutableMapOf()
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

/**
 * The Query class (and its subclass, [DatabaseReference]) are used for reading data.
 * Listeners are attached, and they will be triggered when the corresponding data changes.
 *
 * Instances of Query are obtained by calling [startAt], [endAt], or [limit] on a [DatabaseReference].
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.database.Query] this wraps.
 */
public open class Query internal constructor(public open val compat: CompatQuery) {
    /** The data at this location and every change to it; the flow fails if the listener is cancelled. */
    public val valueEvents: Flow<DataSnapshot> get() = compat.snapshots.map { DataSnapshot(it) }

    /** The changes to the children of this location of the given [types]; the flow fails if the listener is cancelled. */
    public fun childEvents(vararg types: ChildEvent.Type = arrayOf(ADDED, CHANGED, MOVED, REMOVED)): Flow<ChildEvent> = compat.childEvents.mapNotNull { event ->
        val (type, snapshot, previousChildName) = when (event) {
            is CompatChildEvent.Added -> Triple(ADDED, event.snapshot, event.previousChildName)
            is CompatChildEvent.Changed -> Triple(CHANGED, event.snapshot, event.previousChildName)
            is CompatChildEvent.Moved -> Triple(MOVED, event.snapshot, event.previousChildName)
            is CompatChildEvent.Removed -> Triple(REMOVED, event.snapshot, null)
        }
        if (type in types) ChildEvent(DataSnapshot(snapshot), type, previousChildName) else null
    }

    /**
     * Gets the server values for this query. Updates the cache and raises events if successful. If not
     * connected, falls back to a locally-cached value or null.
     */
    public suspend fun get(): DataSnapshot = DataSnapshot(compat.get().await())

    /**
     * Creates a query in which child nodes are ordered by their keys.
     *
     * @return A query with the new constraint
     */
    public fun orderByKey(): Query = Query(compat.orderByKey())

    /**
     * Creates a query in which nodes are ordered by their value
     *
     * @return A query with the new constraint
     */
    public fun orderByValue(): Query = Query(compat.orderByValue())

    /**
     * Creates a query in which child nodes are ordered by the values of the specified path.
     *
     * @param path The path to the child node to use for sorting
     * @return A query with the new constraint
     */
    public fun orderByChild(path: String): Query = Query(compat.orderByChild(path))

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: String, key: String? = null): Query = Query(compat.startAt(value, key))

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: Double, key: String? = null): Query = Query(compat.startAt(value, key))

    /**
     * Creates a query constrained to only return child nodes with a value greater than or equal to
     * the given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to start at, inclusive
     * @return A query with the new constraint
     */
    public fun startAt(value: Boolean, key: String? = null): Query = Query(compat.startAt(value, key))

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: String, key: String? = null): Query = Query(compat.endAt(value, key))

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: Double, key: String? = null): Query = Query(compat.endAt(value, key))

    /**
     * Creates a query constrained to only return child nodes with a value less than or equal to the
     * given value, using the given `orderBy` directive or priority as default.
     *
     * @param value The value to end at, inclusive
     * @return A query with the new constraint
     */
    public fun endAt(value: Boolean, key: String? = null): Query = Query(compat.endAt(value, key))

    /**
     * Creates a query with limit and anchor it to the start of the window.
     *
     * @param limit The maximum number of child nodes to return
     * @return A query with the new constraint
     */
    public fun limitToFirst(limit: Int): Query = Query(compat.limitToFirst(limit))

    /**
     * Creates a query with limit and anchor it to the end of the window.
     *
     * @param limit The maximum number of child nodes to return
     * @return A query with the new constraint
     */
    public fun limitToLast(limit: Int): Query = Query(compat.limitToLast(limit))

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: String, key: String? = null): Query = Query(compat.equalTo(value, key))

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: Double, key: String? = null): Query = Query(compat.equalTo(value, key))

    /**
     * Creates a query constrained to only return child nodes with the given value.
     *
     * @param value The value to query for
     * @return A query with the new constraint
     */
    public fun equalTo(value: Boolean, key: String? = null): Query = Query(compat.equalTo(value, key))

    override fun equals(other: Any?): Boolean = other is Query && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/**
 * A Firebase reference represents a particular location in your Database and can be used for
 * reading or writing data to that Database location.
 *
 * This class is the starting point for all Database operations. After you've initialized it with
 * a URL, you can use it to read data, write data, and to create new DatabaseReferences.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.database.DatabaseReference] this wraps.
 */
public class DatabaseReference internal constructor(override val compat: CompatDatabaseReference) : Query(compat) {
    /**
     * @return The last token in the location pointed to by this reference or null if this reference
     *     points to the database root
     */
    public val key: String? get() = compat.key

    /**
     * Create a reference to an auto-generated child location. The child key is generated client-side
     * and incorporates an estimate of the server's time for sorting purposes. Locations generated on
     * a single client will be sorted in the order that they are created, and will be sorted
     * approximately in order across all clients.
     *
     * @return A DatabaseReference pointing to the new location
     */
    public fun push(): DatabaseReference = DatabaseReference(compat.push())

    /**
     * Get a reference to location relative to this one
     *
     * @param path The relative path from this reference to the new one that should be created
     * @return A new DatabaseReference to the given path
     */
    public fun child(path: String): DatabaseReference = DatabaseReference(compat.child(path))

    /**
     * Provides access to disconnect operations at this location
     *
     * @return An object for managing disconnect operations at this location
     */
    public fun onDisconnect(): OnDisconnect = OnDisconnect(compat.onDisconnect())

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("setValue(value) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) {
        setValue(value) {
            this.encodeDefaults = encodeDefaults
        }
    }

    /**
     * Sets the value at this location, suspending until the server has acknowledged the write.
     *
     * While the client is offline the SDK applies the value locally and queues it, so this call, like the other
     * suspending writes, suspends until the connection is restored; wrap it in `withTimeout` to fail fast instead.
     *
     * @param value The value to write, encoded with [buildSettings]
     */
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
        compat.setValue(encodedValue).await()
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
        compat.updateChildren(encodedUpdate.toCompatMap()).await()
    }

    /**
     * Set the value at this location to 'null'
     *
     * @return The {@link Task} for this operation.
     */
    public suspend fun removeValue() {
        compat.removeValue().await()
    }

    /**
     * Run a transaction on the data at this location.
     *
     * @param handler An object to handle running the transaction
     */
    @OptIn(ExperimentalSerializationApi::class)
    public suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit = {}, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        compat.runTransaction(
            object : Transaction.Handler {
                override fun doTransaction(currentData: CompatMutableData): Transaction.Result {
                    val valueToReencode = currentData.nativeValue
                    // Value may be null initially, so only reencode if this is allowed
                    if (strategy.descriptor.isNullable || valueToReencode != null) {
                        currentData.nativeValue = reencodeTransformation(strategy, valueToReencode, buildSettings, transactionUpdate)
                    }
                    return transactionSuccess(currentData)
                }

                override fun onComplete(error: CompatDatabaseError?, committed: Boolean, currentData: CompatDataSnapshot?) {
                    if (error != null) {
                        deferred.completeExceptionally(error.toException())
                    } else {
                        deferred.complete(DataSnapshot(currentData!!))
                    }
                }
            },
        )
        return deferred.await()
    }
}

/**
 * A DataSnapshot instance contains data from a Firebase Database location. Any time you read
 * Database data, you receive the data as a DataSnapshot.
 *
 * They are efficiently-generated immutable copies of the data at a Firebase Database location. They
 * can't be modified and will never change. To modify data at a location, use a <br>
 * [DatabaseReference] reference (e.g. with [DatabaseReference.setValue]).
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.database.DataSnapshot] this wraps.
 */
public class DataSnapshot internal constructor(public val compat: CompatDataSnapshot) {
    /**
     * Returns true if the snapshot contains a non-null value.
     *
     * @return True if the snapshot contains a non-null value, otherwise false
     */
    public val exists: Boolean get() = compat.exists()

    /**
     * @return The key name for the source location of this snapshot or null if this snapshot points
     *     to the database root.
     */
    public val key: String? get() = compat.key

    /**
     * Used to obtain a reference to the source location for this snapshot.
     *
     * @return A DatabaseReference corresponding to the location that this snapshot came from
     */
    public val ref: DatabaseReference get() = DatabaseReference(compat.ref)

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public val value: Any? get() = compat.value

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public inline fun <reified T> value(): T = decode<T>(value = nativeValue)

    /**
     * [value] returns the data contained in this snapshot as native types.
     *
     * @return The data contained in this snapshot as native types or null if there is no data at this
     *     location.
     */
    public inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, nativeValue, buildSettings)

    /** The data as the platform SDK reads it, for the decoders. */
    @PublishedApi
    internal val nativeValue: Any? get() = compat.nativeValue

    public fun child(path: String): DataSnapshot = DataSnapshot(compat.child(path))

    /**
     * Indicates whether this snapshot has any children
     *
     * @return True if the snapshot has any children, otherwise false
     */
    public val hasChildren: Boolean get() = compat.hasChildren()

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
    public val children: Iterable<DataSnapshot> get() = compat.children.map { DataSnapshot(it) }

    override fun equals(other: Any?): Boolean = other is DataSnapshot && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/**
 * Exception that gets thrown when an operation on Firebase Database fails.
 */
public typealias DatabaseException = com.google.firebase.database.DatabaseException

/**
 * The OnDisconnect class is used to manage operations that will be run on the server when this
 * client disconnects. It can be used to add or remove data based on a client's connection status.
 * It is very useful in applications looking for 'presence' functionality.
 *
 * Instances of this class are obtained by calling [DatabaseReference.onDisconnect]
 * on a Firebase Database ref.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.database.OnDisconnect] this wraps.
 */
public class OnDisconnect internal constructor(public val compat: CompatOnDisconnect) {
    /**
     * Remove the value at this location when the client disconnects
     *
     * @return The {@link Task} for this operation.
     */
    public suspend fun removeValue() {
        compat.removeValue().await()
    }

    /**
     * Cancel any disconnect operations that are queued up at this location
     */
    public suspend fun cancel() {
        compat.cancel().await()
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
        compat.setValue(encodedValue).await()
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
        compat.updateChildren(encodedUpdate.toCompatMap()).await()
    }
}

/** The encoded children as the map the compatibility layer writes. */
internal expect fun EncodedObject.toCompatMap(): Map<String, Any?>

/** The data as the platform SDK reads it (a plain JS value on JS), which the decoders take. */
internal expect val CompatDataSnapshot.nativeValue: Any?

/** The transaction data as the platform SDK reads and writes it (a plain JS value on JS), which the decoders and encoders take. */
internal expect var CompatMutableData.nativeValue: Any?
