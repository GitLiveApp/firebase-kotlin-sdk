/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildAdded
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildChanged
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildMoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildRemoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeValue
import cocoapods.FirebaseDatabase.FIRDatabaseHandle
import cocoapods.FirebaseDatabase.FIRDatabaseQuery
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseDatabase.FIRTransactionResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import platform.Foundation.NSError
import platform.Foundation.NSNull

private const val IOS_PERMISSION_DENIED = 1L
private const val IOS_UNAVAILABLE = 2L
private const val IOS_WRITE_CANCELED = 3L

/** @property ios The underlying Firebase iOS SDK object. */
public actual open class Query internal constructor(public open val ios: FIRDatabaseQuery) {
    public actual val ref: DatabaseReference get() = DatabaseReference(ios.ref)

    public actual fun addValueEventListener(listener: ValueEventListener): ValueEventListener {
        val handle = ios.observeEventType(
            FIRDataEventTypeValue,
            withBlock = { snapshot -> listener.onDataChange(DataSnapshot(snapshot!!)) },
            withCancelBlock = { error -> listener.onCancelled(error!!.toDatabaseError()) },
        )
        Observers.add(listener, ios, handle)
        return listener
    }

    public actual fun addChildEventListener(listener: ChildEventListener): ChildEventListener {
        fun observe(type: FIRDataEventType, onEvent: (DataSnapshot, String?) -> Unit) {
            val handle = ios.observeEventType(
                type,
                andPreviousSiblingKeyWithBlock = { snapshot, previousKey -> onEvent(DataSnapshot(snapshot!!), previousKey) },
                withCancelBlock = { error -> listener.onCancelled(error!!.toDatabaseError()) },
            )
            Observers.add(listener, ios, handle)
        }
        observe(FIRDataEventTypeChildAdded, listener::onChildAdded)
        observe(FIRDataEventTypeChildChanged, listener::onChildChanged)
        observe(FIRDataEventTypeChildMoved, listener::onChildMoved)
        observe(FIRDataEventTypeChildRemoved) { snapshot, _ -> listener.onChildRemoved(snapshot) }
        return listener
    }

    public actual fun addListenerForSingleValueEvent(listener: ValueEventListener) {
        ios.observeSingleEventOfType(
            FIRDataEventTypeValue,
            withBlock = { snapshot -> listener.onDataChange(DataSnapshot(snapshot!!)) },
            withCancelBlock = { error -> listener.onCancelled(error!!.toDatabaseError()) },
        )
    }

    public actual fun removeEventListener(listener: ValueEventListener) {
        Observers.remove(listener)
    }

    public actual fun removeEventListener(listener: ChildEventListener) {
        Observers.remove(listener)
    }

    public actual fun get(): Task<DataSnapshot> = task { completion ->
        ios.getDataWithCompletionBlock { error, snapshot -> completion(snapshot?.let { DataSnapshot(it) }, error) }
    }

    /** Keeps the data of this query synchronized in the persistent cache. */
    internal fun keepSyncedValue(keepSynced: Boolean) {
        ios.keepSynced(keepSynced)
    }

    public actual fun orderByChild(path: String): Query = Query(ios.queryOrderedByChild(path))
    public actual fun orderByKey(): Query = Query(ios.queryOrderedByKey())
    public actual fun orderByPriority(): Query = Query(ios.queryOrderedByPriority())
    public actual fun orderByValue(): Query = Query(ios.queryOrderedByValue())
    public actual fun limitToFirst(limit: Int): Query = Query(ios.queryLimitedToFirst(limit.toULong()))
    public actual fun limitToLast(limit: Int): Query = Query(ios.queryLimitedToLast(limit.toULong()))
    // With a null key the single-argument iOS methods are used: the childKey variants reject a nil key under orderByKey.
    public actual fun startAt(value: String?): Query = Query(ios.queryStartingAtValue(value))
    public actual fun startAt(value: String?, key: String?): Query = Query(if (key == null) ios.queryStartingAtValue(value) else ios.queryStartingAtValue(value, key))
    public actual fun startAt(value: Double): Query = Query(ios.queryStartingAtValue(value))
    public actual fun startAt(value: Double, key: String?): Query = Query(if (key == null) ios.queryStartingAtValue(value) else ios.queryStartingAtValue(value, key))
    public actual fun startAt(value: Boolean): Query = Query(ios.queryStartingAtValue(value))
    public actual fun startAt(value: Boolean, key: String?): Query = Query(if (key == null) ios.queryStartingAtValue(value) else ios.queryStartingAtValue(value, key))
    public actual fun startAfter(value: String?): Query = Query(ios.queryStartingAfterValue(value))
    public actual fun startAfter(value: String?, key: String?): Query = Query(if (key == null) ios.queryStartingAfterValue(value) else ios.queryStartingAfterValue(value, key))
    public actual fun startAfter(value: Double): Query = Query(ios.queryStartingAfterValue(value))
    public actual fun startAfter(value: Double, key: String?): Query = Query(if (key == null) ios.queryStartingAfterValue(value) else ios.queryStartingAfterValue(value, key))
    public actual fun startAfter(value: Boolean): Query = Query(ios.queryStartingAfterValue(value))
    public actual fun startAfter(value: Boolean, key: String?): Query = Query(if (key == null) ios.queryStartingAfterValue(value) else ios.queryStartingAfterValue(value, key))
    public actual fun endAt(value: String?): Query = Query(ios.queryEndingAtValue(value))
    public actual fun endAt(value: String?, key: String?): Query = Query(if (key == null) ios.queryEndingAtValue(value) else ios.queryEndingAtValue(value, key))
    public actual fun endAt(value: Double): Query = Query(ios.queryEndingAtValue(value))
    public actual fun endAt(value: Double, key: String?): Query = Query(if (key == null) ios.queryEndingAtValue(value) else ios.queryEndingAtValue(value, key))
    public actual fun endAt(value: Boolean): Query = Query(ios.queryEndingAtValue(value))
    public actual fun endAt(value: Boolean, key: String?): Query = Query(if (key == null) ios.queryEndingAtValue(value) else ios.queryEndingAtValue(value, key))
    public actual fun endBefore(value: String?): Query = Query(ios.queryEndingBeforeValue(value))
    public actual fun endBefore(value: String?, key: String?): Query = Query(if (key == null) ios.queryEndingBeforeValue(value) else ios.queryEndingBeforeValue(value, key))
    public actual fun endBefore(value: Double): Query = Query(ios.queryEndingBeforeValue(value))
    public actual fun endBefore(value: Double, key: String?): Query = Query(if (key == null) ios.queryEndingBeforeValue(value) else ios.queryEndingBeforeValue(value, key))
    public actual fun endBefore(value: Boolean): Query = Query(ios.queryEndingBeforeValue(value))
    public actual fun endBefore(value: Boolean, key: String?): Query = Query(if (key == null) ios.queryEndingBeforeValue(value) else ios.queryEndingBeforeValue(value, key))
    public actual fun equalTo(value: String?): Query = Query(ios.queryEqualToValue(value))
    public actual fun equalTo(value: String?, key: String?): Query = Query(if (key == null) ios.queryEqualToValue(value) else ios.queryEqualToValue(value, key))
    public actual fun equalTo(value: Double): Query = Query(ios.queryEqualToValue(value))
    public actual fun equalTo(value: Double, key: String?): Query = Query(if (key == null) ios.queryEqualToValue(value) else ios.queryEqualToValue(value, key))
    public actual fun equalTo(value: Boolean): Query = Query(ios.queryEqualToValue(value))
    public actual fun equalTo(value: Boolean, key: String?): Query = Query(if (key == null) ios.queryEqualToValue(value) else ios.queryEqualToValue(value, key))

    /** The iOS SDK creates a new object per call, so queries are equal when they describe the same location and constraints. */
    override fun equals(other: Any?): Boolean = other is Query && (other.ios == ios || other.ios.description == ios.description)

    override fun hashCode(): Int = ios.description.hashCode()

    override fun toString(): String = ios.description.orEmpty()
}

