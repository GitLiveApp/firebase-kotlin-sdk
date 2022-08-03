/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.*
import cocoapods.FirebaseFirestore.FIRDocumentChangeType.*
import dev.gitlive.firebase.*
import kotlin.native.concurrent.freeze
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

actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore {
    return FirebaseFirestore(FIRFirestore.firestoreForApp(app.ios))
}

@Suppress("UNCHECKED_CAST")
actual class FirebaseFirestore(val ios: FIRFirestore) {

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
actual class WriteBatch(val ios: FIRWriteBatch) {

    actual val async = Async(ios)

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFieldPaths.map { it.ios }).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFieldPaths.map { it.ios }).let { this }

    actual fun <T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        merge: Boolean,
        vararg fieldsAndValues: Pair<String, Any?>
    ): WriteBatch {
        val serializedItem = encode(strategy, data, encodeDefaults) as Map<Any?, *>
        val serializedFieldAndValues = fieldsAndValues.associate { (field, value) ->
            field to encode(value, encodeDefaults)
        }

        val result = serializedItem + serializedFieldAndValues
        ios.setData(result as Map<Any?, *>, documentRef.ios, merge)
        return this
    }

    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        ios.updateData(encode(data, encodeDefaults) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        ios.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
            ios.updateData(
                fieldsAndValues.associate { (field, value) -> field to encode(value, true) },
                documentRef.ios
            ).let { this }

    actual inline fun <reified T> update(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        vararg fieldsAndValues: Pair<String, Any?>
    ): WriteBatch {
        val serializedItem = encode(strategy, data, encodeDefaults) as Map<Any?, *>
        val serializedFieldAndValues = fieldsAndValues.associate { (field, value) ->
            field to encode(value, encodeDefaults)
        }

        val result = serializedItem + serializedFieldAndValues
        return ios.updateData(result as Map<Any?, *>, documentRef.ios).let { this }
    }

    actual inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        ios.updateData(
            fieldsAndValues.associate { (path, value) -> path.ios to encode(value, true) },
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
actual class Transaction(val ios: FIRTransaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFieldPaths.map { it.ios }).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, documentRef.ios, mergeFieldPaths.map { it.ios }).let { this }

    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        ios.updateData(encode(data, encodeDefaults) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        ios.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        ios.updateData(
            fieldsAndValues.associate { (field, value) -> field to encode(value, true) },
            documentRef.ios
        ).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        ios.updateData(
            fieldsAndValues.associate { (path, value) -> path.ios to encode(value, true) },
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
actual class DocumentReference actual constructor(internal actual val nativeValue: NativeDocumentReference) {
    val ios: NativeDocumentReference = nativeValue

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val async = Async(nativeValue)

    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) =
        async.set(data, encodeDefaults, merge).await()

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        async.set(data, encodeDefaults, mergeFields = mergeFields).await()

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        async.set(data, encodeDefaults, mergeFieldPaths = mergeFieldPaths).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        async.set(strategy, data, encodeDefaults, merge).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        async.set(strategy, data, encodeDefaults, mergeFields = mergeFields).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        async.set(strategy, data, encodeDefaults, mergeFieldPaths = mergeFieldPaths).await()

    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        async.update(data, encodeDefaults).await()

    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        async.update(strategy, data, encodeDefaults).await()

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        async.update(fieldsAndValues = fieldsAndValues).await()

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        async.update(fieldsAndValues = fieldsAndValues).await()

    actual suspend fun delete() =
        async.delete().await()

    actual suspend fun get() =
        DocumentSnapshot(awaitResult { ios.getDocumentWithCompletion(it) })

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val callback = { snapshot: FIRDocumentSnapshot?, error: NSError? ->
            snapshot?.let { safeOffer(DocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
            Unit
        }.freeze()
        val listener = ios.addSnapshotListener(callback)
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is DocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual class Async(@PublishedApi internal val ios: NativeDocumentReference) {
        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) =
            deferred { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
            deferred { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
            deferred { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.map { it.ios }, it) }

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
            deferred { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
            deferred { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
            deferred { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.map { it.ios }, it) }

        actual inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
            deferred { ios.updateData(encode(data, encodeDefaults) as Map<Any?, *>, it) }

        actual fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
            deferred { ios.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) }

        actual fun update(vararg fieldsAndValues: Pair<String, Any?>) =
            deferred {
                ios.updateData(
                    fieldsAndValues.associate { (field, value) -> field to encode(value, true) },
                    it
                )
            }

        actual fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
            deferred {
                ios.updateData(
                    fieldsAndValues.associate { (path, value) -> path.ios to encode(value, true) },
                    it
                )
            }

        actual fun delete() =
            deferred { ios.deleteDocumentWithCompletion(it) }
    }
}

actual open class Query(open val ios: FIRQuery) {

    actual suspend fun get() = QuerySnapshot(awaitResult { ios.getDocumentsWithCompletion(it) })

    actual fun limit(limit: Number) = Query(ios.queryLimitedTo(limit.toLong()))

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val callback = { snapshot: FIRQuerySnapshot?, error: NSError? ->
            snapshot?.let { safeOffer(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
            Unit
        }.freeze()
        val listener = ios.addSnapshotListener(callback)
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, equalTo: Any?) = Query(ios.queryWhereField(field, isEqualTo = equalTo!!))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(ios.queryWhereFieldPath(path.ios, isEqualTo = equalTo!!))

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
}

@Suppress("UNCHECKED_CAST")
actual class CollectionReference(override val ios: FIRCollectionReference) : Query(ios) {

    actual val path: String
        get() = ios.path

    actual val async = Async(ios)

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    actual fun document() = DocumentReference(ios.documentWithAutoID())

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        DocumentReference(await { ios.addDocumentWithData(encode(data, encodeDefaults) as Map<Any?, *>, it) })
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        DocumentReference(await { ios.addDocumentWithData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) })

    actual class Async(@PublishedApi internal val ios: FIRCollectionReference) {
        actual inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
            deferred { ios.addDocumentWithData(encode(data, encodeDefaults) as Map<Any?, *>, it) }
                .convert(::DocumentReference)

        actual fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
            deferred { ios.addDocumentWithData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) }
                .convert(::DocumentReference)
    }
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

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val ios: FIRDocumentSnapshot) {

    actual val id get() = ios.documentID

    actual val reference get() = DocumentReference(ios.reference)

    actual inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
        return decode(value = data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } })
    }

    actual fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T {
        val data = ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
        return decode(strategy, data?.mapValues { (_, value) -> value?.takeIf { it !is NSNull } })
    }

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }
        return decode(value)
    }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T {
        val value = ios.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }
        return decode(strategy, value)
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
    override fun equals(other: Any?): Boolean = other is FieldPath && ios == other.ios
    override fun hashCode(): Int = ios.hashCode()
    override fun toString(): String = ios.toString()
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
    val callback = { result: T?, error: NSError? ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }.freeze()
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
    }.freeze()
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
    }.freeze()
    val result = function(callback)
    return job.convert { result }
}
