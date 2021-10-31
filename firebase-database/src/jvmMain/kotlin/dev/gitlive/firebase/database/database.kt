/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import com.google.api.core.ApiFuture
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.gitlive.firebase.*
import dev.gitlive.firebase.database.ChildEvent.Type
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    dev.gitlive.firebase.encode(value, shouldEncodeElementDefault, ServerValue.TIMESTAMP)

internal fun <T> encode(strategy: SerializationStrategy<T> , value: T, shouldEncodeElementDefault: Boolean): Any? =
    dev.gitlive.firebase.encode(strategy, value, shouldEncodeElementDefault, ServerValue.TIMESTAMP)

suspend fun <T> ApiFuture<T>.awaitWhileOnline(): T = coroutineScope {
    val notConnected = Firebase.database
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select {
        asDeferred().onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }
}

actual val Firebase.database
        by lazy { FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.jvm))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase(com.google.firebase.database.FirebaseDatabase.getInstance(app.jvm, url))

actual class FirebaseDatabase internal constructor(val jvm: com.google.firebase.database.FirebaseDatabase) {

    private var persistenceEnabled = true

    actual fun reference(path: String) =
        DatabaseReference(jvm.getReference(path), persistenceEnabled)

    actual fun setPersistenceEnabled(enabled: Boolean) =
        jvm.setPersistenceEnabled(enabled).also { persistenceEnabled = enabled }

    actual fun setLoggingEnabled(enabled: Boolean) = Unit

    actual fun useEmulator(host: String, port: Int) = Unit
}

actual open class Query internal constructor(
    open val jvm: com.google.firebase.database.Query,
    val persistenceEnabled: Boolean
) {
    actual fun orderByKey() = Query(jvm.orderByKey(), persistenceEnabled)

    actual fun orderByValue() = Query(jvm.orderByValue(), persistenceEnabled)

    actual fun orderByChild(path: String) = Query(jvm.orderByChild(path), persistenceEnabled)

    actual fun startAt(value: String, key: String?) = Query(jvm.startAt(value, key), persistenceEnabled)

    actual fun startAt(value: Double, key: String?) = Query(jvm.startAt(value, key), persistenceEnabled)

    actual fun startAt(value: Boolean, key: String?) = Query(jvm.startAt(value, key), persistenceEnabled)

    actual fun endAt(value: String, key: String?) = Query(jvm.endAt(value, key), persistenceEnabled)

    actual fun endAt(value: Double, key: String?) = Query(jvm.endAt(value, key), persistenceEnabled)

    actual fun endAt(value: Boolean, key: String?) = Query(jvm.endAt(value, key), persistenceEnabled)

    actual fun limitToFirst(limit: Int) = Query(jvm.limitToFirst(limit), persistenceEnabled)

    actual fun limitToLast(limit: Int) = Query(jvm.limitToLast(limit), persistenceEnabled)

    actual fun equalTo(value: String, key: String?) = Query(jvm.equalTo(value, key), persistenceEnabled)

    actual fun equalTo(value: Double, key: String?) = Query(jvm.equalTo(value, key), persistenceEnabled)

    actual fun equalTo(value: Boolean, key: String?) = Query(jvm.equalTo(value, key), persistenceEnabled)

    actual val valueEvents: Flow<DataSnapshot>
        get() = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                safeOffer(DataSnapshot(snapshot))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        jvm.addValueEventListener(listener)
        awaitClose { jvm.removeEventListener(listener) }
    }

    actual fun childEvents(vararg types: Type): Flow<ChildEvent> = callbackFlow {
        val listener = object : ChildEventListener {

            val moved by lazy { types.contains(Type.MOVED) }
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if(moved) safeOffer(ChildEvent(DataSnapshot(snapshot), Type.MOVED, previousChildName))
            }

            val changed by lazy { types.contains(Type.CHANGED) }
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if(changed) safeOffer(ChildEvent(DataSnapshot(snapshot), Type.CHANGED, previousChildName))
            }

            val added by lazy { types.contains(Type.ADDED) }
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if(added) safeOffer(ChildEvent(DataSnapshot(snapshot), Type.ADDED, previousChildName))
            }

            val removed by lazy { types.contains(Type.REMOVED) }
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {
                if(removed) safeOffer(ChildEvent(DataSnapshot(snapshot), Type.REMOVED, null))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        jvm.addChildEventListener(listener)
        awaitClose { jvm.removeEventListener(listener) }
    }

    override fun toString() = jvm.toString()
}

actual class DatabaseReference internal constructor(
    override val jvm: com.google.firebase.database.DatabaseReference,
    persistenceEnabled: Boolean
): Query(jvm, persistenceEnabled) {

    actual val key get() = jvm.key

    actual fun child(path: String) = DatabaseReference(jvm.child(path), persistenceEnabled)

    actual fun push() = DatabaseReference(jvm.push(), persistenceEnabled)
    actual fun onDisconnect() = OnDisconnect(jvm.onDisconnect(), persistenceEnabled)

    actual suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) = jvm.setValueAsync(encode(value, encodeDefaults))
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        jvm.setValueAsync(encode(strategy, value, encodeDefaults))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        jvm.updateChildrenAsync(encode(update, encodeDefaults) as Map<String, Any?>)
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    actual suspend fun removeValue() = jvm.removeValueAsync()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val android: com.google.firebase.database.DataSnapshot) {

    actual val exists get() = android.exists()

    actual val key get() = android.key

    actual inline fun <reified T> value() =
        decode<T>(value = android.value)

    actual fun <T> value(strategy: DeserializationStrategy<T>) =
        decode(strategy, android.value)

    actual fun child(path: String) = DataSnapshot(android.child(path))
    actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it) }
}

actual class OnDisconnect internal constructor(
    val jvm: com.google.firebase.database.OnDisconnect,
    val persistenceEnabled: Boolean
) {

    actual suspend fun removeValue() = jvm.removeValueAsync()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend fun cancel() = jvm.cancelAsync()
        .run { if(persistenceEnabled) await() else awaitWhileOnline() }
        .run { Unit }

    actual suspend inline fun <reified T> setValue(value: T, encodeDefaults: Boolean) =
        jvm.setValueAsync(encode(value, encodeDefaults))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) =
        jvm.setValueAsync(encode(strategy, value, encodeDefaults))
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit}

    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) =
        jvm.updateChildrenAsync(update.mapValues { (_, it) -> encode(it, encodeDefaults) })
            .run { if(persistenceEnabled) await() else awaitWhileOnline() }
            .run { Unit }
}

actual typealias DatabaseException = com.google.firebase.database.DatabaseException

