/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.*
import cocoapods.FirebaseFirestore.FIRDocumentChangeType.*
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
import platform.Foundation.NSNumber
import platform.darwin.dispatch_queue_t

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(
    FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp)
)

@Suppress("CAST_NEVER_SUCCEEDS")
val LocalCacheSettings.ios: FIRLocalCacheSettingsProtocol get() = when (this) {
    is LocalCacheSettings.Persistent -> sizeBytes?.let { FIRPersistentCacheSettings(it as NSNumber) } ?: FIRPersistentCacheSettings()
    is LocalCacheSettings.Memory -> FIRMemoryCacheSettings(
        when (garbaseCollectorSettings) {
            is LocalCacheSettings.Memory.GarbageCollectorSettings.Eager -> FIRMemoryEagerGCSettings()
            is LocalCacheSettings.Memory.GarbageCollectorSettings.LRUGC -> garbaseCollectorSettings.sizeBytes?.let { FIRMemoryLRUGCSettings(it as NSNumber) } ?: FIRMemoryLRUGCSettings()
        }
    )
}

@Suppress("UNCHECKED_CAST")
actual data class FirebaseFirestore(val ios: FIRFirestore) {

    actual data class Settings(
        actual val sslEnabled: Boolean? = null,
        actual val host: String? = null,
        actual val cacheSettings: LocalCacheSettings? = null,
        val dispatchQueue: dispatch_queue_t = null
    ) {
        actual companion object {
            actual fun create(sslEnabled: Boolean?, host: String?, cacheSettings: LocalCacheSettings?) = Settings(sslEnabled, host, cacheSettings)
        }
    }

    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    actual fun collectionGroup(collectionId: String) = Query(ios.collectionGroupWithID(collectionId))

    actual fun batch() = WriteBatch(ios.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { ios.runTransactionWithBlock({ transaction, _ -> runBlocking { Transaction(transaction!!).func() } }, it) } as T

    actual suspend fun clearPersistence() =
        await { ios.clearPersistenceWithCompletion(it) }

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
        ios.settings = ios.settings.apply {
            cacheSettings = FIRMemoryCacheSettings()
            sslEnabled = false
        }
    }

    actual fun setSettings(settings: Settings) {
        ios.settings = FIRFirestoreSettings().applySettings(settings)
    }

    actual fun updateSettings(settings: Settings) {
        ios.settings = ios.settings.applySettings(settings)
    }

    private fun FIRFirestoreSettings.applySettings(settings: Settings): FIRFirestoreSettings = apply {
        settings.cacheSettings?.let { cacheSettings = it.ios }
        settings.sslEnabled?.let { sslEnabled = it }
        settings.host?.let { host = it }
        settings.dispatchQueue?.let { dispatchQueue = it }
    }

    actual suspend fun disableNetwork() {
        await { ios.disableNetworkWithCompletion(it) }
    }

    actual suspend fun enableNetwork() {
        await { ios.enableNetworkWithCompletion(it) }
    }

    override fun toString(): String = ios.app.toString()
}

@Suppress("UNCHECKED_CAST")
actual class WriteBatch(val ios: FIRWriteBatch) : BaseWriteBatch() {

    actual val async = Async(ios)

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

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>,
        merge: Boolean
    ): BaseWriteBatch {
        val serializedItem = encodedData as Map<Any?, *>
        val serializedFieldAndValues = encodedFieldsAndValues.toMap()

        val result = serializedItem + serializedFieldAndValues
        ios.setData(result as Map<Any?, *>, documentRef.ios, merge)
        return this
    }

    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch = ios.updateData(encodedData as Map<Any?, *>, documentRef.ios).let { this }

    override fun updateEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch {
        val serializedItem = encodedData as Map<Any?, *>
        val serializedFieldAndValues = encodedFieldsAndValues.toMap()

        val result = serializedItem + serializedFieldAndValues
        return ios.updateData(result as Map<Any?, *>, documentRef.ios).let { this }
    }

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

    actual suspend fun commit() = async.commit().await()

    actual class Async(@PublishedApi internal val ios: FIRWriteBatch) {
        actual fun commit() = deferred { ios.commitWithCompletion(it) }
    }
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

    class Async(@PublishedApi internal val ios: NativeDocumentReference) : BaseDocumentReference.Async() {

        override fun setEncoded(encodedData: Any, setOptions: SetOptions): Deferred<Unit> = deferred {
            when (setOptions) {
                is SetOptions.Merge -> ios.setData(encodedData as Map<Any?, *>, true, it)
                is SetOptions.Overwrite -> ios.setData(encodedData as Map<Any?, *>, false, it)
                is SetOptions.MergeFields -> ios.setData(encodedData as Map<Any?, *>, setOptions.fields, it)
                is SetOptions.MergeFieldPaths -> ios.setData(encodedData as Map<Any?, *>, setOptions.encodedFieldPaths, it)
            }
        }

        override fun updateEncoded(encodedData: Any): Deferred<Unit> = deferred {
            ios.updateData(encodedData as Map<Any?, *>, it)
        }

        override fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>): Deferred<Unit> = deferred {
            ios.updateData(encodedFieldsAndValues.toMap(), it)
        }

        override fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Deferred<Unit> = deferred {
            ios.updateData(encodedFieldsAndValues.toMap(), it)
        }

        override fun delete() =
            deferred { ios.deleteDocumentWithCompletion(it) }
    }

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

    override val async = Async(nativeValue)

    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual suspend fun get() =
        DocumentSnapshot(awaitResult { ios.getDocumentWithCompletion(it) })

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

