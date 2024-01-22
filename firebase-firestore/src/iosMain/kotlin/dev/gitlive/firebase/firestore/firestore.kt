/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.*
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType.*
import dev.gitlive.firebase.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError
import platform.Foundation.NSNull

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(
    FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp)
)

@Suppress("UNCHECKED_CAST")
actual class FirebaseFirestore(val ios: FIRFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual fun collectionGroup(collectionId: String) = Query(ios.collectionGroupWithID(collectionId))

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    actual fun batch() = WriteBatch(ios.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { ios.runTransactionWithBlock({ transaction, _ -> runBlocking { Transaction(transaction!!).func() } }, it) } as T

    actual suspend fun clearPersistence() =
        await { ios.clearPersistenceWithCompletion(it) }

    actual fun useEmulator(host: String, port: Int) {
        ios.settings = ios.settings.apply {
            this.host = "$host:$port"
            persistenceEnabled = false
            sslEnabled = false
        }
    }

    actual fun setSettings(persistenceEnabled: Boolean?, sslEnabled: Boolean?, host: String?, cacheSizeBytes: Long?) {
        ios.settings = FIRFirestoreSettings().also { settings ->
            persistenceEnabled?.let { settings.persistenceEnabled = it }
            sslEnabled?.let { settings.sslEnabled = it }
            host?.let { settings.host = it }
            cacheSizeBytes?.let { settings.cacheSizeBytes = it }
        }
    }

    actual suspend fun disableNetwork() {
        await { ios.disableNetworkWithCompletion(it) }
    }

    actual suspend fun enableNetwork() {
        await { ios.enableNetworkWithCompletion(it) }
    }
}

@Suppress("UNCHECKED_CAST")
actual class WriteBatch(val ios: FIRWriteBatch) : BaseWriteBatch() {

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseWriteBatch = when (setOptions) {
        is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, true)
        is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, false)
        is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch = ios.updateData(encodedData as Map<Any?, *>, documentRef.ios).let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): BaseWriteBatch = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun commit() = await { ios.commitWithCompletion(it) }
}

@Suppress("UNCHECKED_CAST")
actual class Transaction(val ios: FIRTransaction) : BaseTransaction() {

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseTransaction = when (setOptions) {
        is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, true)
        is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, false)
        is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseTransaction = ios.updateData(encodedData as Map<Any?, *>, documentRef.ios).let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseTransaction = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): BaseTransaction = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { DocumentSnapshot(ios.getDocument(documentRef.ios, it)!!) }

}

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReference = FIRDocumentReference

@Suppress("UNCHECKED_CAST")
@Serializable(with = DocumentReferenceSerializer::class)
actual class DocumentReference actual constructor(internal actual val nativeValue: NativeDocumentReference) : BaseDocumentReference() {

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val listener = ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            snapshot?.let { trySend(DocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    val ios: NativeDocumentReference by ::nativeValue

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val parent: CollectionReference
        get() = CollectionReference(ios.parent)


    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual suspend fun get() =
        DocumentSnapshot(awaitResult { ios.getDocumentWithCompletion(it) })

    override suspend fun setEncoded(encodedData: Any, setOptions: SetOptions) = await {
        when (setOptions) {
            is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, true, it)
            is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, false, it)
            is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, setOptions.fields, it)
            is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, setOptions.encodedFieldPaths, it)
        }
    }

    override suspend fun updateEncoded(encodedData: Any) = await {
        ios.updateData(encodedData as Map<Any?, *>, it)
    }

    override suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    override suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    override suspend fun delete() = await { ios.deleteDocumentWithCompletion(it) }

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(DocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is DocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}

actual typealias NativeQuery = FIRQuery

