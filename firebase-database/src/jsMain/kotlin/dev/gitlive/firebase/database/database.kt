/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.externals.CancelCallback
import dev.gitlive.firebase.database.externals.ChangeSnapshotCallback
import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.database.externals.child
import dev.gitlive.firebase.database.externals.connectDatabaseEmulator
import dev.gitlive.firebase.database.externals.enableLogging
import dev.gitlive.firebase.database.externals.getDatabase
import dev.gitlive.firebase.database.externals.onChildAdded
import dev.gitlive.firebase.database.externals.onChildChanged
import dev.gitlive.firebase.database.externals.onChildMoved
import dev.gitlive.firebase.database.externals.onChildRemoved
import dev.gitlive.firebase.database.externals.onDisconnect
import dev.gitlive.firebase.database.externals.onValue
import dev.gitlive.firebase.database.externals.push
import dev.gitlive.firebase.database.externals.query
import dev.gitlive.firebase.database.externals.ref
import dev.gitlive.firebase.database.externals.remove
import dev.gitlive.firebase.database.externals.set
import dev.gitlive.firebase.database.externals.update
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.js
import dev.gitlive.firebase.internal.reencodeTransformation
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlin.js.Promise
import kotlin.js.json
import dev.gitlive.firebase.database.externals.DataSnapshot as JsDataSnapshot
import dev.gitlive.firebase.database.externals.DatabaseReference as JsDatabaseReference
import dev.gitlive.firebase.database.externals.OnDisconnect as JsOnDisconnect
import dev.gitlive.firebase.database.externals.Query as JsQuery
import dev.gitlive.firebase.database.externals.endAt as jsEndAt
import dev.gitlive.firebase.database.externals.equalTo as jsEqualTo
import dev.gitlive.firebase.database.externals.goOffline as jsGoOffline
import dev.gitlive.firebase.database.externals.goOnline as jsGoOnline
import dev.gitlive.firebase.database.externals.limitToFirst as jsLimitToFirst
import dev.gitlive.firebase.database.externals.limitToLast as jsLimitToLast
import dev.gitlive.firebase.database.externals.orderByChild as jsOrderByChild
import dev.gitlive.firebase.database.externals.orderByKey as jsOrderByKey
import dev.gitlive.firebase.database.externals.orderByValue as jsOrderByValue
import dev.gitlive.firebase.database.externals.runTransaction as jsRunTransaction
import dev.gitlive.firebase.database.externals.startAt as jsStartAt

actual val Firebase.database
    get() = rethrow { FirebaseDatabase(getDatabase()) }

actual fun Firebase.database(app: FirebaseApp) =
    rethrow { FirebaseDatabase(getDatabase(app = app.js)) }

actual fun Firebase.database(url: String) =
    rethrow { FirebaseDatabase(getDatabase(url = url)) }

actual fun Firebase.database(app: FirebaseApp, url: String) =
    rethrow { FirebaseDatabase(getDatabase(app = app.js, url = url)) }

actual class FirebaseDatabase internal constructor(val js: Database) {

    actual fun reference(path: String) = rethrow { DatabaseReference(NativeDatabaseReference(ref(js, path), js)) }
    actual fun reference() = rethrow { DatabaseReference(NativeDatabaseReference(ref(js), js)) }
    actual fun setPersistenceEnabled(enabled: Boolean) {}
    actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {}
    actual fun setLoggingEnabled(enabled: Boolean) = rethrow { enableLogging(enabled) }
    actual fun useEmulator(host: String, port: Int) = rethrow { connectDatabaseEmulator(js, host, port) }

    actual fun goOffline() = rethrow { jsGoOffline(js) }

    actual fun goOnline() = rethrow { jsGoOnline(js) }
}

internal actual open class NativeQuery(
    open val js: JsQuery,
    val database: Database,
)

