/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildAdded
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildChanged
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildMoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildRemoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeValue
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseQuery
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseDatabase.FIRTransactionResult
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.ChildEvent.Type.ADDED
import dev.gitlive.firebase.database.ChildEvent.Type.CHANGED
import dev.gitlive.firebase.database.ChildEvent.Type.MOVED
import dev.gitlive.firebase.database.ChildEvent.Type.REMOVED
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.ios
import dev.gitlive.firebase.internal.reencodeTransformation
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
import platform.Foundation.NSError
import platform.Foundation.allObjects
import kotlin.collections.component1
import kotlin.collections.component2

actual val Firebase.database
    by lazy { FirebaseDatabase(FIRDatabase.database()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase(FIRDatabase.databaseWithURL(url))

actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp),
)

actual fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp, url),
)

actual class FirebaseDatabase internal constructor(val ios: FIRDatabase) {

    actual fun reference(path: String) =
        DatabaseReference(NativeDatabaseReference(ios.referenceWithPath(path), ios.persistenceEnabled))

    actual fun reference() =
        DatabaseReference(NativeDatabaseReference(ios.reference(), ios.persistenceEnabled))

    actual fun setPersistenceEnabled(enabled: Boolean) {
        ios.persistenceEnabled = enabled
    }

    actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        ios.setPersistenceCacheSizeBytes(cacheSizeInBytes.toULong())
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        FIRDatabase.setLoggingEnabled(enabled)

    actual fun useEmulator(host: String, port: Int) =
        ios.useEmulatorWithHost(host, port.toLong())

    actual fun goOffline() = ios.goOffline()

    actual fun goOnline() = ios.goOnline()
}

fun Type.toEventType() = when (this) {
    ADDED -> FIRDataEventTypeChildAdded
    CHANGED -> FIRDataEventTypeChildChanged
    MOVED -> FIRDataEventTypeChildMoved
    REMOVED -> FIRDataEventTypeChildRemoved
}

internal actual open class NativeQuery(
    open val ios: FIRDatabaseQuery,
    val persistenceEnabled: Boolean,
)

actual open class Query internal actual constructor(
    nativeQuery: NativeQuery,
) {

    internal constructor(ios: FIRDatabaseQuery, persistenceEnabled: Boolean) : this(NativeQuery(ios, persistenceEnabled))

    open val ios: FIRDatabaseQuery = nativeQuery.ios
    val persistenceEnabled: Boolean = nativeQuery.persistenceEnabled

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
            },
        ) { close(DatabaseException(it.toString(), null)) }
        awaitClose { ios.removeObserverWithHandle(handle) }
    }

    actual fun childEvents(vararg types: Type) = callbackFlow<ChildEvent> {
        val handles = types.map { type ->
            ios.observeEventType(
                type.toEventType(),
                andPreviousSiblingKeyWithBlock = { snapShot, key ->
                    trySend(ChildEvent(DataSnapshot(snapShot!!, persistenceEnabled), type, key))
                },
            ) { close(DatabaseException(it.toString(), null)) }
        }
        awaitClose {
            handles.forEach { ios.removeObserverWithHandle(it) }
        }
    }

    override fun toString() = ios.toString()
}

@PublishedApi
internal actual class NativeDatabaseReference internal constructor(
    override val ios: FIRDatabaseReference,
    persistenceEnabled: Boolean,
) : NativeQuery(ios, persistenceEnabled) {

    actual val key get() = ios.key

    actual fun child(path: String) = NativeDatabaseReference(ios.child(path), persistenceEnabled)

    actual fun push() = NativeDatabaseReference(ios.childByAutoId(), persistenceEnabled)
    actual fun onDisconnect() = NativeOnDisconnect(ios, persistenceEnabled)

    actual suspend fun setValueEncoded(encodedValue: Any?) {
        ios.await(persistenceEnabled) { setValue(encodedValue, it) }
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        ios.await(persistenceEnabled) { updateChildValues(encodedUpdate.ios, it) }
    }

    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { removeValueWithCompletionBlock(it) }
    }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        ios.runTransactionBlock(
            block = { firMutableData ->
                firMutableData?.value = reencodeTransformation(strategy, firMutableData?.value, buildSettings, transactionUpdate)
                FIRTransactionResult.successWithValue(firMutableData!!)
            },
            andCompletionBlock = { error, _, snapshot ->
                if (error != null) {
                    deferred.completeExceptionally(DatabaseException(error.toString(), null))
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, persistenceEnabled))
                }
            },
            withLocalEvents = false,
        )
        return deferred.await()
    }
}

val DatabaseReference.ios: FIRDatabaseReference get() = nativeReference.ios

actual class DataSnapshot internal constructor(
    val ios: FIRDataSnapshot,
    private val persistenceEnabled: Boolean,
) {

    actual val exists get() = ios.exists()

    actual val key: String? get() = ios.key

    actual val ref: DatabaseReference get() = DatabaseReference(NativeDatabaseReference(ios.ref, persistenceEnabled))

    actual val value get() = ios.value

    actual inline fun <reified T> value() =
        decode<T>(value = ios.value)

    actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit) =
        decode(strategy, ios.value, buildSettings)

    actual fun child(path: String) = DataSnapshot(ios.childSnapshotForPath(path), persistenceEnabled)
    actual val hasChildren get() = ios.hasChildren()
    actual val children: Iterable<DataSnapshot> get() = ios.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot, persistenceEnabled) }
}

@PublishedApi
internal actual class NativeOnDisconnect internal constructor(
    val ios: FIRDatabaseReference,
    val persistenceEnabled: Boolean,
) {
    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { onDisconnectRemoveValueWithCompletionBlock(it) }
    }

    actual suspend fun cancel() {
        ios.await(persistenceEnabled) { cancelDisconnectOperationsWithCompletionBlock(it) }
    }

    actual suspend fun setValue(encodedValue: Any?) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encodedValue, it) }
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        ios.await(persistenceEnabled) { onDisconnectUpdateChildValues(encodedUpdate.ios, it) }
    }
}

val OnDisconnect.ios: FIRDatabaseReference get() = native.ios
val OnDisconnect.persistenceEnabled get() = native.persistenceEnabled

actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)

private suspend inline fun <T, reified R> T.awaitResult(whileOnline: Boolean, function: T.(callback: (NSError?, R?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { error, result ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    return job.run { if (whileOnline) awaitWhileOnline() else await() } as R
}

suspend inline fun <T> T.await(whileOnline: Boolean, function: T.(callback: (NSError?, FIRDatabaseReference?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error, _ ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    job.run { if (whileOnline) awaitWhileOnline() else await() }
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