/** The iOS observer handles of each listener, so that a listener can be removed the Android way. */
private object Observers {
    private val handles = mutableMapOf<Any, MutableList<Pair<FIRDatabaseQuery, FIRDatabaseHandle>>>()

    fun add(listener: Any, query: FIRDatabaseQuery, handle: FIRDatabaseHandle) {
        handles.getOrPut(listener) { mutableListOf() } += query to handle
    }

    fun remove(listener: Any) {
        handles.remove(listener)?.forEach { (query, handle) -> query.removeObserverWithHandle(handle) }
    }
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual open class DatabaseReference internal constructor(override val ios: FIRDatabaseReference) : Query(ios) {
    public actual val database: FirebaseDatabase get() = FirebaseDatabase(ios.database)
    public actual val key: String? get() = ios.key
    public actual val parent: DatabaseReference? get() = ios.parent?.let { DatabaseReference(it) }
    public actual val root: DatabaseReference get() = DatabaseReference(ios.root)

    public actual fun child(pathString: String): DatabaseReference = DatabaseReference(ios.child(pathString))

    public actual fun push(): DatabaseReference = DatabaseReference(ios.childByAutoId())

    public actual fun onDisconnect(): OnDisconnect = OnDisconnect(ios)

    public actual fun setValue(value: Any?): Task<Nothing?> = write { ios.setValue(value, withCompletionBlock = it) }

    public actual fun setValue(value: Any?, listener: CompletionListener?) {
        ios.setValue(value, withCompletionBlock = listener.completion())
    }

    public actual fun setValue(value: Any?, priority: Any?): Task<Nothing?> = write { ios.setValue(value, andPriority = priority, withCompletionBlock = it) }

    public actual fun setValue(value: Any?, priority: Any?, listener: CompletionListener?) {
        ios.setValue(value, andPriority = priority, withCompletionBlock = listener.completion())
    }

    public actual fun setPriority(priority: Any?): Task<Nothing?> = write { ios.setPriority(priority, withCompletionBlock = it) }

    public actual fun setPriority(priority: Any?, listener: CompletionListener?) {
        ios.setPriority(priority, withCompletionBlock = listener.completion())
    }

    public actual fun updateChildren(update: Map<String, Any?>): Task<Nothing?> = write { ios.updateChildValues(update.toIosMap(), withCompletionBlock = it) }

    public actual fun updateChildren(update: Map<String, Any?>, listener: CompletionListener?) {
        ios.updateChildValues(update.toIosMap(), withCompletionBlock = listener.completion())
    }

    public actual fun removeValue(): Task<Nothing?> = write { ios.removeValueWithCompletionBlock(it) }

    public actual fun removeValue(listener: CompletionListener?) {
        ios.removeValueWithCompletionBlock(listener.completion())
    }

    public actual fun runTransaction(handler: Transaction.Handler) {
        runTransaction(handler, true)
    }

    public actual fun runTransaction(handler: Transaction.Handler, fireLocalEvents: Boolean) {
        ios.runTransactionBlock(
            block = { data ->
                if (handler.doTransaction(MutableData(data!!)).isSuccess) FIRTransactionResult.successWithValue(data) else FIRTransactionResult.abort()
            },
            andCompletionBlock = { error, committed, snapshot ->
                handler.onComplete(error?.toDatabaseError(), committed, snapshot?.let { DataSnapshot(it) })
            },
            withLocalEvents = fireLocalEvents,
        )
    }

    public actual fun interface CompletionListener {
        public actual fun onComplete(error: DatabaseError?, ref: DatabaseReference)
    }

    private fun CompletionListener?.completion(): (NSError?, FIRDatabaseReference?) -> Unit = { error, ref ->
        this?.onComplete(error?.toDatabaseError(), DatabaseReference(ref ?: ios))
    }

    public actual companion object {
        public actual fun goOffline() {
            FIRDatabaseReference.goOffline()
        }

        public actual fun goOnline() {
            FIRDatabaseReference.goOnline()
        }
    }
}

/** @property ios The underlying Firebase iOS SDK reference the operations are registered on. */
public actual class OnDisconnect internal constructor(public val ios: FIRDatabaseReference) {
    public actual fun setValue(value: Any?): Task<Nothing?> = write { ios.onDisconnectSetValue(value, withCompletionBlock = it) }

    public actual fun setValue(value: Any?, listener: DatabaseReference.CompletionListener?) {
        ios.onDisconnectSetValue(value, withCompletionBlock = listener.completion())
    }

    public actual fun setValue(value: Any?, priority: String?): Task<Nothing?> = write { setWithPriority(value, priority, it) }

    public actual fun setValue(value: Any?, priority: String?, listener: DatabaseReference.CompletionListener?) {
        setWithPriority(value, priority, listener.completion())
    }

    public actual fun setValue(value: Any?, priority: Double): Task<Nothing?> = write { ios.onDisconnectSetValue(value, andPriority = priority, withCompletionBlock = it) }

    public actual fun setValue(value: Any?, priority: Double, listener: DatabaseReference.CompletionListener?) {
        ios.onDisconnectSetValue(value, andPriority = priority, withCompletionBlock = listener.completion())
    }

    public actual fun setValue(value: Any?, priority: Map<*, *>?, listener: DatabaseReference.CompletionListener?) {
        setWithPriority(value, priority, listener.completion())
    }

    /** The iOS SDK's priority form takes a non-null priority. */
    private fun setWithPriority(value: Any?, priority: Any?, completion: (NSError?, FIRDatabaseReference?) -> Unit) {
        if (priority == null) ios.onDisconnectSetValue(value, withCompletionBlock = completion) else ios.onDisconnectSetValue(value, andPriority = priority, withCompletionBlock = completion)
    }

    public actual fun updateChildren(update: Map<String, Any?>): Task<Nothing?> = write { ios.onDisconnectUpdateChildValues(update.toIosMap(), withCompletionBlock = it) }

    public actual fun updateChildren(update: Map<String, Any?>, listener: DatabaseReference.CompletionListener?) {
        ios.onDisconnectUpdateChildValues(update.toIosMap(), withCompletionBlock = listener.completion())
    }

    public actual fun removeValue(): Task<Nothing?> = write { ios.onDisconnectRemoveValueWithCompletionBlock(it) }

    public actual fun removeValue(listener: DatabaseReference.CompletionListener?) {
        ios.onDisconnectRemoveValueWithCompletionBlock(listener.completion())
    }

    public actual fun cancel(): Task<Nothing?> = write { ios.cancelDisconnectOperationsWithCompletionBlock(it) }

    public actual fun cancel(listener: DatabaseReference.CompletionListener) {
        ios.cancelDisconnectOperationsWithCompletionBlock(listener.completion())
    }

    private fun DatabaseReference.CompletionListener?.completion(): (NSError?, FIRDatabaseReference?) -> Unit = { error, ref ->
        this?.onComplete(error?.toDatabaseError(), DatabaseReference(ref ?: ios))
    }
}

@Suppress("UNCHECKED_CAST")
/** Null values become NSNull, which the SDK reads as a deletion. */
internal fun Map<String, Any?>.toIosMap(): Map<Any?, *> = mapValues { (_, value) -> value ?: NSNull.`null`() }

/** A write as a [Task] of the SDK's completion block. */
private inline fun write(crossinline start: ((NSError?, FIRDatabaseReference?) -> Unit) -> Unit): Task<Nothing?> = task { completion ->
    start { error, _ -> completion(null, error) }
}

internal inline fun <T> task(crossinline start: ((T?, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(error.toDatabaseError().toException()) }
    return source.task
}

/** The iOS SDK reports permission denied, unavailable and write cancelled by code; everything else is unknown. */
internal fun NSError.toDatabaseError(): DatabaseError = DatabaseError(
    when (code) {
        IOS_PERMISSION_DENIED -> DatabaseError.PERMISSION_DENIED
        IOS_UNAVAILABLE -> DatabaseError.UNAVAILABLE
        IOS_WRITE_CANCELED -> DatabaseError.WRITE_CANCELED
        else -> DatabaseError.UNKNOWN_ERROR
    },
    localizedDescription,
)
