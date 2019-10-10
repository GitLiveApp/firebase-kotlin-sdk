package dev.teamhub.firebase.database

import dev.teamhub.firebase.common.fromJson
import dev.teamhub.firebase.common.toJson
import kotlinx.coroutines.await
import kotlin.reflect.KClass

actual fun getFirebaseDatabase() = rethrow { firebase.database() }

actual typealias FirebaseDatabase = firebase.database.Database

actual typealias DatabaseReference = firebase.database.Reference

actual typealias DataSnapshot = firebase.database.DataSnapshot

actual typealias OnDisconnect = firebase.database.OnDisconnect

actual interface ValueEventListener {
    actual fun onDataChange(data: DataSnapshot)
    actual fun onCancelled(error: DatabaseError)
}

@Suppress("UNCHECKED_CAST")
actual fun <T : Any> DataSnapshot.getValue(valueType: KClass<T>): T? = rethrow { fromJson(`val`(), valueType)  as T? }
actual fun DataSnapshot.exists(): Boolean = rethrow { exists() }
actual fun DataSnapshot.getValue(): Any? = rethrow { fromJson(`val`()) }

actual class DatabaseError(internal val error: Error)

actual val TIMESTAMP: Map<String, String>
    get() = firebase.database.ServerValue.TIMESTAMP

actual suspend fun DatabaseReference.awaitSetValue(value: Any?) = rethrow { set(toJson(value)).await() }
actual suspend fun DatabaseReference.awaitUpdateChildren(update: Map<String, Any?>) = rethrow { update(toJson(update)).await() }

actual suspend fun OnDisconnect.awaitRemoveValue() = rethrow { remove().await() }
actual suspend fun OnDisconnect.awaitCancel() =  rethrow { cancel().await() }
actual suspend fun OnDisconnect.awaitSetValue(value: Any?) =  rethrow { set(value).await() }
actual suspend fun OnDisconnect.awaitUpdateChildren(update: Map<String, Any?>) =  rethrow { update(toJson(update)).await() }

actual class DatabaseException(code: String?, message: String?) : RuntimeException("$code: $message")

actual suspend fun DatabaseReference.awaitRemoveValue() =  rethrow { remove().await() }

actual enum class LoggerLevel {
    DEBUG, INFO, WARN, ERROR, NONE
}

actual fun FirebaseDatabase.getReference(path: String) = ref(path)

actual fun FirebaseDatabase.setPersistenceEnabled(enabled: Boolean) {
}

actual fun FirebaseDatabase.setLogLevel(logLevel: LoggerLevel) = rethrow { firebase.database.enableLogging(logLevel != LoggerLevel.NONE) }

actual fun DatabaseReference.push() = rethrow { push() as DatabaseReference }

actual fun DatabaseReference.onDisconnect() = rethrow { onDisconnect() }

actual fun DatabaseReference.addValueEventListener(listener: ValueEventListener) = rethrow {
    on("value", { listener.onDataChange(it) }, { listener.onCancelled(DatabaseError(it)) })
        .let { listener.asDynamic().callback = it }
        .run { listener }
}

actual fun DatabaseReference.removeEventListener(listener: ValueEventListener) = rethrow { off("value", listener.asDynamic().callback) }


actual fun DatabaseError.toException() = DatabaseException(error.asDynamic().code as String?, error.message)

actual val DataSnapshot.children: Iterable<DataSnapshot>
    get() = rethrow {
        val children = ArrayList<DataSnapshot>(numChildren())
        forEach {
            children.add( it.`val`() as DataSnapshot )
        }
        return children
    }

actual fun DatabaseReference.addListenerForSingleValueEvent(listener: ValueEventListener) = rethrow {
    once("value", { listener.onDataChange(it) }, { listener.onCancelled(DatabaseError(it)) })
        .let { listener.asDynamic().callback = it }
}

actual fun DataSnapshot.child(path: String) = rethrow { asDynamic().child(path).unsafeCast<DataSnapshot>() }

actual annotation class Exclude actual constructor()
actual annotation class IgnoreExtraProperties actual constructor()

private inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.database.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw DatabaseException(e.asDynamic().code as String?, e.message)
    }
}
