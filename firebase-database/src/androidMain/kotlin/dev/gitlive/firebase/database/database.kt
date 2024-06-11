/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("databaseAndroid")

package dev.gitlive.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Logger
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.internal.android
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.reencodeTransformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import java.util.WeakHashMap
import kotlin.time.Duration.Companion.seconds

suspend fun <T> Task<T>.awaitWhileOnline(database: FirebaseDatabase): T =
    merge(
        flow { emit(await()) },
        database
            .reference(".info/connected")
            .valueEvents
            .debounce(2.seconds)
            .filterNot { it.value<Boolean>() }
            .map<DataSnapshot, T> { throw DatabaseException("Database not connected", null) },
    )
        .first()

actual val Firebase.database
    by lazy { FirebaseDatabase.getInstance(com.google.firebase.database.FirebaseDatabase.getInstance()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase.getInstance(com.google.firebase.database.FirebaseDatabase.getInstance(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase.getInstance(com.google.firebase.database.FirebaseDatabase.getInstance(app.android))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase.getInstance(com.google.firebase.database.FirebaseDatabase.getInstance(app.android, url))

actual class FirebaseDatabase internal constructor(val android: com.google.firebase.database.FirebaseDatabase) {

    companion object {
        private val instances = WeakHashMap<com.google.firebase.database.FirebaseDatabase, FirebaseDatabase>()

        internal fun getInstance(
            android: com.google.firebase.database.FirebaseDatabase,
        ) = instances.getOrPut(android) { dev.gitlive.firebase.database.FirebaseDatabase(android) }
    }

    private var persistenceEnabled = true

    actual fun reference(path: String) =
        DatabaseReference(NativeDatabaseReference(android.getReference(path), persistenceEnabled))

    actual fun reference() =
        DatabaseReference(NativeDatabaseReference(android.reference, persistenceEnabled))

    actual fun setPersistenceEnabled(enabled: Boolean) {
        android.setPersistenceEnabled(enabled)
        persistenceEnabled = enabled
    }

    actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        android.setPersistenceCacheSizeBytes(cacheSizeInBytes)
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        android.setLogLevel(Logger.Level.DEBUG.takeIf { enabled } ?: Logger.Level.NONE)

    actual fun useEmulator(host: String, port: Int) =
        android.useEmulator(host, port)

    actual fun goOffline() = android.goOffline()

    actual fun goOnline() = android.goOnline()
}

internal actual open class NativeQuery(
    open val android: com.google.firebase.database.Query,
    val persistenceEnabled: Boolean,
)

actual open class Query internal actual constructor(
    nativeQuery: NativeQuery,
) {

    internal constructor(
        android: com.google.firebase.database.Query,
        persistenceEnabled: Boolean,
    ) : this(NativeQuery(android, persistenceEnabled))

    open val android: com.google.firebase.database.Query = nativeQuery.android
    val persistenceEnabled: Boolean = nativeQuery.persistenceEnabled

    actual fun orderByKey() = Query(android.orderByKey(), persistenceEnabled)

    actual fun orderByValue() = Query(android.orderByValue(), persistenceEnabled)

    actual fun orderByChild(path: String) = Query(android.orderByChild(path), persistenceEnabled)

    actual fun startAt(value: String, key: String?) = Query(android.startAt(value, key), persistenceEnabled)

    actual fun startAt(value: Double, key: String?) = Query(android.startAt(value, key), persistenceEnabled)

    actual fun startAt(value: Boolean, key: String?) = Query(android.startAt(value, key), persistenceEnabled)

    actual fun endAt(value: String, key: String?) = Query(android.endAt(value, key), persistenceEnabled)

    actual fun endAt(value: Double, key: String?) = Query(android.endAt(value, key), persistenceEnabled)

    actual fun endAt(value: Boolean, key: String?) = Query(android.endAt(value, key), persistenceEnabled)

    actual fun limitToFirst(limit: Int) = Query(android.limitToFirst(limit), persistenceEnabled)

    actual fun limitToLast(limit: Int) = Query(android.limitToLast(limit), persistenceEnabled)

    actual fun equalTo(value: String, key: String?) = Query(android.equalTo(value, key), persistenceEnabled)

    actual fun equalTo(value: Double, key: String?) = Query(android.equalTo(value, key), persistenceEnabled)

    actual fun equalTo(value: Boolean, key: String?) = Query(android.equalTo(value, key), persistenceEnabled)

    actual val valueEvents: Flow<DataSnapshot>
        get() = callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    trySendBlocking(DataSnapshot(snapshot, persistenceEnabled))
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    close(error.toException())
                }
            }
            android.addValueEventListener(listener)
            awaitClose { android.removeEventListener(listener) }
        }

    actual fun childEvents(vararg types: Type): Flow<ChildEvent> = callbackFlow {
        val listener = object : ChildEventListener {

            val moved by lazy { types.contains(Type.MOVED) }
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (moved) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.MOVED, previousChildName))
            }

            val changed by lazy { types.contains(Type.CHANGED) }
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (changed) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.CHANGED, previousChildName))
            }

            val added by lazy { types.contains(Type.ADDED) }
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (added) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.ADDED, previousChildName))
            }

            val removed by lazy { types.contains(Type.REMOVED) }
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {
                if (removed) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.REMOVED, null))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        android.addChildEventListener(listener)
        awaitClose { android.removeEventListener(listener) }
    }

    override fun toString() = android.toString()
}

