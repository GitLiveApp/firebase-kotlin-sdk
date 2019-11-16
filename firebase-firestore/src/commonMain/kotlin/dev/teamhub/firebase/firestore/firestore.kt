package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
expect fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore

expect class FirebaseFirestore {
//    var settings: FirebaseFirestoreSettings
    fun collection(collectionPath: String): CollectionReference
    fun document(documentPath: String): DocumentReference
    fun batch(): WriteBatch
    fun setLoggingEnabled(loggingEnabled: Boolean)
    suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T
}

expect class Transaction {
    fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false): Transaction
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String): Transaction
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath): Transaction
    fun update(documentRef: DocumentReference, data: Map<String, Any>): Transaction
    fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): Transaction
    fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): Transaction
    fun delete(documentRef: DocumentReference): Transaction
    suspend fun get(documentRef: DocumentReference): DocumentSnapshot
}

data class FirebaseFirestoreSettings(
    val persistenceEnabled: Boolean = true
)

expect open class Query {
    fun whereEqualTo(field: String, value: Any?): Query
    fun whereEqualTo(path: FieldPath, value: Any?): Query
    fun whereLessThan(field: String, value: Any): Query
    fun whereLessThan(path: FieldPath, value: Any): Query
    fun whereGreaterThan(field: String, value: Any): Query
    fun whereGreaterThan(path: FieldPath, value: Any): Query
    fun whereArrayContains(field: String, value: Any): Query
    fun whereArrayContains(path: FieldPath, value: Any): Query
    val snapshots: Flow<QuerySnapshot>
    suspend fun get(): QuerySnapshot
}

expect class WriteBatch {
    fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false): WriteBatch
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String): WriteBatch
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath): WriteBatch
    fun update(documentRef: DocumentReference, data: Map<String, Any>): WriteBatch
    fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch
    fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch
    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()
}

expect class DocumentReference {
    val id: String
    val snapshots: Flow<DocumentSnapshot>
    suspend fun get(): DocumentSnapshot
    suspend fun set(data: Any, merge: Boolean = false)
    suspend fun set(data: Any, vararg mergeFields: String)
    suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath)
    suspend fun update(data: Map<String, Any>)
    suspend fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any)
    suspend fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any)
    suspend fun delete()
}

expect class CollectionReference : Query {
    suspend fun add(data: Map<String, Any>): DocumentReference
    suspend fun add(pojo: Any): DocumentReference
}

expect class FirebaseFirestoreException : FirebaseException

expect val FirebaseFirestoreException.code: FirestoreExceptionCode

expect enum class FirestoreExceptionCode {
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

expect class QuerySnapshot {
    val documents: List<DocumentSnapshot>
}

expect class DocumentSnapshot {
    /**
     * Returns the value at the field, converted to a POJO, or null if the field or document doesn't
     * exist.
     *
     * @param field The path to the field.
     * @param T The type to convert the field value to.
     * @return The value at the given field or null.
     */
    inline fun <reified T: Any> get(field: String): T?
    inline fun <reified T: Any> getList(field: String): List<T>?
    fun contains(field: String): Boolean
    /**
     * Returns the contents of the document converted to a POJO or null if the document doesn't exist.
     *
     * @param T The type of the object to create.
     * @return The contents of the document in an object of type T or null if the document doesn't
     *     exist.
     */
    inline fun <reified T: Any> data(): T?
    val exists: Boolean
    val id: String
    val reference: DocumentReference
}

expect class FieldPath

expect fun FieldPath(vararg fieldNames: String): FieldPath

expect object FieldValue {
    fun delete(): FieldValueImpl
    fun arrayUnion(vararg elements: Any): FieldValueImpl
    fun arrayRemove(vararg elements: Any): FieldValueImpl
}

expect abstract class FieldValueImpl