actual open class Query internal actual constructor(nativeQuery: NativeQuery) {

    open val ios: FIRQuery = nativeQuery

    actual suspend fun get() = QuerySnapshot(awaitResult { ios.getDocumentsWithCompletion(it) })

    actual fun limit(limit: Number) = Query(ios.queryLimitedTo(limit.toLong()))

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val listener = ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun where(filter: Filter): Query = Query(
        ios.queryWhereFilter(filter.toFIRFilter())
    )

    private fun Filter.toFIRFilter(): FIRFilter = when (this) {
        is Filter.And -> FIRFilter.andFilterWithFilters(filters.map { it.toFIRFilter() })
        is Filter.Or -> FIRFilter.orFilterWithFilters(filters.map { it.toFIRFilter() })
        is Filter.Field -> when (constraint) {
            is WhereConstraint.EqualTo -> FIRFilter.filterWhereField(field, isEqualTo = constraint.safeValue ?: NSNull.`null`())
            is WhereConstraint.NotEqualTo -> FIRFilter.filterWhereField(field, isNotEqualTo = constraint.safeValue ?: NSNull.`null`())
            is WhereConstraint.LessThan -> FIRFilter.filterWhereField(field, isLessThan = constraint.safeValue)
            is WhereConstraint.GreaterThan -> FIRFilter.filterWhereField(field, isGreaterThan = constraint.safeValue)
            is WhereConstraint.LessThanOrEqualTo -> FIRFilter.filterWhereField(field, isLessThanOrEqualTo = constraint.safeValue)
            is WhereConstraint.GreaterThanOrEqualTo -> FIRFilter.filterWhereField(field, isGreaterThanOrEqualTo = constraint.safeValue)
            is WhereConstraint.ArrayContains -> FIRFilter.filterWhereField(field, arrayContains = constraint.safeValue)
            is WhereConstraint.ArrayContainsAny -> FIRFilter.filterWhereField(field, arrayContainsAny = constraint.safeValues)
            is WhereConstraint.InArray -> FIRFilter.filterWhereField(field, `in` = constraint.safeValues)
            is WhereConstraint.NotInArray -> FIRFilter.filterWhereField(field, notIn = constraint.safeValues)
        }
        is Filter.Path -> when (constraint) {
            is WhereConstraint.EqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isEqualTo = constraint.safeValue ?: NSNull.`null`())
            is WhereConstraint.NotEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isNotEqualTo = constraint.safeValue ?: NSNull.`null`())
            is WhereConstraint.LessThan -> FIRFilter.filterWhereFieldPath(path.ios, isLessThan = constraint.safeValue)
            is WhereConstraint.GreaterThan -> FIRFilter.filterWhereFieldPath(path.ios, isGreaterThan = constraint.safeValue)
            is WhereConstraint.LessThanOrEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isLessThanOrEqualTo = constraint.safeValue)
            is WhereConstraint.GreaterThanOrEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isGreaterThanOrEqualTo = constraint.safeValue)
            is WhereConstraint.ArrayContains -> FIRFilter.filterWhereFieldPath(path.ios, arrayContains = constraint.safeValue)
            is WhereConstraint.ArrayContainsAny -> FIRFilter.filterWhereFieldPath(path.ios, arrayContainsAny = constraint.safeValues)
            is WhereConstraint.InArray -> FIRFilter.filterWhereFieldPath(path.ios, `in` = constraint.safeValues)
            is WhereConstraint.NotInArray -> FIRFilter.filterWhereFieldPath(path.ios, notIn = constraint.safeValues)
        }
    }

    internal actual fun _orderBy(field: String, direction: Direction) = Query(ios.queryOrderedByField(field, direction == Direction.DESCENDING))
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(ios.queryOrderedByFieldPath(field.ios, direction == Direction.DESCENDING))

    internal actual fun _startAfter(document: DocumentSnapshot) = Query(ios.queryStartingAfterDocument(document.ios))
    internal actual fun _startAfter(vararg fieldValues: Any) = Query(ios.queryStartingAfterValues(fieldValues.asList()))
    internal actual fun _startAt(document: DocumentSnapshot) = Query(ios.queryStartingAtDocument(document.ios))
    internal actual fun _startAt(vararg fieldValues: Any) = Query(ios.queryStartingAtValues(fieldValues.asList()))

    internal actual fun _endBefore(document: DocumentSnapshot) = Query(ios.queryEndingBeforeDocument(document.ios))
    internal actual fun _endBefore(vararg fieldValues: Any) = Query(ios.queryEndingBeforeValues(fieldValues.asList()))
    internal actual fun _endAt(document: DocumentSnapshot) = Query(ios.queryEndingAtDocument(document.ios))
    internal actual fun _endAt(vararg fieldValues: Any) = Query(ios.queryEndingAtValues(fieldValues.asList()))

}

@Suppress("UNCHECKED_CAST")
actual class CollectionReference(override val ios: FIRCollectionReference) : BaseCollectionReference(ios) {

    actual val path: String
        get() = ios.path

    actual val document get() = DocumentReference(ios.documentWithAutoID())

    actual val parent get() = ios.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    override suspend fun addEncoded(data: Any) = DocumentReference(await { ios.addDocumentWithData(data as Map<Any?, *>, it) })
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

actual enum class ChangeType(internal val ios: FIRDocumentChangeType) {
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

actual class QuerySnapshot(val ios: FIRQuerySnapshot) {
    actual val documents
        get() = ios.documents.map { DocumentSnapshot(it as FIRDocumentSnapshot) }
    actual val documentChanges
        get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
}

actual class DocumentChange(val ios: FIRDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(ios.document)
    actual val newIndex: Int
        get() = ios.newIndex.toInt()
    actual val oldIndex: Int
        get() = ios.oldIndex.toInt()
    actual val type: ChangeType
        get() = ChangeType.values().first { it.ios == ios.type }
}

actual class DocumentSnapshot(val ios: FIRDocumentSnapshot) : BaseDocumentSnapshot() {

    actual val id get() = ios.documentID

    actual val reference get() = DocumentReference(ios.reference)

    override fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? =
        ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    override fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? =
        ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
            ?.mapValues { (_, value) ->
                value?.takeIf { it !is NSNull }
            }

    actual fun contains(field: String) = ios.valueForField(field) != null

    actual val exists get() = ios.exists

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)

    fun ServerTimestampBehavior.toIos() : FIRServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorEstimate
        ServerTimestampBehavior.NONE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorNone
        ServerTimestampBehavior.PREVIOUS -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorPrevious
    }
}

actual class SnapshotMetadata(val ios: FIRSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = ios.pendingWrites
    actual val isFromCache: Boolean get() = ios.fromCache
}

actual class FieldPath private constructor(val ios: FIRFieldPath) {
    actual constructor(vararg fieldNames: String) : this(FIRFieldPath(fieldNames.asList()))
    actual val documentId: FieldPath get() = FieldPath(FIRFieldPath.documentID())
    actual val encoded: EncodedFieldPath = ios
    override fun equals(other: Any?): Boolean = other is FieldPath && ios == other.ios
    override fun hashCode(): Int = ios.hashCode()
    override fun toString(): String = ios.toString()
}

actual typealias EncodedFieldPath = FIRFieldPath

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
