/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.*
import cocoapods.FirebaseFirestore.FIRDocumentChangeType.*
import dev.gitlive.firebase.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError
import platform.Foundation.NSNull

@PublishedApi
internal inline fun <reified T> decode(value: Any?): T =
    decode(value) { (it as? FIRTimestamp)?.run { seconds * 1000 + (nanoseconds / 1000000.0) } }

internal fun <T> decode(strategy: DeserializationStrategy<T>, value: Any?): T =
    decode(strategy, value) { (it as? FIRTimestamp)?.run { seconds * 1000 + (nanoseconds / 1000000.0) } }

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    encode(value, shouldEncodeElementDefault, FIRFieldValue.fieldValueForServerTimestamp())

private fun <T> encode(strategy: SerializationStrategy<T> , value: T, shouldEncodeElementDefault: Boolean): Any? =
    encode(strategy, value, shouldEncodeElementDefault, FIRFieldValue.fieldValueForServerTimestamp())

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore {
    return FirebaseFirestore(FIRFirestore.firestoreForApp(app.native))
}

@Suppress("UNCHECKED_CAST")
actual class FirebaseFirestore(val native: FIRFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(native.collectionWithPath(collectionPath))

    actual fun collectionGroup(collectionId: String) = Query(native.collectionGroupWithID(collectionId))

    actual fun document(documentPath: String) = DocumentReference(native.documentWithPath(documentPath))

    actual fun batch() = WriteBatch(native.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { native.runTransactionWithBlock({ transaction, _ -> runBlocking { Transaction(transaction!!).func() } }, it) } as T

    actual suspend fun clearPersistence() =
        await { native.clearPersistenceWithCompletion(it) }

    actual fun useEmulator(host: String, port: Int) {
        native.settings = native.settings.apply {
            this.host = "$host:$port"
            persistenceEnabled = false
            sslEnabled = false
        }
    }

    actual fun setSettings(persistenceEnabled: Boolean?, sslEnabled: Boolean?, host: String?, cacheSizeBytes: Long?) {
        native.settings = FIRFirestoreSettings().also { settings ->
            persistenceEnabled?.let { settings.persistenceEnabled = it }
            sslEnabled?.let { settings.sslEnabled = it }
            host?.let { settings.host = it }
            cacheSizeBytes?.let { settings.cacheSizeBytes = it }
        }
    }

    actual suspend fun disableNetwork() {
        await { native.disableNetworkWithCompletion(it) }
    }

    actual suspend fun enableNetwork() {
        await { native.enableNetworkWithCompletion(it) }
    }
}

@Suppress("UNCHECKED_CAST")
actual class WriteBatch(val native: FIRWriteBatch) {

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, merge).let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFields.asList()).let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFieldPaths.map { it.native }).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, merge).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFields.asList()).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFieldPaths.map { it.native }).let { this }

    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        native.updateData(encode(data, encodeDefaults) as Map<Any?, *>, documentRef.native).let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        native.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, documentRef.native).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
            native.updateData(fieldsAndValues.associate { it }, documentRef.native).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        native.updateData(fieldsAndValues.associate { (path, value) -> path.native to value }, documentRef.native).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.native).let { this }

    actual suspend fun commit() = await { native.commitWithCompletion(it) }

}

@Suppress("UNCHECKED_CAST")
actual class Transaction(val native: FIRTransaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, merge).let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFields.asList()).let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFieldPaths.map { it.native }).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, merge).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFields.asList()).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.native, mergeFieldPaths.map { it.native }).let { this }

    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        native.updateData(encode(data, encodeDefaults) as Map<Any?, *>, documentRef.native).let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        native.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, documentRef.native).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        native.updateData(fieldsAndValues.associate { it }, documentRef.native).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        native.updateData(fieldsAndValues.associate { (path, value) -> path.native to value }, documentRef.native).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.native).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { DocumentSnapshot(native.getDocument(documentRef.native, it)!!) }

}

@Suppress("UNCHECKED_CAST")
actual class DocumentReference(val native: FIRDocumentReference) {

    actual val id: String
        get() = native.documentID

    actual val path: String
        get() = native.path

    actual val parent: CollectionReference
        get() = CollectionReference(native.parent)

    actual fun collection(collectionPath: String) = CollectionReference(native.collectionWithPath(collectionPath))

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) =
        await { native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        await { native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        await { native.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.map { it.native }, it) }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        await { native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        await { native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        await { native.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.map { it.native }, it) }

    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        await { native.updateData(encode(data, encodeDefaults) as Map<Any?, *>, it) }

    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        await { native.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) }

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        await { block -> native.updateData(fieldsAndValues.associate { it }, block) }

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        await { block -> native.updateData(fieldsAndValues.associate { (path, value) -> path.native to value }, block) }

    actual suspend fun delete() =
        await { native.deleteDocumentWithCompletion(it) }

    actual suspend fun get() =
        DocumentSnapshot(awaitResult { native.getDocumentWithCompletion(it) })

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val listener = native.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(DocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }
}

