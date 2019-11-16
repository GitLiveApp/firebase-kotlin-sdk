package dev.teamhub.firebase.database

import com.google.firebase.database.Logger
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Mapper

actual val Firebase.database
    get() = FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance())

actual fun Firebase.database(url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.android))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.android, url))

actual class FirebaseDatabase internal constructor(val android: com.google.firebase.database.FirebaseDatabase) {
    actual fun reference(path: String) = DatabaseReference(android.getReference(path))

    actual fun setPersistenceEnabled(enabled: Boolean) = android.setPersistenceEnabled(enabled)

    actual fun setLoggingEnabled(enabled: Boolean) =
        android.setLogLevel(Logger.Level.DEBUG.takeIf { enabled } ?: Logger.Level.NONE)
}

actual class DatabaseReference internal constructor(val android: com.google.firebase.database.DatabaseReference) {

    actual fun push() = DatabaseReference(android.push())
    actual fun onDisconnect() = OnDisconnect(android.onDisconnect())

    actual val valueEvents get() = callbackFlow {
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

    actual val singleValueEvent: Deferred<DataSnapshot> get() = CompletableDeferred<DataSnapshot>().also {
        android.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                it.complete(DataSnapshot(snapshot))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                it.completeExceptionally(error.toException())
            }
        })
    }

    actual suspend fun setValue(value: Any?) = android.setValue(value).await().run { Unit }

    actual suspend fun updateChildren(update: Map<String, Any?>) = android.updateChildren(update).await().run { Unit }

    actual suspend fun removeValue() = android.removeValue().await().run { Unit }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val android: com.google.firebase.database.DataSnapshot) {
    actual val exists get() = android.exists()

    actual inline fun <reified T: Any> value() = when(T::class) {
        Boolean::class -> android.value as T?
        String::class -> android.value as T?
        Long::class -> android.value as T?
        else -> android.value?.let { Mapper.unmap<T>(it as Map<String, Any>) }
    }

    actual inline fun <reified T: Any> values() = when(T::class) {
        Boolean::class -> android.value as List<T>?
        String::class -> android.value as List<T>?
        Long::class -> android.value as List<T>?
        else -> (android.value as List<Any>?)?.map { Mapper.unmap<T>(it as Map<String, Any>) }
    }

    actual fun child(path: String) = DataSnapshot(android.child(path))
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it) }
}

actual class OnDisconnect internal constructor(val android: com.google.firebase.database.OnDisconnect) {
    actual suspend fun removeValue() = android.removeValue().await().run { Unit }
    actual suspend fun cancel() = android.cancel().await().run { Unit }
    actual suspend fun setValue(value: Any?) = android.setValue(value).await().run { Unit }
    actual suspend fun updateChildren(update: Map<String, Any?>) = android.updateChildren(update).await().run { Unit }
}

actual typealias DatabaseException = com.google.firebase.database.DatabaseException

actual object ServerValue {
  actual val TIMESTAMP = ServerValue.TIMESTAMP
}

