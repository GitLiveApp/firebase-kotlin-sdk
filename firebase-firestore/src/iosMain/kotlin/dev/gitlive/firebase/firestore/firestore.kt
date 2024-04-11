/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.*
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType.*
import dev.gitlive.firebase.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSError
import platform.Foundation.NSNull

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(
    FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp)
)

actual class FirebaseFirestore(val ios: FIRFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(NativeCollectionReferenceWrapper(ios.collectionWithPath(collectionPath)))

    actual fun collectionGroup(collectionId: String) = Query(ios.collectionGroupWithID(collectionId).wrapped)

    actual fun document(documentPath: String) = DocumentReference(NativeDocumentReference(ios.documentWithPath(documentPath)))

    actual fun batch() = WriteBatch(NativeWriteBatchWrapper(ios.batch()))

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { ios.runTransactionWithBlock({ transaction, _ -> runBlocking { Transaction(NativeTransactionWrapper(transaction!!)).func() } }, it) } as T

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

actual typealias NativeWriteBatch = FIRWriteBatch

@PublishedApi
internal actual class NativeWriteBatchWrapper actual constructor(actual val native: NativeWriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions
    ): NativeWriteBatchWrapper = when (setOptions) {
        is SetOptions.Merge -> native.setData(encodedData.ios, documentRef.ios, true)
        is SetOptions.Overwrite -> native.setData(encodedData.ios, documentRef.ios, false)
        is SetOptions.MergeFields -> native.setData(encodedData.ios, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> native.setData(encodedData.ios, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeWriteBatchWrapper = native.updateData(encodedData.ios, documentRef.ios).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): NativeWriteBatchWrapper = native.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): NativeWriteBatchWrapper = native.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.ios).let { this }

    actual suspend fun commit() = await { native.commitWithCompletion(it) }
}

val WriteBatch.ios get() = native

actual typealias NativeTransaction = FIRTransaction

@PublishedApi
internal actual class NativeTransactionWrapper actual constructor(actual val native: FIRTransaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions
    ): NativeTransactionWrapper = when (setOptions) {
        is SetOptions.Merge -> native.setData(encodedData.ios, documentRef.ios, true)
        is SetOptions.Overwrite -> native.setData(encodedData.ios, documentRef.ios, false)
        is SetOptions.MergeFields -> native.setData(encodedData.ios, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> native.setData(encodedData.ios, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeTransactionWrapper = native.updateData(encodedData.ios, documentRef.ios).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): NativeTransactionWrapper = native.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): NativeTransactionWrapper = native.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.ios).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { NativeDocumentSnapshotWrapper(native.getDocument(documentRef.ios, it)!!) }

}

val Transaction.ios get() = native

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = FIRDocumentReference

