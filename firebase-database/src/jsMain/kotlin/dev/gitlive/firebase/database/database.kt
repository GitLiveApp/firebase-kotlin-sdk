/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlin.js.Promise

actual val Firebase.database
    get() = rethrow { dev.gitlive.firebase.database; FirebaseDatabase(firebase.database()) }

actual fun Firebase.database(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.database; FirebaseDatabase(firebase.database(app.js)) }

actual fun Firebase.database(url: String) =
    rethrow { dev.gitlive.firebase.database; FirebaseDatabase(firebase.app().database(url)) }

actual fun Firebase.database(app: FirebaseApp, url: String) =
    rethrow { dev.gitlive.firebase.database; FirebaseDatabase(app.js.database(url)) }

actual class FirebaseDatabase internal constructor(val js: firebase.database.Database) {
    actual fun reference(path: String) = rethrow { DatabaseReference(js.ref(path), js) }
    actual fun reference() = rethrow { DatabaseReference(js.ref(), js) }
    actual fun setPersistenceEnabled(enabled: Boolean) {}
    actual fun setLoggingEnabled(enabled: Boolean) = rethrow { firebase.database.enableLogging(enabled) }
    actual fun useEmulator(host: String, port: Int) = rethrow { js.useEmulator(host, port) }
}

actual open class Query internal constructor(
    open val js: firebase.database.Query,
    val database: firebase.database.Database
) {

    actual fun orderByKey() = Query(js.orderByKey(), database)
    actual fun orderByValue() = Query(js.orderByValue(), database)
    actual fun orderByChild(path: String) = Query(js.orderByChild(path), database)

    actual val valueEvents get() = callbackFlow<DataSnapshot> {
        val listener = rethrow {
            js.on(
                "value",
                { it, _ -> trySend(DataSnapshot(it, database)) },
                { close(DatabaseException(it)).run { Unit } }
            )
        }
        awaitClose { rethrow { js.off("value", listener) } }
    }

    actual fun childEvents(vararg types: ChildEvent.Type) = callbackFlow<ChildEvent> {
        val listeners = rethrow {
            types.map { type ->
                "child_${type.name.lowercase()}".let { eventType ->
                    eventType to js.on(
                        eventType,
                        { snapshot, previousChildName ->
                            trySend(
                                ChildEvent(
                                    DataSnapshot(snapshot, database),
                                    type,
                                    previousChildName
                                )
                            )
                        },
                        { close(DatabaseException(it)).run { Unit } }
                    )
                }
            }
        }
        awaitClose { rethrow { listeners.forEach { (eventType, listener) -> js.off(eventType, listener) } } }
    }

    actual fun startAt(value: String, key: String?) = Query(js.startAt(value, key ?: undefined), database)

    actual fun startAt(value: Double, key: String?) = Query(js.startAt(value, key ?: undefined), database)

    actual fun startAt(value: Boolean, key: String?) = Query(js.startAt(value, key ?: undefined), database)

    actual fun endAt(value: String, key: String?) = Query(js.endAt(value, key ?: undefined), database)

    actual fun endAt(value: Double, key: String?) = Query(js.endAt(value, key ?: undefined), database)

    actual fun endAt(value: Boolean, key: String?) = Query(js.endAt(value, key ?: undefined), database)

    actual fun limitToFirst(limit: Int) = Query(js.limitToFirst(limit), database)

    actual fun limitToLast(limit: Int) = Query(js.limitToLast(limit), database)

    actual fun equalTo(value: String, key: String?) = Query(js.equalTo(value, key ?: undefined), database)

    actual fun equalTo(value: Double, key: String?) = Query(js.equalTo(value, key ?: undefined), database)

    actual fun equalTo(value: Boolean, key: String?) = Query(js.equalTo(value, key ?: undefined), database)

    override fun toString() = js.toString()

}

actual class DatabaseReference internal constructor(
    override val js: firebase.database.Reference,
    database: firebase.database.Database
): Query(js, database) {

    actual val key get() = rethrow { js.key }
    actual fun push() = rethrow { DatabaseReference(js.push(), database) }
    actual fun child(path: String) = rethrow { DatabaseReference(js.child(path), database) }

    actual fun onDisconnect() = rethrow { OnDisconnect(js.onDisconnect(), database) }

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        rethrow { js.update(encode(update, encodeDefaults)).awaitWhileOnline(database) }

    actual suspend fun removeValue() = rethrow { js.remove().awaitWhileOnline(database) }

    actual suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) = rethrow {
        js.set(encode(value, encodeDefaults)).awaitWhileOnline(database)
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        rethrow { js.set(encode(strategy, value, encodeDefaults)).awaitWhileOnline(database) }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        js.transaction(
            transactionUpdate,
            { error, _, snapshot ->
                if (error != null) {
                    deferred.completeExceptionally(error)
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, database))
                }
            },
            applyLocally = false
        )
        return deferred.await()
    }

}

actual class DataSnapshot internal constructor(
    val js: firebase.database.DataSnapshot,
    val database: firebase.database.Database
) {

    actual val value get(): Any? {
        check(!hasChildren) { "DataSnapshot.value can only be used for primitive values (snapshots without children)" }
        return js.`val`()
    }

    actual inline fun <reified T> value() =
        rethrow { decode<T>(value = js.`val`()) }

    actual fun <T> value(strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.`val`()) }

    actual val exists get() = rethrow { js.exists() }
    actual val key get() = rethrow { js.key }
    actual fun child(path: String) = DataSnapshot(js.child(path), database)
    actual val hasChildren get() = js.hasChildren()
    actual val children: Iterable<DataSnapshot> = rethrow {
        ArrayList<DataSnapshot>(js.numChildren()).also {
            js.forEach { snapshot -> it.add(DataSnapshot(snapshot, database)) }
        }
    }
    actual val ref: DatabaseReference
        get() = DatabaseReference(js.ref, database)

}

actual class OnDisconnect internal constructor(
    val js: firebase.database.OnDisconnect,
    val database: firebase.database.Database
) {

    actual suspend fun removeValue() = rethrow { js.remove().awaitWhileOnline(database) }
    actual suspend fun cancel() =  rethrow { js.cancel().awaitWhileOnline(database) }

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        rethrow { js.update(encode(update, encodeDefaults)).awaitWhileOnline(database) }

    actual suspend inline fun <reified T> setValue(value: T, encodeDefaults: Boolean) =
        rethrow { js.set(encode(value, encodeDefaults)).awaitWhileOnline(database) }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        rethrow { js.set(encode(strategy, value, encodeDefaults)).awaitWhileOnline(database) }
}

actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(error: dynamic) : this("${error.code ?: "UNKNOWN"}: ${error.message}", error.unsafeCast<Throwable>())
}

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.database.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw DatabaseException(e)
    }
}

suspend fun <T> Promise<T>.awaitWhileOnline(database: firebase.database.Database): T = coroutineScope {

    val notConnected = FirebaseDatabase(database)
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select<T> {
        this@awaitWhileOnline.asDeferred().onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }

}
