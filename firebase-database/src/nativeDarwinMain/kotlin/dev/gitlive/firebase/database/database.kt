/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.*
import cocoapods.FirebaseDatabase.FIRDataEventType.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.ChildEvent.Type.*
import dev.gitlive.firebase.decode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.*
import kotlin.collections.component1
import kotlin.collections.component2

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    dev.gitlive.firebase.encode(value, shouldEncodeElementDefault, FIRServerValue.timestamp())

internal fun <T> encode(strategy: SerializationStrategy<T> , value: T, shouldEncodeElementDefault: Boolean): Any? =
    dev.gitlive.firebase.encode(strategy, value, shouldEncodeElementDefault, FIRServerValue.timestamp())

actual val Firebase.database
        by lazy { FirebaseDatabase(FIRDatabase.database()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase(FIRDatabase.databaseWithURL(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase(FIRDatabase.databaseForApp(app.native))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase(FIRDatabase.databaseForApp(app.native, url))

actual class FirebaseDatabase internal constructor(val native: FIRDatabase) {

    actual fun reference(path: String) =
        DatabaseReference(native.referenceWithPath(path), native.persistenceEnabled)

    actual fun reference() =
        DatabaseReference(native.reference(), native.persistenceEnabled)

    actual fun setPersistenceEnabled(enabled: Boolean) {
        native.persistenceEnabled = enabled
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        FIRDatabase.setLoggingEnabled(enabled)

    actual fun useEmulator(host: String, port: Int) =
        native.useEmulatorWithHost(host, port.toLong())
}

fun Type.toEventType() = when(this) {
    ADDED -> FIRDataEventTypeChildAdded
    CHANGED -> FIRDataEventTypeChildChanged
    MOVED -> FIRDataEventTypeChildMoved
    REMOVED -> FIRDataEventTypeChildRemoved
}

actual open class Query internal constructor(
    open val native: FIRDatabaseQuery,
    val persistenceEnabled: Boolean
) {
    actual fun orderByKey() = Query(native.queryOrderedByKey(), persistenceEnabled)

    actual fun orderByValue() = Query(native.queryOrderedByValue(), persistenceEnabled)

    actual fun orderByChild(path: String) = Query(native.queryOrderedByChild(path), persistenceEnabled)

    actual fun startAt(value: String, key: String?) = Query(native.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Double, key: String?) = Query(native.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Boolean, key: String?) = Query(native.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: String, key: String?) = Query(native.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: Double, key: String?) = Query(native.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: Boolean, key: String?) = Query(native.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun limitToFirst(limit: Int) = Query(native.queryLimitedToFirst(limit.toULong()), persistenceEnabled)

    actual fun limitToLast(limit: Int) = Query(native.queryLimitedToLast(limit.toULong()), persistenceEnabled)

    actual fun equalTo(value: String, key: String?) = Query(native.queryEqualToValue(value, key), persistenceEnabled)

    actual fun equalTo(value: Double, key: String?) = Query(native.queryEqualToValue(value, key), persistenceEnabled)

    actual fun equalTo(value: Boolean, key: String?) = Query(native.queryEqualToValue(value, key), persistenceEnabled)

    actual val valueEvents get() = callbackFlow<DataSnapshot> {
        val handle = native.observeEventType(
            FIRDataEventTypeValue,
            withBlock = { snapShot ->
                trySend(DataSnapshot(snapShot!!))
            }
        ) { close(DatabaseException(it.toString(), null)) }
        awaitClose { native.removeObserverWithHandle(handle) }
    }

    actual fun childEvents(vararg types: Type) = callbackFlow<ChildEvent> {
        val handles = types.map { type ->
            native.observeEventType(
                type.toEventType(),
                andPreviousSiblingKeyWithBlock = { snapShot, key ->
                    trySend(ChildEvent(DataSnapshot(snapShot!!), type, key))
                }
            ) { close(DatabaseException(it.toString(), null)) }
        }
        awaitClose {
            handles.forEach { native.removeObserverWithHandle(it) }
        }
    }

    override fun toString() = native.toString()
}

actual class DatabaseReference internal constructor(
    override val native: FIRDatabaseReference,
    persistenceEnabled: Boolean
): Query(native, persistenceEnabled) {

    actual val key get() = native.key

    actual fun child(path: String) = DatabaseReference(native.child(path), persistenceEnabled)

    actual fun push() = DatabaseReference(native.childByAutoId(), persistenceEnabled)
    actual fun onDisconnect() = OnDisconnect(native, persistenceEnabled)

    actual suspend inline fun <reified T> setValue(value: T?, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { setValue(encode(value, encodeDefaults), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { setValue(encode(strategy, value, encodeDefaults), it) }
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { updateChildValues(encode(update, encodeDefaults) as Map<Any?, *>, it) }
    }

    actual suspend fun removeValue() {
        native.await(persistenceEnabled) { removeValueWithCompletionBlock(it) }
    }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val native: FIRDataSnapshot) {

    actual val exists get() = native.exists()

    actual val key: String? get() = native.key

    actual inline fun <reified T> value() =
        decode<T>(value = native.value)

    actual fun <T> value(strategy: DeserializationStrategy<T>) =
        decode(strategy, native.value)

    actual fun child(path: String) = DataSnapshot(native.childSnapshotForPath(path))
    actual val children: Iterable<DataSnapshot> get() = native.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot) }
}

actual class OnDisconnect internal constructor(
    val native: FIRDatabaseReference,
    val persistenceEnabled: Boolean
) {
    actual suspend fun removeValue() {
        native.await(persistenceEnabled) { onDisconnectRemoveValueWithCompletionBlock(it) }
    }

    actual suspend fun cancel() {
        native.await(persistenceEnabled) { cancelDisconnectOperationsWithCompletionBlock(it) }
    }

    actual suspend inline fun <reified T> setValue(value: T, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { onDisconnectSetValue(encode(value, encodeDefaults), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { onDisconnectSetValue(encode(strategy, value, encodeDefaults), it) }
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeDefaults: Boolean) {
        native.await(persistenceEnabled) { onDisconnectUpdateChildValues(update.mapValues { (_, it) -> encode(it, encodeDefaults) } as Map<Any?, *>, it) }
    }
}

actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)

private suspend inline fun <T, reified R> T.awaitResult(whileOnline: Boolean, function: T.(callback: (NSError?, R?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { error, result ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    return job.run { if(whileOnline) awaitWhileOnline() else await() } as R
}

suspend inline fun <T> T.await(whileOnline: Boolean, function: T.(callback: (NSError?, FIRDatabaseReference?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error, _ ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    job.run { if(whileOnline) awaitWhileOnline() else await() }
}

@FlowPreview
suspend fun <T> CompletableDeferred<T>.awaitWhileOnline(): T = coroutineScope {

    val notConnected = Firebase.database
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select<T> {
        onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }
}
