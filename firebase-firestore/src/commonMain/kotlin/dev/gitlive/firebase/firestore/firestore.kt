/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.encode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.jvm.JvmName

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
expect fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore

sealed class LocalCacheSettings {
    data class Persistent(val sizeBytes: Long? = null) : LocalCacheSettings()
    data class Memory(val garbaseCollectorSettings: GarbageCollectorSettings) : LocalCacheSettings() {
        sealed class GarbageCollectorSettings {
            object Eager : GarbageCollectorSettings()
            data class LRUGC(val sizeBytes: Long? = null) : GarbageCollectorSettings()
        }
    }
}

expect class FirebaseFirestore {

    class Settings {

        companion object {
            fun create(sslEnabled: Boolean? = null, host: String? = null, cacheSettings: LocalCacheSettings? = null): Settings
        }

        val sslEnabled: Boolean?
        val host: String?
        val cacheSettings: LocalCacheSettings?
    }

    fun collection(collectionPath: String): CollectionReference
    fun document(documentPath: String): DocumentReference
    fun collectionGroup(collectionId: String): Query
    fun batch(): WriteBatch
    fun setLoggingEnabled(loggingEnabled: Boolean)
    suspend fun clearPersistence()
    suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T
    fun useEmulator(host: String, port: Int)
    fun setSettings(settings: Settings)
    fun updateSettings(settings: Settings)
    suspend fun disableNetwork()
    suspend fun enableNetwork()
}

fun FirebaseFirestore.setSettings(
    sslEnabled: Boolean? = null,
    host: String? = null,
    cacheSettings: LocalCacheSettings? = null
) = FirebaseFirestore.Settings.create(sslEnabled, host, cacheSettings)

sealed class SetOptions {
    object Merge : SetOptions()
    object Overwrite : SetOptions()
    data class MergeFields(val fields: List<String>) : SetOptions()
    data class MergeFieldPaths(val fieldPaths: List<FieldPath>) : SetOptions() {
        val encodedFieldPaths = fieldPaths.map { it.encoded }
    }
}

abstract class BaseTransaction {

    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun set(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath): BaseTransaction = setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath): BaseTransaction = setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    protected abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseTransaction

    fun update(documentRef: DocumentReference, data: Any, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncoded(documentRef, encode(data, encodeSettings)!!)
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

expect open class Query {
    fun limit(limit: Number): Query
    val snapshots: Flow<QuerySnapshot>
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot>
    suspend fun get(): QuerySnapshot
    internal fun _where(field: String, equalTo: Any?): Query
    internal fun _where(path: FieldPath, equalTo: Any?): Query
    internal fun _where(field: String, equalTo: DocumentReference): Query
    internal fun _where(path: FieldPath, equalTo: DocumentReference): Query
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

    internal fun _startAfter(document: DocumentSnapshot): Query
    internal fun _startAfter(vararg fieldValues: Any): Query
    internal fun _startAt(document: DocumentSnapshot): Query
    internal fun _startAt(vararg fieldValues: Any): Query

    internal fun _endBefore(document: DocumentSnapshot): Query
    internal fun _endBefore(vararg fieldValues: Any): Query
    internal fun _endAt(document: DocumentSnapshot): Query
    internal fun _endAt(vararg fieldValues: Any): Query
}

private val Any?.value get() = when (this) {
    is Timestamp -> nativeValue
    is GeoPoint -> nativeValue
    is DocumentReference -> nativeValue
    else -> this
}

fun Query.where(field: String, equalTo: Any?) = _where(field, equalTo.value)
fun Query.where(path: FieldPath, equalTo: Any?) = _where(path, equalTo.value)
fun Query.where(field: String, equalTo: DocumentReference) = _where(field, equalTo)
fun Query.where(path: FieldPath, equalTo: DocumentReference) = _where(path, equalTo)
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

fun Query.startAfter(document: DocumentSnapshot) = _startAfter(document)
fun Query.startAfter(vararg fieldValues: Any) = _startAfter(*(fieldValues.mapNotNull { it.value }.toTypedArray()))
fun Query.startAt(document: DocumentSnapshot) = _startAt(document)
fun Query.startAt(vararg fieldValues: Any) = _startAt(*(fieldValues.mapNotNull { it.value }.toTypedArray()))

fun Query.endBefore(document: DocumentSnapshot) = _endBefore(document)
fun Query.endBefore(vararg fieldValues: Any) = _endBefore(*(fieldValues.mapNotNull { it.value }.toTypedArray()))
fun Query.endAt(document: DocumentSnapshot) = _endAt(document)
fun Query.endAt(vararg fieldValues: Any) = _endAt(*(fieldValues.mapNotNull { it.value }.toTypedArray()))

abstract class BaseWriteBatch {
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String)=
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFields(mergeFields.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false, vararg fieldsAndValues: Pair<String, Any?>) =
        setEncoded(documentRef, encode(strategy, data, encodeSettings)!!, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty(), merge)

    abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseWriteBatch
    abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, encodedFieldsAndValues: List<Pair<String, Any?>>, merge: Boolean): BaseWriteBatch

    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        updateEncoded(documentRef, encode(data, encodeSettings)!!)
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        updateEncoded(documentRef, encode(strategy, data, encodeSettings)!!)
    inline fun <reified T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg fieldsAndValues: Pair<String, Any?>) =
        updateEncoded(documentRef, encode(strategy, data, encodeSettings)!!, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())

    @JvmName("updateField")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())
    @JvmName("updateFieldPath")
    fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, encodeSettings: EncodeSettings = EncodeSettings()) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, encodeSettings).orEmpty())

    abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch
    abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any, encodedFieldsAndValues: List<Pair<String, Any?>>): BaseWriteBatch

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

    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        async.set(data, encodeSettings, merge).await()

    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        async.set(data, encodeSettings, mergeFields = mergeFields).await()

    suspend inline fun <reified T> set(data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        async.set(data, encodeSettings, mergeFieldPaths = mergeFieldPaths).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), merge: Boolean = false) =
        async.set(strategy, data, encodeSettings, merge).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFields: String) =
        async.set(strategy, data, encodeSettings, mergeFields = mergeFields).await()

    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings(), vararg mergeFieldPaths: FieldPath) =
        async.set(strategy, data, encodeSettings, mergeFieldPaths = mergeFieldPaths).await()

    @Suppress("UNCHECKED_CAST")
    suspend inline fun <reified T> update(data: T, encodeSettings: EncodeSettings = EncodeSettings()) =
        async.update(data, encodeSettings).await()

    @Suppress("UNCHECKED_CAST")
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

expect class CollectionReference : Query {
    val path: String
    val async: Async
    val document: DocumentReference
    val parent: DocumentReference?

    fun document(documentPath: String): DocumentReference
    suspend inline fun <reified T> add(data: T, encodeSettings: EncodeSettings = EncodeSettings()): DocumentReference
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()): DocumentReference
    @Suppress("DeferredIsResult")
    class Async {
        inline fun <reified T> add(data: T, encodeSettings: EncodeSettings = EncodeSettings()): Deferred<DocumentReference>
        fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings = EncodeSettings()): Deferred<DocumentReference>
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
    fun <T> get(field: String, strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings = DecodeSettings(), serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T

    fun contains(field: String): Boolean

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T
    fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings = DecodeSettings(), serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): T

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
