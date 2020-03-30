/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

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
    fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false): Transaction
    @ImplicitReflectionSerializer
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String): Transaction
    @ImplicitReflectionSerializer
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath): Transaction

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false): Transaction
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String): Transaction
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath): Transaction

    @ImplicitReflectionSerializer
    fun update(documentRef: DocumentReference, data: Any): Transaction
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T): Transaction

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
    fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false): WriteBatch
    @ImplicitReflectionSerializer
    fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String): WriteBatch
    @ImplicitReflectionSerializer
    fun  set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath): WriteBatch

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath): WriteBatch

    @ImplicitReflectionSerializer
    fun  update(documentRef: DocumentReference, data: Any): WriteBatch
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T): WriteBatch

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()
}

expect class DocumentReference {

    val id: String
    val path: String
    val snapshots: Flow<DocumentSnapshot>
    suspend fun get(): DocumentSnapshot

    @ImplicitReflectionSerializer
    suspend fun set(data: Any, merge: Boolean = false)
    @ImplicitReflectionSerializer
    suspend fun  set(data: Any, vararg mergeFields: String)
    @ImplicitReflectionSerializer
    suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath)

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean = false)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath)

    @ImplicitReflectionSerializer
    suspend fun update(data: Any)
    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T)

    suspend fun update(vararg fieldsAndValues: Pair<String, Any?>)
    suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>)

    suspend fun delete()
}

expect class CollectionReference : Query {
    val path: String
    @ImplicitReflectionSerializer
    suspend fun add(data: Any): DocumentReference
    suspend fun <T> add(data: T, strategy: SerializationStrategy<T>): DocumentReference
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
    inline fun <reified T> get(field: String): T
    fun <T> get(field: String, strategy: DeserializationStrategy<T>): T

    fun contains(field: String): Boolean

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> data(): T
    fun <T> data(strategy: DeserializationStrategy<T>): T

    val exists: Boolean
    val id: String
    val reference: DocumentReference
}

expect class FieldPath

expect fun FieldPath(vararg fieldNames: String): FieldPath

expect object FieldValue {
    fun delete(): Any
    fun arrayUnion(vararg elements: Any): Any
    fun arrayRemove(vararg elements: Any): Any
}


