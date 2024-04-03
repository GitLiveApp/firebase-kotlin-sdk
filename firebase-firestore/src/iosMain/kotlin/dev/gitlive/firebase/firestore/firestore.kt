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

    actual fun collection(collectionPath: String) = CollectionReference(NativeCollectionReference(ios.collectionWithPath(collectionPath)))

    actual fun collectionGroup(collectionId: String) = Query(ios.collectionGroupWithID(collectionId).native)

    actual fun document(documentPath: String) = DocumentReference(NativeDocumentReference(ios.documentWithPath(documentPath)))

    actual fun batch() = WriteBatch(NativeWriteBatch(ios.batch()))

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { ios.runTransactionWithBlock({ transaction, _ -> runBlocking { Transaction(NativeTransaction(transaction!!)).func() } }, it) } as T

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
@PublishedApi
internal actual class NativeWriteBatch(val ios: FIRWriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): NativeWriteBatch = when (setOptions) {
        is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, true)
        is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, false)
        is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: Any): NativeWriteBatch = ios.updateData(encodedData as Map<Any?, *>, documentRef.ios).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): NativeWriteBatch = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): NativeWriteBatch = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun commit() = await { ios.commitWithCompletion(it) }
}

val WriteBatch.ios get() = native.ios

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal actual class NativeTransaction(val ios: FIRTransaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): NativeTransaction = when (setOptions) {
        is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, true)
        is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, false)
        is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: Any): NativeTransaction = ios.updateData(encodedData as Map<Any?, *>, documentRef.ios).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): NativeTransaction = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): NativeTransaction = ios.updateData(
        encodedFieldsAndValues.toMap(),
        documentRef.ios
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { NativeDocumentSnapshot(ios.getDocument(documentRef.ios, it)!!) }

}

val Transaction.ios get() = native.ios

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = FIRDocumentReference

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val listener = ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
            snapshot?.let { trySend(NativeDocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    val ios: NativeDocumentReferenceType by ::nativeValue

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val parent: NativeCollectionReference
        get() = NativeCollectionReference(ios.parent)


    actual fun collection(collectionPath: String) = NativeCollectionReference(ios.collectionWithPath(collectionPath))

    actual suspend fun get() =
        NativeDocumentSnapshot(awaitResult { ios.getDocumentWithCompletion(it) })

    actual suspend fun setEncoded(encodedData: Any, setOptions: SetOptions) = await {
        when (setOptions) {
            is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, true, it)
            is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, false, it)
            is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, setOptions.fields, it)
            is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, setOptions.encodedFieldPaths, it)
        }
    }

    actual suspend fun updateEncoded(encodedData: Any) = await {
        ios.updateData(encodedData as Map<Any?, *>, it)
    }

    actual suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    actual suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) = await {
        ios.updateData(encodedFieldsAndValues.toMap(), it)
    }

    actual suspend fun delete() = await { ios.deleteDocumentWithCompletion(it) }

    actual val snapshots get() = callbackFlow<NativeDocumentSnapshot> {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(NativeDocumentSnapshot(snapshot)) }
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

@PublishedApi
internal actual open class NativeQuery(open val ios: FIRQuery)
internal val FIRQuery.native get() = NativeQuery(this)

actual open class Query internal actual constructor(nativeQuery: NativeQuery) {

    open val ios: FIRQuery = nativeQuery.ios

    actual suspend fun get() = QuerySnapshot(awaitResult { ios.getDocumentsWithCompletion(it) })

    actual fun limit(limit: Number) = Query(ios.queryLimitedTo(limit.toLong()).native)

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
        ios.queryWhereFilter(filter.toFIRFilter()).native
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

    internal actual fun _orderBy(field: String, direction: Direction) = Query(ios.queryOrderedByField(field, direction == Direction.DESCENDING).native)
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(ios.queryOrderedByFieldPath(field.ios, direction == Direction.DESCENDING).native)

    internal actual fun _startAfter(document: DocumentSnapshot) = Query(ios.queryStartingAfterDocument(document.ios).native)
    internal actual fun _startAfter(vararg fieldValues: Any) = Query(ios.queryStartingAfterValues(fieldValues.asList()).native)
    internal actual fun _startAt(document: DocumentSnapshot) = Query(ios.queryStartingAtDocument(document.ios).native)
    internal actual fun _startAt(vararg fieldValues: Any) = Query(ios.queryStartingAtValues(fieldValues.asList()).native)

    internal actual fun _endBefore(document: DocumentSnapshot) = Query(ios.queryEndingBeforeDocument(document.ios).native)
    internal actual fun _endBefore(vararg fieldValues: Any) = Query(ios.queryEndingBeforeValues(fieldValues.asList()).native)
    internal actual fun _endAt(document: DocumentSnapshot) = Query(ios.queryEndingAtDocument(document.ios).native)
    internal actual fun _endAt(vararg fieldValues: Any) = Query(ios.queryEndingAtValues(fieldValues.asList()).native)

}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal actual class NativeCollectionReference(override val ios: FIRCollectionReference) : NativeQuery(ios) {

    actual val path: String
        get() = ios.path

    actual val document get() = NativeDocumentReference(ios.documentWithAutoID())

    actual val parent get() = ios.parent?.let{ NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(ios.documentWithPath(documentPath))

    actual suspend fun addEncoded(data: Any) = NativeDocumentReference(await { ios.addDocumentWithData(data as Map<Any?, *>, it) })
}

val CollectionReference.ios get() = native.ios

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
        get() = ios.documents.map { DocumentSnapshot(NativeDocumentSnapshot(it as FIRDocumentSnapshot)) }
    actual val documentChanges
        get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
}

actual class DocumentChange(val ios: FIRDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshot(ios.document))
    actual val newIndex: Int
        get() = ios.newIndex.toInt()
    actual val oldIndex: Int
        get() = ios.oldIndex.toInt()
    actual val type: ChangeType
        get() = ChangeType.values().first { it.ios == ios.type }
}

@PublishedApi
internal actual class NativeDocumentSnapshot(val ios: FIRDocumentSnapshot) {

    actual val id get() = ios.documentID

    actual val reference get() = NativeDocumentReference(ios.reference)

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? =
        ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? =
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

val DocumentSnapshot.ios get() = native.ios

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
