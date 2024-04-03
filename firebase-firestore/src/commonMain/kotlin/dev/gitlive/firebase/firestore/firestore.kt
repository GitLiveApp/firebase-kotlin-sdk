/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
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
            data object Eager : GarbageCollectorSettings()
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
    fun collectionGroup(collectionId: String): Query
    fun document(documentPath: String): DocumentReference
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

@Deprecated("Use dev.gitlive.firebase.firestore instead", replaceWith = ReplaceWith("setSettings(FirebaseFirestore.Settings.create())"))
fun FirebaseFirestore.setSettings(
    persistenceEnabled: Boolean? = null,
    sslEnabled: Boolean? = null,
    host: String? = null,
    cacheSizeBytes: Long? = null,
) = setSettings(
    FirebaseFirestore.Settings.create(
        sslEnabled,
        host,
        persistenceEnabled?.let { persistence ->
            if (persistence) {
                LocalCacheSettings.Persistent(cacheSizeBytes)
            } else {
                LocalCacheSettings.Memory(
                    cacheSizeBytes?.let {
                        LocalCacheSettings.Memory.GarbageCollectorSettings.LRUGC(it)
                    } ?: LocalCacheSettings.Memory.GarbageCollectorSettings.Eager
                )
            }
        }
    )
)

@PublishedApi
internal sealed class SetOptions {
    data object Merge : SetOptions()
    data object Overwrite : SetOptions()
    data class MergeFields(val fields: List<String>) : SetOptions()
    data class MergeFieldPaths(val fieldPaths: List<FieldPath>) : SetOptions() {
        val encodedFieldPaths = fieldPaths.map { it.encoded }
    }
}

@PublishedApi
internal expect class NativeTransaction {
    fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): NativeTransaction
    fun updateEncoded(documentRef: DocumentReference, encodedData: Any): NativeTransaction
    fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): NativeTransaction
    fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): NativeTransaction
    fun delete(documentRef: DocumentReference): NativeTransaction
    suspend fun get(documentRef: DocumentReference): NativeDocumentSnapshot
}

data class Transaction internal constructor(@PublishedApi internal val native: NativeTransaction) {

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean = false): Transaction = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)


    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): Transaction = Transaction(native.setEncoded(documentRef, encodedData, setOptions))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun update(documentRef: DocumentReference, data: Any, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(documentRef, encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncoded(documentRef, encode(strategy, data, buildSettings)!!)

    @JvmName("updateFields")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())
    @JvmName("updateFieldPaths")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: Any): Transaction = Transaction(native.updateEncoded(documentRef, encodedData))

    @PublishedApi
    internal fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): Transaction = Transaction(native.updateEncodedFieldsAndValues(documentRef, encodedFieldsAndValues))

    @PublishedApi
    internal fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Transaction = Transaction(native.updateEncodedFieldPathsAndValues(documentRef, encodedFieldsAndValues))

    fun delete(documentRef: DocumentReference): Transaction = Transaction(native.delete(documentRef))
    suspend fun get(documentRef: DocumentReference): DocumentSnapshot = DocumentSnapshot(native.get(documentRef))
}

@PublishedApi
internal expect open class NativeQuery

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
    is DocumentReference -> native.nativeValue
    is Map<*, *> -> this.mapNotNull { (key, value) -> key?.let { it.safeValue to value?.safeValue } }
    is Collection<*> -> this.mapNotNull { it?.safeValue }
    else -> this
}

@PublishedApi
internal expect class NativeWriteBatch {
    fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions): NativeWriteBatch
    fun updateEncoded(documentRef: DocumentReference, encodedData: Any): NativeWriteBatch
    fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): NativeWriteBatch
    fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): NativeWriteBatch
    fun delete(documentRef: DocumentReference): NativeWriteBatch
    suspend fun commit()
}

data class WriteBatch internal constructor(@PublishedApi internal val native: NativeWriteBatch) {

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <reified T> set(documentRef: DocumentReference, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(documentRef, strategy, data, *mergeFields){
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        setEncoded(documentRef, encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: Any, setOptions: SetOptions) = WriteBatch(native.setEncoded(documentRef, encodedData, setOptions))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <reified T> update(documentRef: DocumentReference, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        updateEncoded(documentRef, encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    inline fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
        updateEncoded(documentRef, encode(strategy, data, buildSettings)!!)

    @JvmName("updateField")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())
    @JvmName("updateFieldPath")
    inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: Any) = WriteBatch(native.updateEncoded(documentRef, encodedData))

    @PublishedApi
    internal fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>) = WriteBatch(native.updateEncodedFieldsAndValues(documentRef, encodedFieldsAndValues))

    @PublishedApi
    internal fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) = WriteBatch(native.updateEncodedFieldPathsAndValues(documentRef, encodedFieldsAndValues))

