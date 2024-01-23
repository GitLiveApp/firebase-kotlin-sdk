/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
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

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean = false): BaseTransaction = set(documentRef, data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)


    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, *mergeFields) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, *mergeFieldPaths) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, *mergeFields) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, *mergeFieldPaths) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): BaseTransaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseTransaction

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { shouldEncodeElementDefault = encodeDefaults }"))
    fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) = update(documentRef, data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun update(documentRef: DocumentReference, data: Any, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(documentRef, encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(documentRef, encode(strategy, data, buildSettings)!!)

    @JvmName("updateFields")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())
    @JvmName("updateFieldPaths")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseTransaction

    @PublishedApi
    internal abstract fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): BaseTransaction

    @PublishedApi
    internal abstract fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): BaseTransaction
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

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, *mergeFields) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, *mergeFieldPaths) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, *mergeFields){
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, *mergeFieldPaths) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal abstract fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): BaseWriteBatch

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { shouldEncodeElementDefault = encodeDefaults }"))
    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) = update(documentRef, data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <reified T> update(documentRef: DocumentReference, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        updateEncoded(documentRef, encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { shouldEncodeElementDefault = encodeDefaults }"))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    inline fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        updateEncoded(documentRef, encode(strategy, data, buildSettings)!!)

    @JvmName("updateField")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())
    @JvmName("updateFieldPath")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal abstract fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch

    @PublishedApi
    internal abstract fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): BaseWriteBatch

    @PublishedApi
    internal abstract fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): BaseWriteBatch
}

expect class WriteBatch : BaseWriteBatch {

    fun delete(documentRef: DocumentReference): WriteBatch
    suspend fun commit()
}

/** A class representing a platform specific Firebase DocumentReference. */
expect class NativeDocumentReference

abstract class BaseDocumentReference {

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(data, *mergeFields)  {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(data, *mergeFieldPaths)  {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, merge) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(strategy, data, merge) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(
        encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFields) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(strategy, data, *mergeFields) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(
        encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFieldPaths) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(strategy, data, *mergeFieldPaths) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = setEncoded(
        encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal abstract suspend fun setEncoded(encodedData: Any, setOptions: SetOptions)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(data) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) = update(data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <reified T> update(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(strategy, data) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(strategy, data)  {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <T> update(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(encode(strategy, data, buildSettings)!!)

    @JvmName("updateFields")
    suspend inline fun update(vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldsAndValues(encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @JvmName("updateFieldPaths")
    suspend inline fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldPathsAndValues(encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal abstract suspend fun updateEncoded(encodedData: Any)

    @PublishedApi
    internal abstract suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>)

    @PublishedApi
    internal abstract suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>)

    abstract suspend fun delete()
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

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(data) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) = add(data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <reified T> add(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = addEncoded(
        encode(data, buildSettings)!!
    )

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(strategy, data) { shouldEncodeElementDefault = encodeDefaults }"))
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = add(strategy, data) {
        shouldEncodeElementDefault = encodeDefaults
    }
    suspend inline fun <T> add(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = addEncoded(
        encode(strategy, data, buildSettings)!!
    )

    @PublishedApi
    internal abstract suspend fun addEncoded(data: Any): DocumentReference
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
    inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(field, serverTimestampBehavior), buildSettings)
    inline fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(field, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal abstract fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(encodedData(serverTimestampBehavior), buildSettings)
    inline fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, encodedData(serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal abstract fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
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
