/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase.firestore

import cocoapods.FirebaseFirestore.*
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.decode
import dev.teamhub.firebase.encode
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError

actual val Firebase.firestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(FIRFirestore.firestoreForApp(app.ios))

actual class FirebaseFirestore(val ios: FIRFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(ios.collectionWithPath(collectionPath))

    actual fun document(documentPath: String) = DocumentReference(ios.documentWithPath(documentPath))

    actual fun batch() = WriteBatch(ios.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        awaitResult<Any?> { ios.runTransactionWithBlock({ transaction, error -> runBlocking { Transaction(transaction!!).func() } }, it) } as T
}

actual class WriteBatch(val ios: FIRWriteBatch) {

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, mergeFieldsPaths.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, mergeFieldsPaths.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any) =
        ios.updateData(encode(data) as Map<Any?, *>, documentRef.ios).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T) =
        ios.updateData(encode(strategy, data) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
            ios.updateData(fieldsAndValues.associate { it }, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        ios.updateData(fieldsAndValues.associate { it }, documentRef.ios).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun commit() = await { ios.commitWithCompletion(it) }

}

actual class Transaction(val ios: FIRTransaction) {

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        ios.setData(encode(data)!! as Map<Any?, *>, documentRef.ios, mergeFieldsPaths.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, merge).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, mergeFields.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        ios.setData(encode(strategy, data)!! as Map<Any?, *>, documentRef.ios, mergeFieldsPaths.asList()).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any) =
        ios.updateData(encode(data) as Map<Any?, *>, documentRef.ios).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T) =
        ios.updateData(encode(strategy, data) as Map<Any?, *>, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        ios.updateData(fieldsAndValues.associate { it }, documentRef.ios).let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        ios.updateData(fieldsAndValues.associate { it }, documentRef.ios).let { this }

    actual fun delete(documentRef: DocumentReference) =
        ios.deleteDocument(documentRef.ios).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { DocumentSnapshot(ios.getDocument(documentRef.ios, it)!!) }

}

actual class DocumentReference(val ios: FIRDocumentReference) {

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    @Suppress("UNCHECKED_CAST")
    actual suspend fun set(data: Any, merge: Boolean) =
        await { ios.setData(encode(data)!! as Map<Any?, *>, merge, it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun set(data: Any, vararg mergeFields: String) =
        await { ios.setData(encode(data)!! as Map<Any?, *>, mergeFields.asList(), it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath) =
        await { ios.setData(encode(data)!! as Map<Any?, *>, mergeFieldsPaths.asList(), it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean) =
        await { ios.setData(encode(strategy, data)!! as Map<Any?, *>, merge, it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        await { ios.setData(encode(strategy, data)!! as Map<Any?, *>, mergeFields.asList(), it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        await { ios.setData(encode(strategy, data)!! as Map<Any?, *>, mergeFieldsPaths.asList(), it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun update(data: Any) =
        await { ios.updateData(encode(data) as Map<Any?, *>, it) }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T) =
        await { ios.updateData(encode(strategy, data) as Map<Any?, *>, it) }

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        await { block -> ios.updateData(fieldsAndValues.associate { it }, block) }

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        await { block -> ios.updateData(fieldsAndValues.associate { it }, block) }

    actual suspend fun delete() =
        await { ios.deleteDocumentWithCompletion(it) }

    actual suspend fun get() =
        DocumentSnapshot(awaitResult { ios.getDocumentWithCompletion(it) })

    actual val snapshots get() = callbackFlow {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { offer(DocumentSnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }
}

actual open class Query(open val ios: FIRQuery) {

    actual suspend fun get() = QuerySnapshot(awaitResult { ios.getDocumentsWithCompletion(it) })

    internal actual fun _where(field: String, equalTo: Any?) = Query(ios.queryWhereField(field, isEqualTo = equalTo!!))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(ios.queryWhereFieldPath(path, isEqualTo = equalTo!!))

    actual val snapshots get() = callbackFlow {
        println("adding snapshot listener to query ${this@Query}")
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { offer(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { ios.queryWhereField(field, isLessThan = it!!) } ?: ios).let { ios ->
            (greaterThan?.let { ios.queryWhereField(field, isGreaterThan = it!!) } ?: ios).let { ios ->
                arrayContains?.let { ios.queryWhereField(field, arrayContains = it!!) } ?: ios
            }
        }
    )

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { ios.queryWhereFieldPath(path, isLessThan = it!!) } ?: ios).let { ios ->
            (greaterThan?.let { ios.queryWhereFieldPath(path, isGreaterThan = it!!) } ?: ios).let { ios ->
                arrayContains?.let { ios.queryWhereFieldPath(path, arrayContains = it!!) } ?: ios
            }
        }
    )
}

actual class CollectionReference(override val ios: FIRCollectionReference) : Query(ios) {

    actual val path: String
        get() = ios.path

    @Suppress("UNCHECKED_CAST")
    actual suspend fun add(data: Any) =
        DocumentReference(await { ios.addDocumentWithData(encode(data) as Map<Any?, *>, it) })

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>) =
        DocumentReference(await { ios.addDocumentWithData(encode(strategy, data) as Map<Any?, *>) })
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

private fun NSError.toException() = when(domain) {
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
}

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val ios: FIRDocumentSnapshot) {

    actual val id get() = ios.documentID

    actual val reference get() = DocumentReference(ios.reference)

    actual inline fun <reified T: Any> data() = decode<T>(value = ios.data())

    actual fun <T> data(strategy: DeserializationStrategy<T>) = decode(strategy, ios.data())

    actual inline fun <reified T> get(field: String) = decode<T>(value = ios.valueForField(field))

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>) =
        decode(strategy, ios.valueForField(field))

    actual fun contains(field: String) = ios.valueForField(field) != null

    actual val exists get() = ios.exists
}

actual typealias FieldPath = FIRFieldPath

actual fun FieldPath(vararg fieldNames: String) = FIRFieldPath(fieldNames.asList())

actual object FieldValue {
    actual fun delete(): Any = FIRFieldValue.fieldValueForDelete()
    actual fun arrayUnion(vararg elements: Any): Any = FIRFieldValue.fieldValueForArrayUnion(elements.asList())
    actual fun arrayRemove(vararg elements: Any): Any = FIRFieldValue.fieldValueForArrayUnion(elements.asList())
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

suspend fun <T> awaitResult(function: (callback: (T?, NSError?) -> Unit) -> Unit): T {
    val job = CompletableDeferred<T>()
    function { result, error ->
        if(result != null) {
            job.complete(result)
        } else if(error != null) {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await()
}

suspend fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
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
