/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.js.JsName

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
expect fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore

expect class FirebaseFirestore {
    fun collection(collectionPath: String): CollectionReference
    fun document(documentPath: String): DocumentReference
    fun collectionGroup(collectionId: String): Query
    fun batch(): WriteBatch
    fun setLoggingEnabled(loggingEnabled: Boolean)
    suspend fun clearPersistence()
    suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T
    fun useEmulator(host: String, port: Int)
    fun setSettings(persistenceEnabled: Boolean? = null, sslEnabled: Boolean? = null, host: String? = null, cacheSizeBytes: Long? = null)
    suspend fun disableNetwork()
    suspend fun enableNetwork()
}

expect class Transaction {

    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, merge: Boolean = false): Transaction
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, vararg mergeFields: String): Transaction
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): Transaction

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): Transaction
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): Transaction
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): Transaction

    fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true): Transaction
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): Transaction

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): Transaction
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): Transaction

    fun delete(documentRef: DocumentReference): Transaction
    suspend fun get(documentRef: DocumentReference): DocumentSnapshot
}

expect open class Query {
    fun limit(limit: Number): Query
    val snapshots: Flow<QuerySnapshot>
    suspend fun get(): QuerySnapshot
    internal fun _where(field: String, equalTo: Any?): Query
    internal fun _where(path: FieldPath, equalTo: Any?): Query
    internal fun _where(field: String, lessThan: Any? = null, greaterThan: Any? = null,
                        arrayContains: Any? = null, notEqualTo: Any? = null,
                        lessThanOrEqualTo: Any? = null, greaterThanOrEqualTo: Any? = null): Query
    internal fun _where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null,
                        arrayContains: Any? = null, notEqualTo: Any? = null,
                        lessThanOrEqualTo: Any? = null, greaterThanOrEqualTo: Any? = null): Query
    internal fun _where(field: String, inArray: List<Any>? = null,
                        arrayContainsAny: List<Any>? = null, notInArray: List<Any>? = null): Query
    internal fun _where(path: FieldPath, inArray: List<Any>? = null,
                        arrayContainsAny: List<Any>? = null, notInArray: List<Any>? = null): Query
    internal fun _orderBy(field: String, direction: Direction): Query
    internal fun _orderBy(field: FieldPath, direction: Direction): Query
}

private val Any?.value get() = when (this) {
    is Timestamp -> nativeValue
    is GeoPoint -> nativeValue
    is DocumentReference -> nativeValue
    else -> this
}


fun Query.where(field: String, equalTo: Any?) = _where(field, equalTo.value)
fun Query.where(path: FieldPath, equalTo: Any?) = _where(path, equalTo.value)
fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null,
                arrayContains: Any? = null, notEqualTo: Any? = null,
                lessThanOrEqualTo: Any? = null, greaterThanOrEqualTo: Any? = null) =
    _where(field, lessThan.value, greaterThan.value, arrayContains.value, notEqualTo.value, lessThanOrEqualTo.value, greaterThanOrEqualTo.value)
fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null,
                arrayContains: Any? = null, notEqualTo: Any? = null,
                lessThanOrEqualTo: Any? = null, greaterThanOrEqualTo: Any? = null) =
    _where(path, lessThan.value, greaterThan.value, arrayContains.value, notEqualTo.value, lessThanOrEqualTo.value, greaterThanOrEqualTo.value)
fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null,
                notInArray: List<Any>? = null) =
    _where(field, inArray.value, arrayContainsAny.value, notInArray.value)
fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null,
                notInArray: List<Any>? = null) =
    _where(path, inArray.value, arrayContainsAny.value, notInArray.value)
fun Query.orderBy(field: String, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)
fun Query.orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)

expect class WriteBatch {
    val async: Async

    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): WriteBatch

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch

    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true): WriteBatch
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): WriteBatch
    inline fun <reified T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()

    @Suppress("DeferredIsResult")
    class Async {
        fun commit(): Deferred<Unit>
    }
}

/** A class representing a platform specific Firebase DocumentReference. */
expect class NativeDocumentReference

/** A class representing a Firebase DocumentReference. */
@Serializable(with = DocumentReferenceSerializer::class)
expect class DocumentReference internal constructor(nativeValue: NativeDocumentReference) {
    internal val nativeValue: NativeDocumentReference

    val id: String
    val path: String
    val snapshots: Flow<DocumentSnapshot>

    val async: Async

    fun collection(collectionPath: String): CollectionReference
    suspend fun get(): DocumentSnapshot

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, merge: Boolean = false)
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, vararg mergeFields: String)
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath)

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath)

    suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean = true)
    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true)

    suspend fun update(vararg fieldsAndValues: Pair<String, Any?>)
    suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>)

    suspend fun delete()

    @Suppress("DeferredIsResult")
    class Async {
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, merge: Boolean = false): Deferred<Unit>
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): Deferred<Unit>
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): Deferred<Unit>

        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): Deferred<Unit>
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): Deferred<Unit>
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): Deferred<Unit>

        inline fun <reified T> update(data: T, encodeDefaults: Boolean = true): Deferred<Unit>
        fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): Deferred<Unit>

        fun update(vararg fieldsAndValues: Pair<String, Any?>): Deferred<Unit>
        fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>): Deferred<Unit>

        fun delete(): Deferred<Unit>
    }
}

expect class CollectionReference : Query {
    val path: String
    val async: Async

    fun document(documentPath: String): DocumentReference
    fun document(): DocumentReference
    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean = true): DocumentReference
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): DocumentReference
    @Suppress("DeferredIsResult")
    class Async {
        inline fun <reified T> add(data: T, encodeDefaults: Boolean = true): Deferred<DocumentReference>
        fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): Deferred<DocumentReference>
    }
}

expect class FirebaseFirestoreException : FirebaseException

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
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

expect enum class Direction {
    ASCENDING,
    DESCENDING
}

expect class QuerySnapshot {
    val documents: List<DocumentSnapshot>
    val documentChanges: List<DocumentChange>
    val metadata: SnapshotMetadata
}

expect enum class ChangeType {
    ADDED ,
    MODIFIED,
    REMOVED
}

expect class DocumentChange {
    val document: DocumentSnapshot
    val newIndex: Int
    val oldIndex: Int
    val type: ChangeType
}

expect class DocumentSnapshot {

    inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T
    fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T

    fun contains(field: String): Boolean

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T
    fun <T> data(strategy: DeserializationStrategy<T>,  serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T

    val exists: Boolean
    val id: String
    val reference: DocumentReference
    val metadata: SnapshotMetadata
}

enum class ServerTimestampBehavior {
    ESTIMATE,
    NONE,
    PREVIOUS
}

expect class SnapshotMetadata {
    val hasPendingWrites: Boolean
    val isFromCache: Boolean
}

expect class FieldPath(vararg fieldNames: String) {
    val documentId: FieldPath
}