actual open class Query(open val native: FIRQuery) {

    actual suspend fun get() = QuerySnapshot(awaitResult { native.getDocumentsWithCompletion(it) })

    actual fun limit(limit: Number) = Query(native.queryLimitedTo(limit.toLong()))

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = native.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val listener = native.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, equalTo: Any?) = Query(native.queryWhereField(field, isEqualTo = equalTo!!))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(native.queryWhereFieldPath(path.native, isEqualTo = equalTo!!))

    internal actual fun _where(field: String, equalTo: DocumentReference) = Query(native.queryWhereField(field, isEqualTo = equalTo.native))
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = Query(native.queryWhereFieldPath(path.native, isEqualTo = equalTo.native))

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { native.queryWhereField(field, isLessThan = it) } ?: native).let { native2 ->
            (greaterThan?.let { native2.queryWhereField(field, isGreaterThan = it) } ?: native2).let { native3 ->
                arrayContains?.let { native3.queryWhereField(field, arrayContains = it) } ?: native3
            }
        }
    )

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { native.queryWhereFieldPath(path.native, isLessThan = it) } ?: native).let { native2 ->
            (greaterThan?.let { native2.queryWhereFieldPath(path.native, isGreaterThan = it) } ?: native2).let { native3 ->
                arrayContains?.let { native3.queryWhereFieldPath(path.native, arrayContains = it) } ?: native3
            }
        }
    )

    internal actual fun _where(field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { native.queryWhereField(field, `in` = it) } ?: native).let { native2 ->
            arrayContainsAny?.let { native2.queryWhereField(field, arrayContainsAny = arrayContainsAny) } ?: native2
        }
    )

    internal actual fun _where(path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { native.queryWhereFieldPath(path.native, `in` = it) } ?: native).let { native2 ->
            arrayContainsAny?.let { native2.queryWhereFieldPath(path.native, arrayContainsAny = arrayContainsAny) } ?: native2
        }
    )

    internal actual fun _orderBy(field: String, direction: Direction) = Query(native.queryOrderedByField(field, direction == Direction.DESCENDING))
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(native.queryOrderedByFieldPath(field.native, direction == Direction.DESCENDING))

    internal actual fun _startAfter(document: DocumentSnapshot) = Query(native.queryStartingAfterDocument(document.native))
    internal actual fun _startAfter(vararg fieldValues: Any) = Query(native.queryStartingAfterValues(fieldValues.asList()))
    internal actual fun _startAt(document: DocumentSnapshot) = Query(native.queryStartingAtDocument(document.native))
    internal actual fun _startAt(vararg fieldValues: Any) = Query(native.queryStartingAtValues(fieldValues.asList()))

    internal actual fun _endBefore(document: DocumentSnapshot) = Query(native.queryEndingBeforeDocument(document.native))
    internal actual fun _endBefore(vararg fieldValues: Any) = Query(native.queryEndingBeforeValues(fieldValues.asList()))
    internal actual fun _endAt(document: DocumentSnapshot) = Query(native.queryEndingAtDocument(document.native))
    internal actual fun _endAt(vararg fieldValues: Any) = Query(native.queryEndingAtValues(fieldValues.asList()))

}
@Suppress("UNCHECKED_CAST")
actual class CollectionReference(override val native: FIRCollectionReference) : Query(native) {

    actual val path: String
        get() = native.path

    actual val document get() = DocumentReference(native.documentWithAutoID())

    actual val parent get() = native.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(native.documentWithPath(documentPath))

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        DocumentReference(await { native.addDocumentWithData(encode(data, encodeDefaults) as Map<Any?, *>, it) })

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean) =
        DocumentReference(await { native.addDocumentWithData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) })
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        DocumentReference(await { native.addDocumentWithData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) })
}

actual class FirebaseFirestoreException(message: String, val code: FirestoreExceptionCode) : FirebaseException(message)

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED
}

actual enum class Direction {
    ASCENDING,
    DESCENDING
}

actual enum class ChangeType(internal val native: FIRDocumentChangeType) {
    ADDED(FIRDocumentChangeTypeAdded),
    MODIFIED(FIRDocumentChangeTypeModified),
    REMOVED(FIRDocumentChangeTypeRemoved)
}

