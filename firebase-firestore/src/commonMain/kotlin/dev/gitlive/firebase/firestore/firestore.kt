/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
public expect fun Firebase.firestore(app: FirebaseApp, databaseId: String? = null): FirebaseFirestore

expect class FirebaseFirestore {
    fun collection(collectionPath: String): CollectionReference
    fun collectionGroup(collectionId: String): Query
    fun document(documentPath: String): DocumentReference
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
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot>
    suspend fun get(): QuerySnapshot

    internal fun where(filter: Filter): Query

    internal fun _orderBy(field: String, direction: Direction): Query
    internal fun _orderBy(field: FieldPath, direction: Direction): Query

    internal fun _startAfter(document: DocumentSnapshot): Query
    internal fun _startAfter(vararg fieldValues: Any): Query
    internal fun _startAt(document: DocumentSnapshot): Query
    internal fun _startAt(vararg fieldValues: Any): Query

    internal fun _endBefore(document: DocumentSnapshot): Query
    internal fun _endBefore(vararg fieldValues: Any): Query
    internal fun _endAt(document: DocumentSnapshot): Query
    internal fun _endAt(vararg fieldValues: Any): Query
}

fun Query.where(builder: FilterBuilder.() -> Filter?) = builder(FilterBuilder())?.let { where(it) } ?: this

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { field equalTo equalTo }", "dev.gitlive.firebase.firestore"))
fun Query.where(field: String, equalTo: Any?) = where {
    field equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { path equalTo equalTo }", "dev.gitlive.firebase.firestore"))
fun Query.where(path: FieldPath, equalTo: Any?) = where {
    path equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = where {
    all(
        *listOfNotNull(
            lessThan?.let { field lessThan it },
            greaterThan?.let { field greaterThan it },
            arrayContains?.let { field contains it }
        ).toTypedArray()
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = where {
    all(
        *listOfNotNull(
            lessThan?.let { path lessThan it },
            greaterThan?.let { path greaterThan it },
            arrayContains?.let { path contains it }
        ).toTypedArray()
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null) = where {
    all(
        *listOfNotNull(
            inArray?.let { field inArray it },
            arrayContainsAny?.let { field containsAny  it },
        ).toTypedArray()
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null) = where {
    all(
        *listOfNotNull(
            inArray?.let { path inArray it },
            arrayContainsAny?.let { path containsAny  it },
        ).toTypedArray()
    )
}

fun Query.orderBy(field: String, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)
fun Query.orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)

fun Query.startAfter(document: DocumentSnapshot) = _startAfter(document)
fun Query.startAfter(vararg fieldValues: Any) = _startAfter(*(fieldValues.mapNotNull { it.safeValue }.toTypedArray()))
fun Query.startAt(document: DocumentSnapshot) = _startAt(document)
fun Query.startAt(vararg fieldValues: Any) = _startAt(*(fieldValues.mapNotNull { it.safeValue }.toTypedArray()))

fun Query.endBefore(document: DocumentSnapshot) = _endBefore(document)
fun Query.endBefore(vararg fieldValues: Any) = _endBefore(*(fieldValues.mapNotNull { it.safeValue }.toTypedArray()))
fun Query.endAt(document: DocumentSnapshot) = _endAt(document)
fun Query.endAt(vararg fieldValues: Any) = _endAt(*(fieldValues.mapNotNull { it.safeValue }.toTypedArray()))

internal val Any.safeValue: Any get() = when (this) {
    is Timestamp -> nativeValue
    is GeoPoint -> nativeValue
    is DocumentReference -> nativeValue
    is Map<*, *> -> this.mapNotNull { (key, value) -> key?.let { it.safeValue to value?.safeValue } }
    is Collection<*> -> this.mapNotNull { it?.safeValue }
    else -> this
}

expect class WriteBatch {
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): WriteBatch
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): WriteBatch

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String): WriteBatch
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath): WriteBatch

    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true): WriteBatch
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): WriteBatch

    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()
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
    val parent: CollectionReference
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot>

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
}

/**
 * A serializer for [DocumentReference]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms.
 */
object DocumentReferenceSerializer : KSerializer<DocumentReference> by SpecialValueSerializer(
    serialName = "DocumentReference",
    toNativeValue = DocumentReference::nativeValue,
    fromNativeValue = { value ->
        when (value) {
            is NativeDocumentReference -> DocumentReference(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)

expect class CollectionReference : Query {
    val path: String
    val document: DocumentReference
    val parent: DocumentReference?

    fun document(documentPath: String): DocumentReference
    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean = true): DocumentReference
    @Deprecated("This will be replaced with add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true)")
    suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean = true): DocumentReference
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true): DocumentReference
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

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
expect class FieldValue internal constructor(nativeValue: Any) {
    internal val nativeValue: Any

    companion object {
        val serverTimestamp: FieldValue
        val delete: FieldValue
        fun increment(value: Int): FieldValue
        fun arrayUnion(vararg elements: Any): FieldValue
        fun arrayRemove(vararg elements: Any): FieldValue
    }
}

/** A serializer for [FieldValue]. Must be used in conjunction with [FirebaseEncoder]. */
object FieldValueSerializer : KSerializer<FieldValue> by SpecialValueSerializer(
    serialName = "FieldValue",
    toNativeValue = FieldValue::nativeValue,
    fromNativeValue = { raw ->
        raw?.let(::FieldValue) ?: throw SerializationException("Cannot deserialize $raw")
    }
)