@PublishedApi
internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val listener = ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            snapshot?.let { trySend(NativeDocumentSnapshotWrapper(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    val ios: NativeDocumentReferenceType by ::nativeValue

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val parent: NativeCollectionReferenceWrapper
        get() = NativeCollectionReferenceWrapper(ios.parent)


    actual fun collection(collectionPath: String) = NativeCollectionReferenceWrapper(ios.collectionWithPath(collectionPath))

    actual suspend fun get() =
        NativeDocumentSnapshotWrapper(awaitResult { ios.getDocumentWithCompletion(it) })

    actual suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) = await {
        when (setOptions) {
            is SetOptions.Merge -> ios.setData(encodedData.ios, true, it)
            is SetOptions.Overwrite -> ios.setData(encodedData.ios, false, it)
            is SetOptions.MergeFields -> ios.setData(encodedData.ios, setOptions.fields, it)
            is SetOptions.MergeFieldPaths -> ios.setData(encodedData.ios, setOptions.encodedFieldPaths, it)
        }
    }

    actual suspend fun updateEncoded(encodedData: EncodedObject) = await {
        ios.updateData(encodedData.ios, it)
    }

    actual suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    actual suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    actual suspend fun delete() = await { ios.deleteDocumentWithCompletion(it) }

    actual val snapshots get() = callbackFlow<NativeDocumentSnapshotWrapper> {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(NativeDocumentSnapshotWrapper(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is NativeDocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}

val DocumentReference.ios get() = native.ios

actual typealias NativeQuery = FIRQuery

@PublishedApi
internal actual open class NativeQueryWrapper actual internal constructor(actual open val native: NativeQuery)  {
    actual suspend fun get() = QuerySnapshot(awaitResult { native.getDocumentsWithCompletion(it) })

    actual fun limit(limit: Number) = native.queryLimitedTo(limit.toLong()).wrapped

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

    actual fun where(filter: Filter) = native.queryWhereFilter(filter.toFIRFilter()).wrapped

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

    actual fun orderBy(field: String, direction: Direction) = native.queryOrderedByField(field, direction == Direction.DESCENDING).wrapped
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.queryOrderedByFieldPath(field, direction == Direction.DESCENDING).wrapped

    actual fun startAfter(document: NativeDocumentSnapshot) = native.queryStartingAfterDocument(document).wrapped
    actual fun startAfter(vararg fieldValues: Any) = native.queryStartingAfterValues(fieldValues.asList()).wrapped
    actual fun startAt(document: NativeDocumentSnapshot) = native.queryStartingAtDocument(document).wrapped
    actual fun startAt(vararg fieldValues: Any) = native.queryStartingAtValues(fieldValues.asList()).wrapped

    actual fun endBefore(document: NativeDocumentSnapshot) = native.queryEndingBeforeDocument(document).wrapped
    actual fun endBefore(vararg fieldValues: Any) = native.queryEndingBeforeValues(fieldValues.asList()).wrapped
    actual fun endAt(document: NativeDocumentSnapshot) = native.queryEndingAtDocument(document).wrapped
    actual fun endAt(vararg fieldValues: Any) = native.queryEndingAtValues(fieldValues.asList()).wrapped
}

val Query.ios get() = native

internal val FIRQuery.wrapped get() = NativeQueryWrapper(this)

actual typealias NativeCollectionReference = FIRCollectionReference

@PublishedApi
internal actual class NativeCollectionReferenceWrapper internal actual constructor(actual override val native: NativeCollectionReference) : NativeQueryWrapper(native) {

    actual val path: String
        get() = native.path

    actual val document get() = NativeDocumentReference(native.documentWithAutoID())

    actual val parent get() = native.parent?.let{ NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(native.documentWithPath(documentPath))

    actual suspend fun addEncoded(data: EncodedObject) = NativeDocumentReference(await { native.addDocumentWithData(data.ios, it) })
}

val CollectionReference.ios get() = native

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
        get() = ios.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it as FIRDocumentSnapshot)) }
    actual val documentChanges
        get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
}

actual class DocumentChange(val ios: FIRDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(ios.document))
    actual val newIndex: Int
        get() = ios.newIndex.toInt()
    actual val oldIndex: Int
        get() = ios.oldIndex.toInt()
    actual val type: ChangeType
        get() = ChangeType.values().first { it.ios == ios.type }
}

actual typealias NativeDocumentSnapshot = FIRDocumentSnapshot

@PublishedApi
internal actual class NativeDocumentSnapshotWrapper actual constructor(actual val native: NativeDocumentSnapshot) {

    actual val id get() = native.documentID

    actual val reference get() = NativeDocumentReference(native.reference)

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? =
        native.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    // Despite its name implying otherwise, valueForField accepts both a String representation of a Field and a FIRFieldPath
    actual fun getEncoded(fieldPath: EncodedFieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? =
        native.valueForField(fieldPath, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? =
        native.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
            ?.mapValues { (_, value) ->
                value?.takeIf { it !is NSNull }
            }

    actual fun contains(field: String) = native.valueForField(field) != null
    actual fun contains(fieldPath: EncodedFieldPath) = native.valueForField(fieldPath) != null

    actual val exists get() = native.exists

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(native.metadata)

    fun ServerTimestampBehavior.toIos() : FIRServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorEstimate
        ServerTimestampBehavior.NONE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorNone
        ServerTimestampBehavior.PREVIOUS -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorPrevious
    }
}

val DocumentSnapshot.ios get() = native

actual class SnapshotMetadata(val ios: FIRSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = ios.pendingWrites
    actual val isFromCache: Boolean get() = ios.fromCache
}

actual class FieldPath private constructor(val ios: FIRFieldPath) {
    actual companion object {
        actual val documentId = FieldPath(FIRFieldPath.documentID())
    }
    actual constructor(vararg fieldNames: String) : this(FIRFieldPath(fieldNames.asList()))
    actual val documentId: FieldPath get() = FieldPath.documentId
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