fun NSError.toException() = when(domain) {
    FIRFirestoreErrorDomain -> when(code) {
        FIRFirestoreErrorCodeOK -> FirestoreExceptionCode.OK
        FIRFirestoreErrorCodeCancelled -> FirestoreExceptionCode.CANCELLED
        FIRFirestoreErrorCodeUnknown -> FirestoreExceptionCode.UNKNOWN
        FIRFirestoreErrorCodeInvalidArgument -> FirestoreExceptionCode.INVALID_ARGUMENT
        FIRFirestoreErrorCodeDeadlineExceeded -> FirestoreExceptionCode.DEADLINE_EXCEEDED
        FIRFirestoreErrorCodeNotFound -> FirestoreExceptionCode.NOT_FOUND
        FIRFirestoreErrorCodeAlreadyExists -> FirestoreExceptionCode.ALREADY_EXISTS
        FIRFirestoreErrorCodePermissionDenied -> FirestoreExceptionCode.PERMISSION_DENIED
        FIRFirestoreErrorCodeResourceExhausted -> FirestoreExceptionCode.RESOURCE_EXHAUSTED
        FIRFirestoreErrorCodeFailedPrecondition -> FirestoreExceptionCode.FAILED_PRECONDITION
        FIRFirestoreErrorCodeAborted -> FirestoreExceptionCode.ABORTED
        FIRFirestoreErrorCodeOutOfRange -> FirestoreExceptionCode.OUT_OF_RANGE
        FIRFirestoreErrorCodeUnimplemented -> FirestoreExceptionCode.UNIMPLEMENTED
        FIRFirestoreErrorCodeInternal -> FirestoreExceptionCode.INTERNAL
        FIRFirestoreErrorCodeUnavailable -> FirestoreExceptionCode.UNAVAILABLE
        FIRFirestoreErrorCodeDataLoss -> FirestoreExceptionCode.DATA_LOSS
        FIRFirestoreErrorCodeUnauthenticated -> FirestoreExceptionCode.UNAUTHENTICATED
        else -> FirestoreExceptionCode.UNKNOWN
    }
    else -> FirestoreExceptionCode.UNKNOWN
}.let { FirebaseFirestoreException(description!!, it) }

actual class QuerySnapshot(val native: FIRQuerySnapshot) {
    actual val documents
        get() = native.documents.map { DocumentSnapshot(it as FIRDocumentSnapshot) }
    actual val documentChanges
        get() = native.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(native.metadata)
}

actual class DocumentChange(val native: FIRDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(native.document)
    actual val newIndex: Int
        get() = native.newIndex.toInt()
    actual val oldIndex: Int
        get() = native.oldIndex.toInt()
    actual val type: ChangeType
        get() = ChangeType.values().first { it.native == native.type }
}

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val native: FIRDocumentSnapshot) {

    actual val id get() = native.documentID

    actual val reference get() = DocumentReference(native.reference)

    actual inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = native.dataWithServerTimestampBehavior(serverTimestampBehavior.toNative())
        return decode(value = data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } })
    }

    actual fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = native.dataWithServerTimestampBehavior(serverTimestampBehavior.toNative())
        return decode(strategy, data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } })
    }

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = native.valueForField(field, serverTimestampBehavior.toNative())?.takeIf { it !is NSNull }
        return decode(value)
    }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = native.valueForField(field, serverTimestampBehavior.toNative())?.takeIf { it !is NSNull }
        return decode(strategy, value)
    }

    actual fun contains(field: String) = native.valueForField(field) != null

    actual val exists get() = native.exists

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(native.metadata)

    fun ServerTimestampBehavior.toNative() : FIRServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorEstimate
        ServerTimestampBehavior.NONE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorNone
        ServerTimestampBehavior.PREVIOUS -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorPrevious
    }
}

actual class SnapshotMetadata(val native: FIRSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = native.pendingWrites
    actual val isFromCache: Boolean get() = native.fromCache
}

actual class FieldPath private constructor(val native: FIRFieldPath) {
    actual constructor(vararg fieldNames: String) : this(FIRFieldPath(fieldNames.asList()))
    actual val documentId: FieldPath get() = FieldPath(FIRFieldPath.documentID())
}

actual object FieldValue {
    actual val serverTimestamp = Double.POSITIVE_INFINITY
    actual val delete: Any get() = FIRFieldValue.fieldValueForDelete()
    actual fun increment(value: Int): Any = FIRFieldValue.fieldValueForIntegerIncrement(value.toLong())
    actual fun arrayUnion(vararg elements: Any): Any = FIRFieldValue.fieldValueForArrayUnion(elements.asList())
    actual fun arrayRemove(vararg elements: Any): Any = FIRFieldValue.fieldValueForArrayRemove(elements.asList())
    actual fun delete(): Any = delete
}

private fun <T, R> T.throwError(block: T.(errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
    memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val result = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value
        if (error != null) {
            throw error.toException()
        }
        return result
    }
}

suspend inline fun <reified T> awaitResult(function: (callback: (T?, NSError?) -> Unit) -> Unit): T {
    val job = CompletableDeferred<T?>()
    function { result, error ->
         if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as T
}

suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val result = function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
    return result
}