    fun delete(documentRef: DocumentReference): WriteBatch = WriteBatch(native.delete(documentRef))
    suspend fun commit() = native.commit()
}

/** A class representing a platform specific Firebase DocumentReference. */
expect class NativeDocumentReferenceType

@PublishedApi
internal expect class NativeDocumentReference(nativeValue: NativeDocumentReferenceType) {
    val nativeValue: NativeDocumentReferenceType
    val id: String
    val path: String
    val snapshots: Flow<NativeDocumentSnapshot>
    val parent: NativeCollectionReference
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<NativeDocumentSnapshot>

    fun collection(collectionPath: String): NativeCollectionReference
    suspend fun get(): NativeDocumentSnapshot
    suspend fun setEncoded(encodedData: Any, setOptions: SetOptions)
    suspend fun updateEncoded(encodedData: Any)
    suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>)
    suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>)
    suspend fun delete()
}

/** A class representing a Firebase DocumentReference. */
@Serializable(with = DocumentReferenceSerializer::class)
data class DocumentReference internal constructor(@PublishedApi internal val native: NativeDocumentReference) {

    internal val nativeValue get() = native.nativeValue

    val id: String get() = native.id
    val path: String get() = native.path
    val snapshots: Flow<DocumentSnapshot> get() = native.snapshots.map(::DocumentSnapshot)
    val parent: CollectionReference get() = CollectionReference(native.parent)
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot> = native.snapshots(includeMetadataChanges).map(::DocumentSnapshot)

    fun collection(collectionPath: String): CollectionReference = CollectionReference(native.collection(collectionPath))
    suspend fun get(): DocumentSnapshot = DocumentSnapshot(native.get())

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, merge) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(encode(data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(data, *mergeFields)  {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(encode(data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(data, *mergeFieldPaths)  {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <reified T> set(data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(encode(data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) = set(strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(
        encode(strategy, data, buildSettings)!!, if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) = set(strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(
        encode(strategy, data, buildSettings)!!, SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) = set(strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.setEncoded(
        encode(strategy, data, buildSettings)!!, SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(data) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) = update(data) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <reified T> update(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.updateEncoded(encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = update(strategy, data)  {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <T> update(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.updateEncoded(encode(strategy, data, buildSettings)!!)

    @JvmName("updateFields")
    suspend inline fun update(vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.updateEncodedFieldsAndValues(encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @JvmName("updateFieldPaths")
    suspend inline fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = native.updateEncodedFieldPathsAndValues(encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    suspend fun delete() = native.delete()
}

@PublishedApi
internal expect class NativeCollectionReference : NativeQuery {
    val path: String
    val document: NativeDocumentReference
    val parent: NativeDocumentReference?

    fun document(documentPath: String): NativeDocumentReference
    suspend fun addEncoded(data: Any): NativeDocumentReference
}

data class CollectionReference internal constructor(@PublishedApi internal val native: NativeCollectionReference) : Query(native) {

    val path: String get() = native.path
    val document: DocumentReference get() = DocumentReference(native.document)
    val parent: DocumentReference? get() = native.parent?.let(::DocumentReference)

    fun document(documentPath: String): DocumentReference = DocumentReference(native.document(documentPath))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(data) { this.encodeDefaults = encodeDefaults }"))
    suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) = add(data) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <reified T> add(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = addEncoded(
        encode(data, buildSettings)!!
    )

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) = add(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline fun <T> add(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) = addEncoded(
        encode(strategy, data, buildSettings)!!
    )

    @PublishedApi
    internal suspend fun addEncoded(data: Any): DocumentReference = DocumentReference(native.addEncoded(data))
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

@PublishedApi
internal expect class NativeDocumentSnapshot {

    val exists: Boolean
    val id: String
    val reference: NativeDocumentReference
    val metadata: SnapshotMetadata

    fun contains(field: String): Boolean

    fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
    fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
}

data class DocumentSnapshot internal constructor(@PublishedApi internal val native: NativeDocumentSnapshot) {

    val exists: Boolean get() = native.exists
    val id: String get() = native.id
    val reference: DocumentReference get() = DocumentReference(native.reference)
    val metadata: SnapshotMetadata get() = native.metadata


    inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(field, serverTimestampBehavior), buildSettings)
    inline fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(field, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = native.getEncoded(field, serverTimestampBehavior)

    inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(encodedData(serverTimestampBehavior), buildSettings)
    inline fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, encodedData(serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = native.encodedData(serverTimestampBehavior)
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
    companion object {
        val documentId: FieldPath
    }
    @Deprecated("Use companion object instead", replaceWith = ReplaceWith("FieldPath.documentId"))
    val documentId: FieldPath
    val encoded: EncodedFieldPath
}

expect class EncodedFieldPath
