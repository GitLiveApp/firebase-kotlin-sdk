package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.common.firebase
import dev.teamhub.firebase.common.fromJson
import dev.teamhub.firebase.common.toJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.DynamicObjectParser

actual val Firebase.database
    get() = rethrow { dev.teamhub.firebase.common.database; FirebaseDatabase(dev.teamhub.firebase.common.firebase.database()) }

actual fun Firebase.database(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.common.database; FirebaseDatabase(dev.teamhub.firebase.common.firebase.database(app.js)) }

actual fun Firebase.database(url: String) =
    rethrow { dev.teamhub.firebase.common.database; FirebaseDatabase(dev.teamhub.firebase.common.firebase.app().database(url)) }

actual fun Firebase.database(app: FirebaseApp, url: String) =
    rethrow { dev.teamhub.firebase.common.database; FirebaseDatabase(app.js.database(url)) }

actual class FirebaseDatabase internal constructor(val js: firebase.database.Database) {
    actual fun reference(path: String) = rethrow { DatabaseReference(js.ref(path)) }
    actual fun setPersistenceEnabled(enabled: Boolean) {}
    actual fun setLoggingEnabled(enabled: Boolean) = rethrow { firebase.database.enableLogging(enabled) }
}

actual class DatabaseReference internal constructor(val js: firebase.database.Reference) {

    actual fun push() = rethrow { DatabaseReference(js.push()) }
    actual fun onDisconnect() = rethrow { OnDisconnect(js.onDisconnect()) }
    actual suspend fun setValue(value: Any?) = rethrow { js.set(toJson(value)).await() }
    actual suspend fun updateChildren(update: Map<String, Any?>) = rethrow { js.update(toJson(update)).await() }
    actual suspend fun removeValue() = rethrow { js.remove().await() }

    actual val valueEvents get() = callbackFlow {
        val listener = js.on(
            "value",
            { offer(DataSnapshot(it)) },
            { close(DatabaseException(it)).run { Unit } }
        )
        awaitClose { js.off("value", listener) }
    }

    actual val singleValueEvent: Deferred<DataSnapshot> get() = CompletableDeferred<DataSnapshot>().also {
        js.once(
            "value",
            { snapshot -> it.complete(DataSnapshot(snapshot)) },
            { error -> it.completeExceptionally(DatabaseException(error)).run { Unit } }
        )
    }
}

actual class DataSnapshot internal constructor(val js: firebase.database.DataSnapshot) {

    actual inline fun <reified T : Any> value(): T? =
        rethrow { DynamicObjectParser().parse(js.`val`()) }

    actual inline fun <reified T : Any> values(): List<T>? =
        rethrow { js.`val`().unsafeCast<Array<*>>().map { DynamicObjectParser().parse<T>(it) } }

    actual val exists get() = rethrow { js.exists() }
    actual fun child(path: String) = DataSnapshot(js.child(path))

    actual val children: Iterable<DataSnapshot> = rethrow {
        ArrayList<DataSnapshot>(js.numChildren()).also {
            js.forEach { snapshot -> it.add(DataSnapshot(snapshot)) }
        }
    }

}

actual class OnDisconnect internal constructor(val js: firebase.database.OnDisconnect) {
    actual suspend fun removeValue() = rethrow { js.remove().await() }
    actual suspend fun cancel() =  rethrow { js.cancel().await() }
    actual suspend fun setValue(value: Any?) =  rethrow { js.set(value).await() }
    actual suspend fun updateChildren(update: Map<String, Any?>) =  rethrow { js.update(toJson(update)).await() }
}

actual typealias ServerValue = firebase.database.ServerValue

actual class DatabaseException(error: dynamic) :
    RuntimeException("${error.code}: ${error.message}", error.unsafeCast<Throwable>())

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.database.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw DatabaseException(e)
    }
}
