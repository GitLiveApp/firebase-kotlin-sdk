package dev.teamhub.firebase.database

import cocoapods.FirebaseDatabase.*
import cocoapods.FirebaseDatabase.FIRDataEventType.*
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.database.ChildEvent.Type
import dev.teamhub.firebase.database.ChildEvent.Type.*
import dev.teamhub.firebase.decode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.*
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map
import kotlin.collections.mapValues

fun encode(value: Any?) =
    dev.teamhub.firebase.encode(value, FIRServerValue.timestamp())
fun <T> encode(strategy: SerializationStrategy<T> , value: T): Any? =
    dev.teamhub.firebase.encode(strategy, value, FIRServerValue.timestamp())

actual val Firebase.database
        by lazy { FirebaseDatabase(FIRDatabase.database()) }

actual fun Firebase.database(url: String) =
    FirebaseDatabase(FIRDatabase.databaseWithURL(url))

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase(FIRDatabase.databaseForApp(app.ios))

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase(FIRDatabase.databaseForApp(app.ios, url))

actual class FirebaseDatabase internal constructor(val ios: FIRDatabase) {

    actual fun reference(path: String) =
        DatabaseReference(ios.referenceWithPath(path), ios.persistenceEnabled)

    actual fun setPersistenceEnabled(enabled: Boolean) {
        ios.persistenceEnabled = enabled
    }

    actual fun setLoggingEnabled(enabled: Boolean) =
        FIRDatabase.setLoggingEnabled(enabled)
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

    actual fun orderByChild(path: String) = Query(ios.queryOrderedByChild(path), persistenceEnabled)

    actual fun startAt(value: String, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Double, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual fun startAt(value: Boolean, key: String?) = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    actual val valueEvents get() = callbackFlow {
        val handle = ios.observeEventType(
            FIRDataEventTypeValue,
            withBlock = { offer(DataSnapshot(it!!)) }
        ) { close(DatabaseException(it.toString())) }
        awaitClose { ios.removeObserverWithHandle(handle) }
    }

    actual fun childEvents(vararg types: Type) = callbackFlow {
        val handles = types.map { type ->
            ios.observeEventType(
                type.toEventType(),
                andPreviousSiblingKeyWithBlock = { it, key -> offer(ChildEvent(DataSnapshot(it!!), type, key)) }
            ) { close(DatabaseException(it.toString())) }
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

    actual suspend fun setValue(value: Any?) {
        ios.await(persistenceEnabled) { setValue(encode(value), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T) {
        ios.await(persistenceEnabled) { setValue(encode(strategy, value), it) }
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateChildren(update: Map<String, Any?>) {
        ios.await(persistenceEnabled) { updateChildValues(encode(update) as Map<Any?, *>, it) }
    }

    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { removeValueWithCompletionBlock(it) }
    }
}

@Suppress("UNCHECKED_CAST")
actual class DataSnapshot internal constructor(val ios: FIRDataSnapshot) {

    actual val exists get() = ios.exists()

    actual val key: String? get() = ios.key

    actual inline fun <reified T> value() =
        decode<T>(value = ios.value)

    actual fun <T> value(strategy: DeserializationStrategy<T>) =
        decode(strategy, ios.value)

    actual fun child(path: String) = DataSnapshot(ios.childSnapshotForPath(path))
    actual val children: Iterable<DataSnapshot> get() = ios.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot) }
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

    actual suspend fun setValue(value: Any) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encode(value), it) }
    }

    actual suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encode(strategy, value), it) }
    }

    actual suspend fun updateChildren(update: Map<String, Any?>) {
        ios.await(persistenceEnabled) { onDisconnectUpdateChildValues(update.mapValues { (_, it) -> encode(it) } as Map<Any?, *>, it) }
    }
}

actual class DatabaseException(message: String) : RuntimeException(message)

private suspend fun <T, R> T.awaitResult(whileOnline: Boolean, function: T.(callback: (NSError?, R?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R>()
    function { error, result ->
        if(result != null) {
            job.complete(result)
        } else if(error != null) {
            job.completeExceptionally(DatabaseException(error.toString()))
        }
    }
    return job.run { if(whileOnline) awaitWhileOnline() else await() }
}

suspend fun <T> T.await(whileOnline: Boolean, function: T.(callback: (NSError?, FIRDatabaseReference?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error, _ ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(DatabaseException(error.toString()))
        }
    }
    job.run { if(whileOnline) awaitWhileOnline() else await() }
}

private suspend fun <T> CompletableDeferred<T>.awaitWhileOnline(): T = coroutineScope {

    val notConnected = Firebase.database
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select<T> {
        onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected") }
    }
}