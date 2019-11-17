package dev.teamhub.firebase.database

import com.google.firebase.database.Logger
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.*

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

@Serializable
data class Value<T>(val value: T)

actual class DatabaseReference internal constructor(val android: com.google.firebase.database.DatabaseReference) {

    actual fun push() = DatabaseReference(android.push())
    actual fun onDisconnect() = OnDisconnect(android.onDisconnect())

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

    actual suspend inline fun <reified T : Any> setValue(value: T) =
        android.setValue(Mapper.map(Value(value))["value"]).await().run { Unit }

    actual suspend inline fun <reified T> setValue(value: T, strategy: SerializationStrategy<T>) =
        object : KSerializer<T>, SerializationStrategy<T> by strategy {
            override fun deserialize(decoder: Decoder) = error("not supported")
        }.let {
            android.setValue(Mapper.map(Value.serializer(it), Value(value))["value"]).await()
        }.run { Unit }

    actual suspend fun updateChildren(update: Map<String, Any?>) =
        android.updateChildren(Mapper.map(update)).await().run { Unit }

    actual suspend fun removeValue() = android.removeValue().await().run { Unit }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val android: com.google.firebase.database.DataSnapshot) {
    actual val exists get() = android.exists()

    actual inline fun <reified T: Any> value() =
        Mapper.unmapNullable<Value<T>>(mapOf("value" to android.value)).value

    actual inline fun <reified T> value(strategy: DeserializationStrategy<T>) =
        object : KSerializer<T>, DeserializationStrategy<T> by strategy {
            override fun serialize(encoder: Encoder, obj: T) = error("not supported")
        }.let {
            Mapper.unmapNullable(Value.serializer(it), mapOf("value" to android.value)).value
        }

    actual fun child(path: String) = DataSnapshot(android.child(path))
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it) }
}

actual class OnDisconnect internal constructor(val android: com.google.firebase.database.OnDisconnect) {

    actual suspend fun removeValue() = android.removeValue().await().run { Unit }
    actual suspend fun cancel() = android.cancel().await().run { Unit }

    actual suspend inline fun <reified T : Any> setValue(value: T) =
        android.setValue(Mapper.map(Value(value))["value"]).await().run { Unit }

    actual suspend inline fun <reified T> setValue(value: T, strategy: SerializationStrategy<T>) =
        object : KSerializer<T>, SerializationStrategy<T> by strategy {
            override fun deserialize(decoder: Decoder) = error("not supported")
        }.let {
            android.setValue(Mapper.map(Value.serializer(it), Value(value))["value"]).await()
        }.run { Unit }

    actual suspend fun updateChildren(update: Map<String, Any?>) =
        android.updateChildren(Mapper.map(update)).await().run { Unit }
}

actual typealias DatabaseException = com.google.firebase.database.DatabaseException

actual object ServerValue {
  actual val TIMESTAMP = ServerValue.TIMESTAMP
}