actual open class Query(open val ios: FIRQuery) {

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

    internal actual fun _where(field: String, equalTo: Any?) = Query(ios.queryWhereField(field, isEqualTo = equalTo!!))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(ios.queryWhereFieldPath(path.ios, isEqualTo = equalTo!!))

    internal actual fun _where(field: String, equalTo: DocumentReference) = Query(ios.queryWhereField(field, isEqualTo = equalTo.ios))
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = Query(ios.queryWhereFieldPath(path.ios, isEqualTo = equalTo.ios))

    internal actual fun _where(
        field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = Query(
        when {
            lessThan != null -> ios.queryWhereField(field, isLessThan = lessThan)
            greaterThan != null -> ios.queryWhereField(field, isGreaterThan = greaterThan)
            arrayContains != null -> ios.queryWhereField(field, arrayContains = arrayContains)
            notEqualTo != null -> ios.queryWhereField(field, isNotEqualTo = notEqualTo)
            lessThanOrEqualTo != null -> ios.queryWhereField(field, isLessThanOrEqualTo = lessThanOrEqualTo)
            greaterThanOrEqualTo != null -> ios.queryWhereField(field, isGreaterThanOrEqualTo = greaterThanOrEqualTo)
            else -> ios
        }
    )

    internal actual fun _where(
        path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = Query(
            when {
                lessThan != null -> ios.queryWhereFieldPath(path.ios, isLessThan = lessThan)
                greaterThan != null -> ios.queryWhereFieldPath(path.ios, isGreaterThan = greaterThan)
                arrayContains != null -> ios.queryWhereFieldPath(path.ios, arrayContains = arrayContains)
                notEqualTo != null -> ios.queryWhereFieldPath(path.ios, isNotEqualTo = notEqualTo)
                lessThanOrEqualTo != null -> ios.queryWhereFieldPath(path.ios, isLessThanOrEqualTo = lessThanOrEqualTo)
                greaterThanOrEqualTo != null -> ios.queryWhereFieldPath(path.ios, isGreaterThanOrEqualTo = greaterThanOrEqualTo)
                else -> ios
            }
        )

    internal actual fun _where(
        field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = Query(
            when {
                inArray != null -> ios.queryWhereField(field, `in` = inArray)
                arrayContainsAny != null -> ios.queryWhereField(field, arrayContainsAny = arrayContainsAny)
                notInArray != null -> ios.queryWhereField(field, notIn = notInArray)
                else -> ios
            }
        )

    internal actual fun _where(
        path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = Query(
            when {
                inArray != null -> ios.queryWhereFieldPath(path.ios, `in` = inArray)
                arrayContainsAny != null -> ios.queryWhereFieldPath(path.ios, arrayContainsAny = arrayContainsAny)
                notInArray != null -> ios.queryWhereFieldPath(path.ios, notIn = notInArray)
                else -> ios
            }
        )

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
actual class CollectionReference(override val ios: FIRCollectionReference) : Query(ios) {

    actual val path: String
        get() = ios.path

    actual val async = Async(ios)

    actual val document get() = DocumentReference(ios.documentWithAutoID())

    actual val parent get() = ios.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    actual suspend inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
        DocumentReference(await { ios.addDocumentWithData(encode(data, encodeSettings) as Map<Any?, *>, it) })
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
        DocumentReference(await { ios.addDocumentWithData(encode(strategy, data, encodeSettings) as Map<Any?, *>, it) })

    actual class Async(@PublishedApi internal val ios: FIRCollectionReference) {
        actual inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
            deferred { ios.addDocumentWithData(encode(data, encodeSettings) as Map<Any?, *>, it) }
                .convert(::DocumentReference)

        actual fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
            deferred { ios.addDocumentWithData(encode(strategy, data, encodeSettings) as Map<Any?, *>, it) }
                .convert(::DocumentReference)
    }
}

actual class FirebaseFirestoreException(message: String, val code: FirestoreExceptionCode) : FirebaseException(message)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
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

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val ios: FIRDocumentSnapshot) {

    actual val id get() = ios.documentID

    actual val reference get() = DocumentReference(ios.reference)

    actual inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
        return decode(value = data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } })
    }

    actual fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
        return decode(strategy, data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } }, decodeSettings)
    }

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }
        return decode(value)
    }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }
        return decode(strategy, value, decodeSettings)
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
    val callback = { result: T?, error: NSError? ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    function(callback)
    return job.await() as T
}

suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val callback = { error: NSError? ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    val result = function(callback)
    job.await()
    return result
}

@Suppress("DeferredIsResult")
@PublishedApi
internal inline fun <T> deferred(function: (callback: (NSError?) -> Unit) -> T): Deferred<T> {
    val job = CompletableDeferred<Unit>()
    val callback = { error: NSError? ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    val result = function(callback)
    return job.convert { result }
}