actual open class Query internal actual constructor(
    nativeQuery: NativeQuery,
) {

    internal constructor(js: JsQuery, database: Database) : this(NativeQuery(js, database))

    open val js: JsQuery = nativeQuery.js
    val database: Database = nativeQuery.database

    actual fun orderByKey() = Query(query(js, jsOrderByKey()), database)
    actual fun orderByValue() = Query(query(js, jsOrderByValue()), database)
    actual fun orderByChild(path: String) = Query(query(js, jsOrderByChild(path)), database)

    actual val valueEvents
        get() = callbackFlow<DataSnapshot> {
            val unsubscribe = rethrow {
                onValue(
                    query = js,
                    callback = { trySend(DataSnapshot(it, database)) },
                    cancelCallback = { close(DatabaseException(it)).run { } },
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
                            DataSnapshot(snapshot, database),
                            type,
                            previousChildName,
                        ),
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

    actual fun startAt(value: String, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)), database)

    actual fun startAt(value: Double, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)), database)

    actual fun startAt(value: Boolean, key: String?) = Query(query(js, jsStartAt(value, key ?: undefined)), database)

    actual fun endAt(value: String, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)), database)

    actual fun endAt(value: Double, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)), database)

    actual fun endAt(value: Boolean, key: String?) = Query(query(js, jsEndAt(value, key ?: undefined)), database)

    actual fun limitToFirst(limit: Int) = Query(query(js, jsLimitToFirst(limit)), database)

    actual fun limitToLast(limit: Int) = Query(query(js, jsLimitToLast(limit)), database)

    actual fun equalTo(value: String, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)), database)

    actual fun equalTo(value: Double, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)), database)

    actual fun equalTo(value: Boolean, key: String?) = Query(query(js, jsEqualTo(value, key ?: undefined)), database)

    override fun toString() = js.toString()
}

@PublishedApi
internal actual class NativeDatabaseReference internal constructor(
    override val js: JsDatabaseReference,
    database: Database,
) : NativeQuery(js, database) {

    actual val key get() = rethrow { js.key }
    actual fun push() = rethrow { NativeDatabaseReference(push(js), database) }
    actual fun child(path: String) = rethrow { NativeDatabaseReference(child(js, path), database) }

    actual fun onDisconnect() = rethrow { NativeOnDisconnect(onDisconnect(js), database) }

    actual suspend fun removeValue() = rethrow { remove(js).awaitWhileOnline(database) }

    actual suspend fun setValueEncoded(encodedValue: Any?) = rethrow {
        set(js, encodedValue).awaitWhileOnline(database)
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) =
        rethrow { update(js, encodedUpdate.js).awaitWhileOnline(database) }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        return DataSnapshot(
            jsRunTransaction<Any?>(js, transactionUpdate = { currentData ->
                reencodeTransformation(strategy, currentData ?: json(), buildSettings, transactionUpdate)
            }).awaitWhileOnline(database).snapshot,
            database,
        )
    }
}

actual class DataSnapshot internal constructor(
    val js: JsDataSnapshot,
    val database: Database,
) {
    actual val value get(): Any? {
        check(!hasChildren) { "DataSnapshot.value can only be used for primitive values (snapshots without children)" }
        return js.`val`()
    }

    actual inline fun <reified T> value() =
        rethrow { decode<T>(value = js.`val`()) }

    actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit) =
        rethrow { decode(strategy, js.`val`(), buildSettings) }

    actual val exists get() = rethrow { js.exists() }
    actual val key get() = rethrow { js.key }
    actual fun child(path: String) = DataSnapshot(js.child(path), database)
    actual val hasChildren get() = js.hasChildren()
    actual val children: Iterable<DataSnapshot> = rethrow {
        ArrayList<DataSnapshot>(js.size).also {
            js.forEach { snapshot ->
                it.add(DataSnapshot(snapshot, database))
                false // don't cancel enumeration
            }
        }
    }
    actual val ref: DatabaseReference
        get() = DatabaseReference(NativeDatabaseReference(js.ref, database))
}

@PublishedApi
internal actual class NativeOnDisconnect internal constructor(
    val js: JsOnDisconnect,
    val database: Database,
) {

    actual suspend fun removeValue() = rethrow { js.remove().awaitWhileOnline(database) }
    actual suspend fun cancel() = rethrow { js.cancel().awaitWhileOnline(database) }

    actual suspend fun setValue(encodedValue: Any?) =
        rethrow { js.set(encodedValue).awaitWhileOnline(database) }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) =
        rethrow { js.update(encodedUpdate.js).awaitWhileOnline(database) }
}

val OnDisconnect.js get() = native.js
val OnDisconnect.database get() = native.database

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

suspend fun <T> Promise<T>.awaitWhileOnline(database: Database): T = coroutineScope {
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