@PublishedApi
internal actual class NativeDatabaseReference internal constructor(
    override val android: com.google.firebase.database.DatabaseReference,
    persistenceEnabled: Boolean,
) : NativeQuery(android, persistenceEnabled) {

    actual val key get() = android.key
    val database = FirebaseDatabase(android.database)

    actual fun child(path: String) = NativeDatabaseReference(android.child(path), persistenceEnabled)

    actual fun push() = NativeDatabaseReference(android.push(), persistenceEnabled)
    actual fun onDisconnect() = NativeOnDisconnect(android.onDisconnect(), persistenceEnabled, database)

    actual suspend fun setValueEncoded(encodedValue: Any?) = android.setValue(encodedValue)
        .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) =
        android.updateChildren(encodedUpdate.android)
            .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }

    actual suspend fun removeValue() = android.removeValue()
        .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    @OptIn(ExperimentalSerializationApi::class)
    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        android.runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val valueToReencode = currentData.value
                // Value may be null initially, so only reencode if this is allowed
                if (strategy.descriptor.isNullable || valueToReencode != null) {
                    currentData.value = reencodeTransformation(
                        strategy,
                        valueToReencode,
                        buildSettings,
                        transactionUpdate,
                    )
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: com.google.firebase.database.DataSnapshot?,
            ) {
                if (error != null) {
                    deferred.completeExceptionally(error.toException())
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, persistenceEnabled))
                }
            }
        })
        return deferred.await()
    }
}

val DatabaseReference.android get() = nativeReference.android

actual class DataSnapshot internal constructor(
    val android: com.google.firebase.database.DataSnapshot,
    private val persistenceEnabled: Boolean,
) {

    actual val exists get() = android.exists()

    actual val key get() = android.key

    actual val ref: DatabaseReference get() = DatabaseReference(NativeDatabaseReference(android.ref, persistenceEnabled))

    actual val value get() = android.value

    actual inline fun <reified T> value() =
        decode<T>(value = android.value)

    actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit) =
        decode(strategy, android.value, buildSettings)

    actual fun child(path: String) = DataSnapshot(android.child(path), persistenceEnabled)
    actual val hasChildren get() = android.hasChildren()
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it, persistenceEnabled) }
}

@PublishedApi
internal actual class NativeOnDisconnect internal constructor(
    val android: com.google.firebase.database.OnDisconnect,
    val persistenceEnabled: Boolean,
    val database: FirebaseDatabase,
) {

    actual suspend fun removeValue() = android.removeValue()
        .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun cancel() = android.cancel()
        .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun setValue(encodedValue: Any?) = android.setValue(encodedValue)
        .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) =
        android.updateChildren(encodedUpdate.android)
            .run { if (persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }
}

val OnDisconnect.android get() = native.android
val OnDisconnect.persistenceEnabled get() = native.persistenceEnabled
val OnDisconnect.database get() = native.database

actual typealias DatabaseException = com.google.firebase.database.DatabaseException
