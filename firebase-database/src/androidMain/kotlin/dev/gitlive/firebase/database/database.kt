/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("AndroidDatabase")
package dev.gitlive.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Logger
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import java.util.*
import kotlin.time.Duration.Companion.seconds

suspend fun <T> Task<T>.awaitWhileOnline(database: FirebaseDatabase): T =
    merge(
        flow { emit(await()) },
        database
            .reference(".info/connected")
            .valueEvents
            .debounce(2.seconds)
            .filter { !it.value<Boolean>() }
            .map<DataSnapshot, T> { throw DatabaseException("Database not connected", null) }
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
            android: com.google.firebase.database.FirebaseDatabase
        ) = instances.getOrPut(android) { dev.gitlive.firebase.database.FirebaseDatabase(android) }
    }

    actual data class Settings(
        actual val persistenceEnabled: Boolean = false,
        actual val persistenceCacheSizeBytes: Long? = null,
    ) {

        actual companion object {
            actual fun createSettings(persistenceEnabled: Boolean, persistenceCacheSizeBytes:  Long?) = Settings(persistenceEnabled, persistenceCacheSizeBytes)
        }
    }

    private var persistenceEnabled = true

    actual fun reference(path: String) =
        DatabaseReference(android.getReference(path), persistenceEnabled)

    actual fun reference() =
        DatabaseReference(android.reference, persistenceEnabled)

    actual fun setSettings(settings: Settings) {
        android.setPersistenceEnabled(settings.persistenceEnabled)
        persistenceEnabled = settings.persistenceEnabled
        settings.persistenceCacheSizeBytes?.let { android.setPersistenceCacheSizeBytes(it) }
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        android.setLogLevel(Logger.Level.DEBUG.takeIf { enabled } ?: Logger.Level.NONE)

    actual fun useEmulator(host: String, port: Int) =
        android.useEmulator(host, port)
}

actual open class Query internal constructor(
    open val android: com.google.firebase.database.Query,
    val persistenceEnabled: Boolean
) {
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
                if(moved) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.MOVED, previousChildName))
            }

            val changed by lazy { types.contains(Type.CHANGED) }
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if(changed) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.CHANGED, previousChildName))
            }

            val added by lazy { types.contains(Type.ADDED) }
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if(added) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.ADDED, previousChildName))
            }

            val removed by lazy { types.contains(Type.REMOVED) }
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {
                if(removed) trySend(ChildEvent(DataSnapshot(snapshot, persistenceEnabled), Type.REMOVED, null))
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

actual class DatabaseReference internal constructor(
    override val android: com.google.firebase.database.DatabaseReference,
    persistenceEnabled: Boolean
): Query(android, persistenceEnabled) {

    actual val key get() = android.key
    val database = FirebaseDatabase(android.database)

    actual fun child(path: String) = DatabaseReference(android.child(path), persistenceEnabled)

    actual fun push() = DatabaseReference(android.push(), persistenceEnabled)
    actual fun onDisconnect() = OnDisconnect(android.onDisconnect(), persistenceEnabled, database)

    actual suspend inline fun <reified T> setValue(value: T?, encodeSettings: EncodeSettings) = android.setValue(encode(value, encodeSettings))
        .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings) =
        android.setValue(encode(strategy, value, encodeSettings))
            .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings) =
        android.updateChildren(encode(update, encodeSettings) as Map<String, Any?>)
            .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }

    actual suspend fun removeValue() = android.removeValue()
        .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, decodeSettings: DecodeSettings, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        android.runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = currentData.value?.let {
                    transactionUpdate(decode(strategy, it, decodeSettings))
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: com.google.firebase.database.DataSnapshot?
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
@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(
    val android: com.google.firebase.database.DataSnapshot,
    private val persistenceEnabled: Boolean
) {

    actual val exists get() = android.exists()

    actual val key get() = android.key

    actual val ref: DatabaseReference get() = DatabaseReference(android.ref, persistenceEnabled)

    actual val value get() = android.value

    actual inline fun <reified T> value() =
        decode<T>(value = android.value)

    actual fun <T> value(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings) =
        decode(strategy, android.value, decodeSettings)

    actual fun child(path: String) = DataSnapshot(android.child(path), persistenceEnabled)
    actual val hasChildren get() = android.hasChildren()
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it, persistenceEnabled) }
}

actual class OnDisconnect internal constructor(
    val android: com.google.firebase.database.OnDisconnect,
    val persistenceEnabled: Boolean,
    val database: FirebaseDatabase,
) {

    actual suspend fun removeValue() = android.removeValue()
        .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend fun cancel() = android.cancel()
        .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
        .run { Unit }

    actual suspend inline fun <reified T> setValue(value: T, encodeSettings: EncodeSettings) =
        android.setValue(encode(value, encodeSettings))
            .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings) =
        android.setValue(encode(strategy, value, encodeSettings))
            .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit}

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings) =
        android.updateChildren(update.mapValues { (_, it) -> encode(it, encodeSettings) })
            .run { if(persistenceEnabled) await() else awaitWhileOnline(database) }
            .run { Unit }
}

actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)
