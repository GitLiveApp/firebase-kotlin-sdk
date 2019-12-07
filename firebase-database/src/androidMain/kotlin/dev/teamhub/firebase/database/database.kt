package dev.teamhub.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.database.Logger
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.decode
import dev.teamhub.firebase.encode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

suspend fun <T> Task<T>.awaitWhileOnline(): T = coroutineScope {
    val notConnected = Firebase.database
        .reference(".info/connected")
        .snapshots
        .filter { !it.value<Boolean>() }
        .conflate()
        .produceIn(this)

    select<T> {
        asDeferred().onAwait { it }
        notConnected.onReceive { throw DatabaseException("Database not connected") }
    }
}

actual val Firebase.database
    get() = FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance())

actual fun Firebase.database(url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.android))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.android, url))

actual class FirebaseDatabase internal constructor(val android: com.google.firebase.database.FirebaseDatabase) {

    private var persistenceEnabled = true

    actual fun reference(path: String) =
        DatabaseReference(android.getReference(path), persistenceEnabled)

    actual fun setPersistenceEnabled(enabled: Boolean) =
        android.setPersistenceEnabled(enabled).also { persistenceEnabled = enabled }

    actual fun setLoggingEnabled(enabled: Boolean) =
        android.setLogLevel(Logger.Level.DEBUG.takeIf { enabled } ?: Logger.Level.NONE)
}

actual class DatabaseReference internal constructor(
    val android: com.google.firebase.database.DatabaseReference,
    val persistenceEnabled: Boolean
) {

    actual fun push() = DatabaseReference(android.push(), persistenceEnabled)
    actual fun onDisconnect() = OnDisconnect(android.onDisconnect(), persistenceEnabled)

    actual val snapshots get() = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                offer(DataSnapshot(snapshot))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        android.addValueEventListener(listener)
        awaitClose { android.removeEventListener(listener) }
    }

    actual suspend fun setValue(value: Any?) = android.setValue(encode(value))
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T) =
        android.setValue(encode(strategy, value))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    actual suspend fun updateChildren(update: Map<String, Any?>) =
        android.updateChildren(update.mapValues { (_, it) -> encode(value = it) })
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    actual suspend fun removeValue() = android.removeValue()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val android: com.google.firebase.database.DataSnapshot) {

    actual val exists get() = android.exists()

    actual inline fun <reified T> value() =
        decode<T>(value = android.value)

    actual inline fun <reified T> value(strategy: DeserializationStrategy<T>) =
        decode(strategy, android.value)

    actual fun child(path: String) = DataSnapshot(android.child(path))
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it) }
}

actual class OnDisconnect internal constructor(
    val android: com.google.firebase.database.OnDisconnect,
    val persistenceEnabled: Boolean
) {

    actual suspend fun removeValue() = android.removeValue()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend fun cancel() = android.cancel()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend inline fun <reified T : Any> setValue(value: T) =
        android.setValue(encode(value = value))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    actual suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T) =
        android.setValue(encode(strategy, value))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit}

    actual suspend fun updateChildren(update: Map<String, Any?>) =
        android.updateChildren(update.mapValues { (_, it) -> encode(value = it) })
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }
}

actual typealias DatabaseException = com.google.firebase.database.DatabaseException

actual object ServerValue {
  actual val TIMESTAMP = ServerValue.TIMESTAMP
}

