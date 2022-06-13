/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.*
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.externals.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.js.Promise
import kotlin.js.json
import dev.gitlive.firebase.externals.database.DataSnapshot as JsDataSnapshot
import dev.gitlive.firebase.externals.database.DatabaseReference as JsDatabaseReference
import dev.gitlive.firebase.externals.database.OnDisconnect as JsOnDisconnect
import dev.gitlive.firebase.externals.database.Query as JsQuery
import dev.gitlive.firebase.externals.database.endAt as jsEndAt
import dev.gitlive.firebase.externals.database.equalTo as jsEqualTo
import dev.gitlive.firebase.externals.database.limitToFirst as jsLimitToFirst
import dev.gitlive.firebase.externals.database.limitToLast as jsLimitToLast
import dev.gitlive.firebase.externals.database.orderByChild as jsOrderByChild
import dev.gitlive.firebase.externals.database.orderByKey as jsOrderByKey
import dev.gitlive.firebase.externals.database.orderByValue as jsOrderByValue
import dev.gitlive.firebase.externals.database.startAt as jsStartAt

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    encode(value, shouldEncodeElementDefault, serverTimestamp())

internal fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? =
    encode(strategy, value, shouldEncodeElementDefault, serverTimestamp())


actual val Firebase.database
    get() = rethrow { FirebaseDatabase(getDatabase()) }

actual fun Firebase.database(app: FirebaseApp) =
    rethrow { FirebaseDatabase(getDatabase(app = app.js)) }

actual fun Firebase.database(url: String) =
    rethrow { FirebaseDatabase(getDatabase(url = url)) }

actual fun Firebase.database(app: FirebaseApp, url: String) =
    rethrow { FirebaseDatabase(getDatabase(app = app.js, url = url)) }

actual class FirebaseDatabase internal constructor(val js: Database) {
    actual fun reference(path: String) = rethrow { DatabaseReference(ref(js, path)) }
    actual fun reference() = rethrow { DatabaseReference(ref(js)) }
    actual fun setPersistenceEnabled(enabled: Boolean) {}
    actual fun setLoggingEnabled(enabled: Boolean) = rethrow { enableLogging(enabled) }
    actual fun useEmulator(host: String, port: Int) = rethrow { connectDatabaseEmulator(js, host, port) }
}

actual open class Query internal constructor(open val js: JsQuery) {

    actual fun orderByKey() = Query(query(js, jsOrderByKey()))
    actual fun orderByValue() = Query(query(js, jsOrderByValue()))
    actual fun orderByChild(path: String) = Query(query(js, jsOrderByChild(path)))

    actual val valueEvents
        get() = callbackFlow<DataSnapshot> {
            val unsubscribe = rethrow {
                onValue(
                    query = js,
                    callback = { trySend(DataSnapshot(it)) },
                    cancelCallback = { close(DatabaseException(it)).run { } }
                )
            }
            awaitClose { rethrow { unsubscribe() } }
        }

    actual fun childEvents(vararg types: ChildEvent.Type) = callbackFlow<ChildEvent> {
        val unsubscribes = rethrow {
            types.map { type ->
                val callback: ChangeSnapshotCallback = { snapshot, previousChildName ->
                    trySend(
                        ChildEvent(
                            DataSnapshot(snapshot),
                            type,
                            previousChildName
                        )
                    )
                }

                val cancelCallback: CancelCallback = {
                    close(DatabaseException(it)).run { }
                }

                when (type) {
                    ChildEvent.Type.ADDED -> onChildAdded(js, callback, cancelCallback)
                    ChildEvent.Type.CHANGED -> onChildChanged(js, callback, cancelCallback)
                    ChildEvent.Type.MOVED -> onChildMoved(js, callback, cancelCallback)
                    ChildEvent.Type.REMOVED -> onChildRemoved(js, callback, cancelCallback)
                }
            }
        }
        awaitClose { rethrow { unsubscribes.forEach { it.invoke() } } }
    }

    actual fun startAt(value: String, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)))

    actual fun startAt(value: Double, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)))

    actual fun startAt(value: Boolean, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)))

    actual fun endAt(value: String, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)))

    actual fun endAt(value: Double, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)))

    actual fun endAt(value: Boolean, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)))

    actual fun limitToFirst(limit: Int) = Query(query(js, jsLimitToFirst(limit)))

    actual fun limitToLast(limit: Int) = Query(query(js, jsLimitToLast(limit)))

    actual fun equalTo(value: String, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)))

    actual fun equalTo(value: Double, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)))

    actual fun equalTo(value: Boolean, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)))

    override fun toString() = js.toString()

}

actual class DatabaseReference internal constructor(override val js: JsDatabaseReference) : Query(js) {

    actual val key get() = rethrow { js.key }
    actual fun push() = rethrow { DatabaseReference(push(js)) }
    actual fun child(path: String) = rethrow { DatabaseReference(child(js, path)) }

    actual fun onDisconnect() = rethrow { OnDisconnect(onDisconnect(js)) }

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        rethrow { update(js, encode(update, encodeDefaults) ?: json()).awaitWhileOnline() }

    actual suspend fun removeValue() = rethrow { remove(js).awaitWhileOnline() }

    actual suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) = rethrow {
        set(js, encode(value, encodeDefaults)).awaitWhileOnline()
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        rethrow { set(js, encode(strategy, value, encodeDefaults)).awaitWhileOnline() }
}

actual class DataSnapshot internal constructor(val js: JsDataSnapshot) {

    actual inline fun <reified T> value() =
        rethrow { decode<T>(value = js.`val`()) }

    actual fun <T> value(strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.`val`()) }

    actual val exists get() = rethrow { js.exists() }
    actual val key get() = rethrow { js.key }
    actual fun child(path: String) = DataSnapshot(js.child(path))

    actual val children: Iterable<DataSnapshot> = rethrow {
        ArrayList<DataSnapshot>(js.size).also {
            js.forEach { snapshot -> it.add(DataSnapshot(snapshot)); false /* don't cancel enumeration */ }
        }
    }

}

actual class OnDisconnect internal constructor(val js: JsOnDisconnect) {

    actual suspend fun removeValue() = rethrow { js.remove().awaitWhileOnline() }
    actual suspend fun cancel() = rethrow { js.cancel().awaitWhileOnline() }

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        rethrow { js.update(encode(update, encodeDefaults) ?: json()).awaitWhileOnline() }

    actual suspend inline fun <reified T> setValue(value: T, encodeDefaults: Boolean) =
        rethrow { js.set(encode(value, encodeDefaults)).awaitWhileOnline() }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        rethrow { js.set(encode(strategy, value, encodeDefaults)).awaitWhileOnline() }
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
    } catch (e: dynamic) {
        throw DatabaseException(e)
    }
}

suspend fun <T> Promise<T>.awaitWhileOnline(): T = coroutineScope {

    val notConnected = Firebase.database
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select<T> {
        this@awaitWhileOnline.asDeferred().onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }

}
