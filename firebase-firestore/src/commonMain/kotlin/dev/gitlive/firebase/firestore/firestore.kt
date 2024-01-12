/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmName

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
expect fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore

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

sealed class SetOptions {
    data object Merge : SetOptions()
    data object Overwrite : SetOptions()
    data class MergeFields(val fields: List<String>) : SetOptions()
    data class MergeFieldPaths(val fieldPaths: List<FieldPath>) : SetOptions() {
        val encodedFieldPaths = fieldPaths.map { it.encoded }
    }
}

abstract class BaseTransaction {

    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false): BaseTransaction =
        setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) = setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) = setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) = setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) = setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) = setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    protected abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseTransaction

    fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!)
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!)

    @JvmName("updateFields")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())
    @JvmName("updateFieldPaths")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())

    protected abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseTransaction
    protected abstract fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): BaseTransaction
    protected abstract fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): BaseTransaction
}

expect class Transaction : BaseTransaction {
    fun delete(documentRef: DocumentReference): Transaction
    suspend fun get(documentRef: DocumentReference): DocumentSnapshot
}

expect class NativeQuery

expect open class Query internal constructor(nativeQuery: NativeQuery) {
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

abstract class BaseWriteBatch {

    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) =
        setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) =
        setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) =
        setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) =
        setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseWriteBatch

    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        updateEncoded(documentRef, encode(data, encodeDefaults, serializersModule)!!)
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        updateEncoded(documentRef, encode(strategy, data, encodeDefaults, serializersModule)!!)

    @JvmName("updateField")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())
    @JvmName("updateFieldPath")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())

    abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch

    protected abstract fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): BaseWriteBatch
    protected abstract fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): BaseWriteBatch
}

expect class WriteBatch : BaseWriteBatch {
    val async: Async

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()

    @Suppress("DeferredIsResult")
    class Async {
        fun commit(): Deferred<Unit>
    }
}

/** A class representing a platform specific Firebase DocumentReference. */
expect class NativeDocumentReference

abstract class BaseDocumentReference {

    abstract class Async {
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) = setEncoded(encode(data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) = setEncoded(encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
        inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) = setEncoded(encode(data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) = setEncoded(
            encode(strategy, data, encodeDefaults, serializersModule)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String)= setEncoded(
            encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFields(mergeFields.asList()))
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) = setEncoded(
            encode(strategy, data, encodeDefaults, serializersModule)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

        abstract fun setEncoded(encodedData: Any, setOptions: SetOptions): Deferred<Unit>

        inline fun <reified T> update(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncoded(encode(data, encodeDefaults, serializersModule)!!)
        fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = update(encode(strategy, data, encodeDefaults, serializersModule))

        @JvmName("updateFields")
        fun update(vararg fieldsAndValues: Pair<String, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldsAndValues(encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())
        @JvmName("updateFieldPaths")
        fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = updateEncodedFieldPathsAndValues(encodeFieldAndValue(fieldsAndValues, encodeDefaults, serializersModule).orEmpty())

        abstract fun updateEncoded(encodedData: Any): Deferred<Unit>
        protected abstract fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>): Deferred<Unit>
        protected abstract fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Deferred<Unit>

        abstract fun delete(): Deferred<Unit>
    }

    abstract val async: Async

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) =
        async.set(data, encodeDefaults, serializersModule, merge).await()

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) =
        async.set(data, encodeDefaults, serializersModule, mergeFields = mergeFields).await()

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) =
        async.set(data, encodeDefaults, serializersModule, mergeFieldPaths = mergeFieldPaths).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), merge: Boolean = false) =
        async.set(strategy, data, encodeDefaults, serializersModule, merge).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFields: String) =
        async.set(strategy, data, encodeDefaults, serializersModule, mergeFields = mergeFields).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule(), vararg mergeFieldPaths: FieldPath) =
        async.set(strategy, data, encodeDefaults, serializersModule, mergeFieldPaths = mergeFieldPaths).await()

    suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.update(data, encodeDefaults, serializersModule).await()

    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.update(strategy, data, encodeDefaults, serializersModule).await()

    @JvmName("updateFields")
    suspend fun update(vararg fieldsAndValues: Pair<String, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.update(fieldsAndValues = fieldsAndValues, encodeDefaults, serializersModule).await()

    @JvmName("updateFieldPaths")
    suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.update(fieldsAndValues = fieldsAndValues, encodeDefaults, serializersModule).await()

    suspend fun delete() =
        async.delete().await()
}

/** A class representing a Firebase DocumentReference. */
@Serializable(with = DocumentReferenceSerializer::class)
expect class DocumentReference internal constructor(nativeValue: NativeDocumentReference) : BaseDocumentReference {
    internal val nativeValue: NativeDocumentReference

    val id: String
    val path: String
    val snapshots: Flow<DocumentSnapshot>
    val parent: CollectionReference
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot>

    fun collection(collectionPath: String): CollectionReference
    suspend fun get(): DocumentSnapshot
}

abstract class BaseCollectionReference(nativeQuery: NativeQuery) : Query(nativeQuery) {

    @Suppress("DeferredIsResult")
    abstract class Async {
        inline fun <reified T> add(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = addEncoded(
            encode(data, encodeDefaults, serializersModule)!!
        )
        fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) = addEncoded(
            encode(strategy, data, encodeDefaults, serializersModule)!!
        )

        abstract fun addEncoded(data: Any): Deferred<DocumentReference>
    }

    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.add(data, encodeDefaults, serializersModule).await()
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
        async.add(strategy, data, encodeDefaults, serializersModule).await()

    abstract val async: Async
}

expect class CollectionReference : BaseCollectionReference {
    val path: String
    val document: DocumentReference
    val parent: DocumentReference?

    fun document(documentPath: String): DocumentReference
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

abstract class BaseDocumentSnapshot {
    inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, serializersModule: SerializersModule = EmptySerializersModule()): T = decode(value = getEncoded(field, serverTimestampBehavior), serializersModule)
    fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, serializersModule: SerializersModule = EmptySerializersModule()): T = decode(strategy, getEncoded(field, serverTimestampBehavior), serializersModule)

    abstract fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, serializersModule: SerializersModule = EmptySerializersModule()): T = decode(encodedData(serverTimestampBehavior), serializersModule)
    fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, serializersModule: SerializersModule = EmptySerializersModule()): T = decode(strategy, encodedData(serverTimestampBehavior), serializersModule)

    abstract fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
}

expect class DocumentSnapshot : BaseDocumentSnapshot {

    fun contains(field: String): Boolean

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
    val encoded: EncodedFieldPath
}

expect class EncodedFieldPath
