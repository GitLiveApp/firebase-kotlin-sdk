package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy

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

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, merge: Boolean = false): Transaction
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String): Transaction
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldsPaths: FieldPath): Transaction

    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, merge: Boolean = false): Transaction
    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String): Transaction
    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath): Transaction

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> update(documentRef: DocumentReference, data: T): Transaction
    inline fun <reified T> update(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>): Transaction

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): Transaction
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): Transaction

    fun delete(documentRef: DocumentReference): Transaction
    suspend fun get(documentRef: DocumentReference): DocumentSnapshot
}

//data class FirebaseFirestoreSettings(
//    val persistenceEnabled: Boolean = true
//)

expect open class Query {
    internal fun _where(field: String, equalTo: Any?): Query
    internal fun _where(path: FieldPath, equalTo: Any?): Query
    internal fun _where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query
    internal fun _where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query
    val snapshots: Flow<QuerySnapshot>
    suspend fun get(): QuerySnapshot
}

fun Query.where(field: String, equalTo: Any?) = _where(field, equalTo)
fun Query.where(path: FieldPath, equalTo: Any?) = _where(path, equalTo)
fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = _where(field, lessThan, greaterThan, arrayContains)
fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = _where(path, lessThan, greaterThan, arrayContains)


expect class WriteBatch {
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, merge: Boolean = false): WriteBatch
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String): WriteBatch
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldsPaths: FieldPath): WriteBatch

    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, merge: Boolean = false): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath): WriteBatch

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> update(documentRef: DocumentReference, data: T): WriteBatch
    inline fun <reified T> update(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>): WriteBatch

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()
}

expect class DocumentReference {

    val id: String
    val snapshots: Flow<DocumentSnapshot>
    suspend fun get(): DocumentSnapshot

    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> set(data: T, merge: Boolean = false)
    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> set(data: T, vararg mergeFields: String)
    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> set(data: T, vararg mergeFieldsPaths: FieldPath)

    suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, merge: Boolean = false)
    suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String)
    suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath)

    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> update(data: T)
    suspend inline fun <reified T> update(data: T, strategy: SerializationStrategy<T>)

    suspend fun update(vararg fieldsAndValues: Pair<String, Any?>)
    suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>)

    suspend fun delete()
}

expect class CollectionReference : Query {
    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> add(data: T): DocumentReference
    suspend inline fun <reified T> add(data: T, strategy: SerializationStrategy<T>): DocumentReference
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

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> get(field: String): T
    inline fun <reified T> get(field: String, strategy: DeserializationStrategy<T>): T

    fun contains(field: String): Boolean

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> data(): T?
    inline fun <reified T> data(strategy: DeserializationStrategy<T>): T?

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


