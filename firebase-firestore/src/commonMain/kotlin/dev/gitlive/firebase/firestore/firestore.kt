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

    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean = false): BaseTransaction = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    protected abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseTransaction

    fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) = update(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    fun update(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncoded(documentRef, encode(data, encodeSettings)!!)
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncoded(documentRef, encode(strategy, data, encodeSettings)!!)

    @JvmName("updateFields")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())
    @JvmName("updateFieldPaths")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())

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
    internal fun _where(field: String, equalTo: Any?): Query
    internal fun _where(path: FieldPath, equalTo: Any?): Query
    internal fun _where(field: String, equalTo: DocumentReference): Query
    internal fun _where(path: FieldPath, equalTo: DocumentReference): Query
    internal fun _where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query
    internal fun _where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query
    internal fun _where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query
    internal fun _where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query

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

/** @return a native value of a wrapper or self. */
private val Any.value get() = when (this) {
    is Timestamp -> nativeValue
    is GeoPoint -> nativeValue
    is DocumentReference -> nativeValue
    else -> this
}

fun Query.where(field: String, equalTo: Any?) = _where(field, equalTo?.value)
fun Query.where(path: FieldPath, equalTo: Any?) = _where(path, equalTo?.value)
fun Query.where(field: String, equalTo: DocumentReference) = _where(field, equalTo.value)
fun Query.where(path: FieldPath, equalTo: DocumentReference) = _where(path, equalTo.value)
fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = _where(field, lessThan?.value, greaterThan?.value, arrayContains?.value)
fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null) = _where(path, lessThan?.value, greaterThan?.value, arrayContains?.value)
fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null) = _where(field, inArray?.value, arrayContainsAny?.value)
fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null) = _where(path, inArray?.value, arrayContainsAny?.value)

fun Query.orderBy(field: String, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)
fun Query.orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING) = _orderBy(field, direction)

fun Query.startAfter(document: DocumentSnapshot) = _startAfter(document)
fun Query.startAfter(vararg fieldValues: Any) = _startAfter(*(fieldValues.map { it.value }.toTypedArray()))
fun Query.startAt(document: DocumentSnapshot) = _startAt(document)
fun Query.startAt(vararg fieldValues: Any) = _startAt(*(fieldValues.map { it.value }.toTypedArray()))

fun Query.endBefore(document: DocumentSnapshot) = _endBefore(document)
fun Query.endBefore(vararg fieldValues: Any) = _endBefore(*(fieldValues.map { it.value }.toTypedArray()))
fun Query.endAt(document: DocumentSnapshot) = _endAt(document)
fun Query.endAt(vararg fieldValues: Any) = _endAt(*(fieldValues.map { it.value }.toTypedArray()))

abstract class BaseWriteBatch {

    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseWriteBatch

    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) = update(documentRef, data, EncodeSettings(encodeDefaults))
    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        updateEncoded(documentRef, encode(data, encodeSettings)!!)
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        updateEncoded(documentRef, encode(strategy, data, encodeSettings)!!)

    @JvmName("updateField")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())
    @JvmName("updateFieldPath")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())

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
        inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) = setEncoded(encode(data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
        inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) = setEncoded(encode(data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
        inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) = setEncoded(encode(data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) = setEncoded(
            encode(strategy, data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String)= setEncoded(
            encode(strategy, data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
        fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) = setEncoded(
            encode(strategy, data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

        abstract fun setEncoded(encodedData: Any, setOptions: SetOptions): Deferred<Unit>

        inline fun <reified T> update(data: T, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncoded(encode(data, encodeSettings)!!)
        fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) = update(encode(strategy, data, encodeSettings))

        @JvmName("updateFields")
        fun update(vararg fieldsAndValues: Pair<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldsAndValues(encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())
        @JvmName("updateFieldPaths")
        fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldPathsAndValues(encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())

        abstract fun updateEncoded(encodedData: Any): Deferred<Unit>
        protected abstract fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>): Deferred<Unit>
        protected abstract fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Deferred<Unit>

        abstract fun delete(): Deferred<Unit>
    }

    abstract val async: Async

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        async.set(data, encodeSettings, merge).await()

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        async.set(data, encodeSettings, mergeFields = mergeFields).await()

    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        async.set(data, encodeSettings, mergeFieldPaths = mergeFieldPaths).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), merge)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        async.set(strategy, data, encodeSettings, merge).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFields)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        async.set(strategy, data, encodeSettings, mergeFields = mergeFields).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults), *mergeFieldPaths)
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        async.set(strategy, data, encodeSettings, mergeFieldPaths = mergeFieldPaths).await()

    suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) = update(data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend inline fun <reified T> update(data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        async.update(data, encodeSettings).await()

    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        async.update(strategy, data, encodeSettings).await()

    @JvmName("updateFields")
    suspend fun update(vararg fieldsAndValues: Pair<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) =
        async.update(fieldsAndValues = fieldsAndValues, encodeSettings).await()

    @JvmName("updateFieldPaths")
    suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) =
        async.update(fieldsAndValues = fieldsAndValues, encodeSettings).await()

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
        inline fun <reified T> add(data: T, encodeSettings: EncodeSettings = EncodeSettings()) = addEncoded(
            encode(data, encodeSettings)!!
        )
        fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) = addEncoded(
            encode(strategy, data, encodeSettings)!!
        )

        abstract fun addEncoded(data: Any): Deferred<DocumentReference>
    }

    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) = add(data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend inline fun <reified T> add(data: T, encodeSettings: EncodeSettings = EncodeSettings()) = async.add(data, encodeSettings).await()
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = add(strategy, data, EncodeSettings(shouldEncodeElementDefault = encodeDefaults))
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) = async.add(strategy, data, encodeSettings).await()

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
    inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, decodeSettings: DecodeSettings = DecodeSettings()): T = decode(value = getEncoded(field, serverTimestampBehavior), decodeSettings)
    fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, decodeSettings: DecodeSettings = DecodeSettings()): T = decode(strategy, getEncoded(field, serverTimestampBehavior), decodeSettings)

    abstract fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, decodeSettings: DecodeSettings = DecodeSettings()): T = decode(encodedData(serverTimestampBehavior), decodeSettings)
    fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, decodeSettings: DecodeSettings = DecodeSettings()): T = decode(strategy, encodedData(serverTimestampBehavior), decodeSettings)

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
