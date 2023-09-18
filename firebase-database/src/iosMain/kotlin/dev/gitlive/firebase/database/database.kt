/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.*
import cocoapods.FirebaseDatabase.FIRDataEventType.*
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.ChildEvent.Type.*
import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError
import platform.Foundation.allObjects
import platform.darwin.dispatch_queue_t
import kotlin.collections.component1
import kotlin.collections.component2

actual val Firebase.database
        by lazy { FirebaseDatabase(FIRDatabase.database()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase(FIRDatabase.databaseWithURL(url))

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp)
)

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp, url)
)

actual class FirebaseDatabase internal constructor(val ios: FIRDatabase) {

    actual data class Settings(
        actual val persistenceEnabled: Boolean = false,
        actual val persistenceCacheSizeBytes: Long? = null,
        val callbackQueue: dispatch_queue_t = null
    ) {

        actual companion object {
            actual fun createSettings(persistenceEnabled: Boolean, persistenceCacheSizeBytes:  Long?) = Settings(persistenceEnabled, persistenceCacheSizeBytes)
        }
    }

    actual fun reference(path: String) =
        DatabaseReference(ios.referenceWithPath(path), ios.persistenceEnabled)

    actual fun reference() =
        DatabaseReference(ios.reference(), ios.persistenceEnabled)

    actual fun setSettings(settings: Settings) {
        ios.persistenceEnabled = settings.persistenceEnabled
        settings.persistenceCacheSizeBytes?.let { ios.setPersistenceCacheSizeBytes(it.toULong()) }
        settings.callbackQueue?.let { ios.callbackQueue = it }
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        FIRDatabase.setLoggingEnabled(enabled)

    actual fun useEmulator(host: String, port: Int) =
        ios.useEmulatorWithHost(host, port.toLong())
}

fun Type.toEventType() = when(this) {
    ADDED -> FIRDataEventTypeChildAdded
    CHANGED -> FIRDataEventTypeChildChanged
    MOVED -> FIRDataEventTypeChildMoved
    REMOVED -> FIRDataEventTypeChildRemoved
}

actual open class Query internal constructor(
    open val ios: FIRDatabaseQuery,
    val persistenceEnabled: Boolean
) {
    actual fun orderByKey() = Query(ios.queryOrderedByKey(), persistenceEnabled)

    actual fun orderByValue() = Query(ios.queryOrderedByValue(), persistenceEnabled)

    actual fun orderByChild(path: String) = Query(ios.queryOrderedByChild(path), persistenceEnabled)

    actual fun startAt(value: String, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Double, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Boolean, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: String, key: String?) = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: Double, key: String?) = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun endAt(value: Boolean, key: String?) = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    actual fun limitToFirst(limit: Int) = Query(ios.queryLimitedToFirst(limit.toULong()), persistenceEnabled)

    actual fun limitToLast(limit: Int) = Query(ios.queryLimitedToLast(limit.toULong()), persistenceEnabled)

    actual fun equalTo(value: String, key: String?) = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    actual fun equalTo(value: Double, key: String?) = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    actual fun equalTo(value: Boolean, key: String?) = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    actual val valueEvents get() = callbackFlow<DataSnapshot> {
        val handle = ios.observeEventType(
            FIRDataEventTypeValue,
            withBlock = { snapShot ->
                trySend(DataSnapshot(snapShot!!, persistenceEnabled))
            }
        ) { close(DatabaseException(it.toString(), null)) }
        awaitClose { ios.removeObserverWithHandle(handle) }
    }

    actual fun childEvents(vararg types: Type) = callbackFlow<ChildEvent> {
        val handles = types.map { type ->
            ios.observeEventType(
                type.toEventType(),
                andPreviousSiblingKeyWithBlock = { snapShot, key ->
                    trySend(ChildEvent(DataSnapshot(snapShot!!, persistenceEnabled), type, key))
                }
            ) { close(DatabaseException(it.toString(), null)) }
        }
        awaitClose {
            handles.forEach { ios.removeObserverWithHandle(it) }
        }
    }

    override fun toString() = ios.toString()
}

actual class DatabaseReference internal constructor(
    override val ios: FIRDatabaseReference,
    persistenceEnabled: Boolean
): Query(ios, persistenceEnabled) {

    actual val key get() = ios.key

    actual fun child(path: String) = DatabaseReference(ios.child(path), persistenceEnabled)

    actual fun push() = DatabaseReference(ios.childByAutoId(), persistenceEnabled)
    actual fun onDisconnect() = OnDisconnect(ios, persistenceEnabled)

    actual suspend inline fun <reified T> setValue(value: T?, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { setValue(encode(value, encodeSettings), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { setValue(encode(strategy, value, encodeSettings), it) }
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { updateChildValues(encode(update, encodeSettings) as Map<Any?, *>, it) }
    }

    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { removeValueWithCompletionBlock(it) }
    }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, decodeSettings: DecodeSettings, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        ios.runTransactionBlock(
            block = { firMutableData ->
                firMutableData?.value = firMutableData?.value?.let {
                    transactionUpdate(decode(strategy, it, decodeSettings))
                }
                FIRTransactionResult.successWithValue(firMutableData!!)
            },
            andCompletionBlock = { error, _, snapshot ->
                if (error != null) {
                    deferred.completeExceptionally(DatabaseException(error.toString(), null))
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, persistenceEnabled))
                }
            },
            withLocalEvents = false
        )
        return deferred.await()
    }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(
    val ios: FIRDataSnapshot,
    private val persistenceEnabled: Boolean
) {

    actual val exists get() = ios.exists()

    actual val key: String? get() = ios.key

    actual val ref: DatabaseReference get() = DatabaseReference(ios.ref, persistenceEnabled)

    actual val value get() = ios.value

    actual inline fun <reified T> value() =
        decode<T>(value = ios.value)

    actual fun <T> value(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings) =
        decode(strategy, ios.value, decodeSettings)

    actual fun child(path: String) = DataSnapshot(ios.childSnapshotForPath(path), persistenceEnabled)
    actual val hasChildren get() = ios.hasChildren()
    actual val children: Iterable<DataSnapshot> get() = ios.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot, persistenceEnabled) }
}

actual class OnDisconnect internal constructor(
    val ios: FIRDatabaseReference,
    val persistenceEnabled: Boolean
) {
    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { onDisconnectRemoveValueWithCompletionBlock(it) }
    }

    actual suspend fun cancel() {
        ios.await(persistenceEnabled) { cancelDisconnectOperationsWithCompletionBlock(it) }
    }

    actual suspend inline fun <reified T> setValue(value: T, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encode(value, encodeSettings), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encode(strategy, value, encodeSettings), it) }
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>, encodeSettings: EncodeSettings) {
        ios.await(persistenceEnabled) { onDisconnectUpdateChildValues(update.mapValues { (_, it) -> encode(it, encodeSettings) } as Map<Any?, *>, it) }
    }
}

actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)

private suspend inline fun <T, reified R> T.awaitResult(whileOnline: Boolean, function: T.(callback: (NSError?, R?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    val callback = { error: NSError?, result: R? ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    function(callback)
    return job.run { if(whileOnline) awaitWhileOnline() else await() } as R
}

suspend inline fun <T> T.await(whileOnline: Boolean, function: T.(callback: (NSError?, FIRDatabaseReference?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    val callback = { error: NSError?, _: FIRDatabaseReference? ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    function(callback)
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
