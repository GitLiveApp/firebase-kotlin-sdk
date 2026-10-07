/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.internal.EncodedObject
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.aggregateAverage
import com.google.firebase.firestore.aggregateSum
import com.google.firebase.firestore.fieldPathDocumentId
import com.google.firebase.firestore.fieldPathOf
import com.google.firebase.firestore.setFirestoreLoggingEnabled
import dev.gitlive.firebase.firestore.internal.SetOptions
import dev.gitlive.firebase.firestore.internal.getAwait
import dev.gitlive.firebase.firestore.internal.nativeData
import dev.gitlive.firebase.firestore.internal.nativeGet
import dev.gitlive.firebase.firestore.internal.runSuspendTransaction
import dev.gitlive.firebase.firestore.internal.toCompat
import dev.gitlive.firebase.firestore.internal.toCompatMap
import dev.gitlive.firebase.firestore.internal.wrapperSnapshots
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.encodeAsObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.jvm.JvmName
import com.google.firebase.firestore.CollectionReference as CompatCollectionReference
import com.google.firebase.firestore.DocumentChange as CompatDocumentChange
import com.google.firebase.firestore.DocumentReference as CompatDocumentReference
import com.google.firebase.firestore.DocumentSnapshot as CompatDocumentSnapshot
import com.google.firebase.firestore.FieldPath as CompatFieldPath
import com.google.firebase.firestore.FirebaseFirestore as CompatFirebaseFirestore
import com.google.firebase.firestore.Query as CompatQuery
import com.google.firebase.firestore.QuerySnapshot as CompatQuerySnapshot
import com.google.firebase.firestore.SnapshotMetadata as CompatSnapshotMetadata
import com.google.firebase.firestore.Transaction as CompatTransaction
import com.google.firebase.firestore.WriteBatch as CompatWriteBatch

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.FirebaseFirestore] this wraps. */
public class FirebaseFirestore internal constructor(public val compat: CompatFirebaseFirestore) {

    public companion object {}

    public var settings: FirebaseFirestoreSettings
        @Deprecated("Property can only be written.", level = DeprecationLevel.ERROR)
        get() = throw NotImplementedError()
        set(value) {
            value.applyTo(compat)
        }

    public fun collection(collectionPath: String): CollectionReference = CollectionReference(compat.collection(collectionPath))
    public fun collectionGroup(collectionId: String): Query = Query(compat.collectionGroup(collectionId))
    public fun document(documentPath: String): DocumentReference = DocumentReference(compat.document(documentPath))
    public fun batch(): WriteBatch = WriteBatch(compat.batch())
    public fun setLoggingEnabled(loggingEnabled: Boolean) {
        setFirestoreLoggingEnabled(loggingEnabled)
    }
    public suspend fun clearPersistence() {
        compat.clearPersistence().await()
    }
    public suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T = compat.runSuspendTransaction { func(Transaction(it)) }
    public fun useEmulator(host: String, port: Int) {
        compat.useEmulator(host, port)
    }

    @Deprecated("Use SettingsBuilder instead", replaceWith = ReplaceWith("settings = firestoreSettings { }", "dev.gitlive.firebase.firestore.firestoreSettings"))
    public fun setSettings(
        persistenceEnabled: Boolean? = null,
        sslEnabled: Boolean? = null,
        host: String? = null,
        cacheSizeBytes: Long? = null,
    ) {
        settings = firestoreSettings {
            this.sslEnabled = sslEnabled ?: true
            this.host = host ?: FirebaseFirestoreSettings.DEFAULT_HOST
            this.cacheSettings = if (persistenceEnabled != false) {
                LocalCacheSettings.Persistent(
                    cacheSizeBytes ?: FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED,
                )
            } else {
                val cacheSize = cacheSizeBytes ?: FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
                val garbageCollectionSettings =
                    if (cacheSize == FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) {
                        MemoryGarbageCollectorSettings.Eager
                    } else {
                        MemoryGarbageCollectorSettings.LRUGC(cacheSize)
                    }
                LocalCacheSettings.Memory(garbageCollectionSettings)
            }
        }
    }

    public suspend fun disableNetwork() {
        compat.disableNetwork().await()
    }
    public suspend fun enableNetwork() {
        compat.enableNetwork().await()
    }

    public suspend fun terminate() {
        compat.terminate().await()
    }

    public suspend fun waitForPendingWrites() {
        compat.waitForPendingWrites().await()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseFirestore && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

public expect class FirebaseFirestoreSettings {

    public companion object {
        public val CACHE_SIZE_UNLIMITED: Long
        internal val DEFAULT_HOST: String
        internal val MINIMUM_CACHE_BYTES: Long
        internal val DEFAULT_CACHE_SIZE_BYTES: Long
    }

    public class Builder {
        public constructor()
        public constructor(settings: FirebaseFirestoreSettings)

        public var sslEnabled: Boolean
        public var host: String
        public var cacheSettings: LocalCacheSettings

        public fun build(): FirebaseFirestoreSettings
    }

    public val sslEnabled: Boolean
    public val host: String
    public val cacheSettings: LocalCacheSettings
}

public expect fun firestoreSettings(settings: FirebaseFirestoreSettings? = null, builder: FirebaseFirestoreSettings.Builder.() -> Unit): FirebaseFirestoreSettings

/** Applies these settings to [firestore], including the platform-specific callback executor or dispatch queue. */
internal expect fun FirebaseFirestoreSettings.applyTo(firestore: CompatFirebaseFirestore)

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.Transaction] this wraps. */
public data class Transaction internal constructor(public val compat: CompatTransaction) {

    public companion object {}

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean = false): Transaction = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String): Transaction = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Transaction = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false): Transaction = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String): Transaction = set(documentRef, strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Transaction = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): Transaction = apply {
        setOptions.toCompat()?.let { compat.set(documentRef.compat, encodedData.toCompatMap(), it) } ?: compat.set(documentRef.compat, encodedData.toCompatMap())
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    public fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean): Transaction = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun update(documentRef: DocumentReference, data: Any, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncoded(documentRef, encodeAsObject(data, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): Transaction = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncoded(documentRef, encodeAsObject(strategy, data, buildSettings))

    @JvmName("updateFields")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) { fieldsAndValues.forEach { (field, value) -> field to value } }"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): Transaction = update(documentRef, *fieldsAndValues) {}

    @JvmName("updateFields")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) {\napply(buildSettings)\nfieldsAndValues.forEach { (field, value) -> field to value }\n}"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): Transaction = updateFields(
        documentRef,
    ) {
        apply(buildSettings)
        fieldsAndValues.forEach { (field, value) ->
            field to value
        }
    }

    @JvmName("updateFieldPaths")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) { fieldsAndValues.forEach { (field, value) -> field to value } }"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): Transaction = update(documentRef, *fieldsAndValues) {}

    @JvmName("updateFieldPaths")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) {\napply(buildSettings)\nfieldsAndValues.forEach { (field, value) -> field to value }\n}"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): Transaction = updateFields(
        documentRef,
    ) {
        apply(buildSettings)
        fieldsAndValues.forEach { (field, value) ->
            field to value
        }
    }

    /**
     * Updates Fields/[FieldPath] of a [DocumentReference] using a [FieldsAndValuesUpdateDSL].
     * @param documentRef the [DocumentReference] to update
     * @param fieldsAndValuesUpdateDSL closure for configuring the [FieldsAndValuesUpdateDSL]
     */
    public fun updateFields(
        documentRef: DocumentReference,
        fieldsAndValuesUpdateDSL: FieldsAndValuesUpdateDSL.() -> Unit,
    ): Transaction = apply {
        FieldsAndValuesUpdateDSL().apply(fieldsAndValuesUpdateDSL).fieldsAndValues.takeIf { it.isNotEmpty() }?.performUpdate(
            updateAsField = { field, value, moreFieldsAndValues -> compat.update(documentRef.compat, field, value, *moreFieldsAndValues) },
            updateAsFieldPath = { fieldPath, value, moreFieldsAndValues -> compat.update(documentRef.compat, fieldPath, value, *moreFieldsAndValues) },
        )
    }

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): Transaction = apply { compat.update(documentRef.compat, encodedData.toCompatMap()) }

    public fun delete(documentRef: DocumentReference): Transaction = apply { compat.delete(documentRef.compat) }
    public suspend fun get(documentRef: DocumentReference): DocumentSnapshot = DocumentSnapshot(compat.getAwait(documentRef.compat))
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.Query] this wraps. */
public open class Query internal constructor(public open val compat: CompatQuery) {

    public companion object {}

    public fun limit(limit: Number): Query = Query(compat.limit(limit.toLong()))
    public fun limitToLast(limit: Number): Query = Query(compat.limitToLast(limit.toLong()))

    public suspend fun count(): Long = compat.count().get(AggregateSource.SERVER).await().count
    public suspend fun sum(field: String): Double = aggregate(aggregateSum(field)) ?: 0.0
    public suspend fun sum(field: FieldPath): Double = aggregate(aggregateSum(field.compat)) ?: 0.0
    public suspend fun average(field: String): Double? = aggregate(aggregateAverage(field))
    public suspend fun average(field: FieldPath): Double? = aggregate(aggregateAverage(field.compat))

    private suspend fun aggregate(field: com.google.firebase.firestore.AggregateField): Double? = compat.aggregate(field).get(AggregateSource.SERVER).await().getDouble(field)

    public val snapshots: Flow<QuerySnapshot> get() = snapshots()
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot> = compat.wrapperSnapshots(includeMetadataChanges).map { QuerySnapshot(it) }
    public suspend fun get(source: Source = Source.DEFAULT): QuerySnapshot = QuerySnapshot(compat.get(source.toCompat()).await())

    public fun where(builder: FilterBuilder.() -> Filter?): Query = builder(FilterBuilder())?.let { Query(compat.where(it.toCompat())) } ?: this

    public fun orderBy(field: String, direction: Direction = Direction.ASCENDING): Query = Query(compat.orderBy(field, direction))
    public fun orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING): Query = Query(compat.orderBy(field.compat, direction))

    public fun startAfter(document: DocumentSnapshot): Query = Query(compat.startAfter(document.compat))

    @Deprecated("Deprecated. Use `startAfterFieldValues` instead", replaceWith = ReplaceWith("startAfterFieldValues { fieldValues.forEach { add(it) } }"))
    public fun startAfter(vararg fieldValues: Any?): Query = startAfter(*fieldValues) {}

    @Deprecated("Deprecated. Use `startAfterFieldValues` instead", replaceWith = ReplaceWith("startAfterFieldValues {\napply(buildSettings)\nfieldValues.forEach { add(it) }\n}"))
    public fun startAfter(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = startAfterFieldValues {
        apply(buildSettings)

        fieldValues.forEach {
            add(it)
        }
    }

    /**
     * Creates and returns a new [Query] that starts after the provided fields relative to the order of the query.
     * The field values are configured using a [FieldValuesDSL].
     * The order of the field values must match the order of the [orderBy] clauses of the query
     * @param builder closure for configuring the [FieldValuesDSL]
     */
    public fun startAfterFieldValues(builder: FieldValuesDSL.() -> Unit): Query = Query(compat.startAfter(*FieldValuesDSL().apply(builder).fieldValues.toTypedArray()))

    public fun startAt(document: DocumentSnapshot): Query = Query(compat.startAt(document.compat))

    @Deprecated("Deprecated. Use `startAtFieldValues` instead", replaceWith = ReplaceWith("startAtFieldValues { fieldValues.forEach { add(it) } }"))
    public fun startAt(vararg fieldValues: Any?): Query = startAt(*fieldValues) {}

    @Deprecated("Deprecated. Use `startAtFieldValues` instead", replaceWith = ReplaceWith("startAtFieldValues {\napply(buildSettings)\nfieldValues.forEach { add(it) }\n}"))
    public fun startAt(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = startAtFieldValues {
        apply(buildSettings)
        fieldValues.forEach {
            add(it)
        }
    }

    /**
     * Creates and returns a new [Query] that starts at the provided fields relative to the order of the query.
     * The field values are configured using a [FieldValuesDSL].
     * The order of the field values must match the order of the [orderBy] clauses of the query
     * @param builder closure for configuring the [FieldValuesDSL]
     */
    public fun startAtFieldValues(builder: FieldValuesDSL.() -> Unit): Query = Query(compat.startAt(*FieldValuesDSL().apply(builder).fieldValues.toTypedArray()))

    public fun endBefore(document: DocumentSnapshot): Query = Query(compat.endBefore(document.compat))

    @Deprecated("Deprecated. Use `endBefore` instead", replaceWith = ReplaceWith("endBeforeFieldValues { fieldValues.forEach { add(it) } }"))
    public fun endBefore(vararg fieldValues: Any?): Query = endBefore(*fieldValues) {}

    @Deprecated("Deprecated. Use `endBefore` instead", replaceWith = ReplaceWith("endBefore {\napply(buildSettings)\nfieldValues.forEach { add(it) }\n}"))
    public fun endBefore(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = endBeforeFieldValues {
        apply(buildSettings)
        fieldValues.forEach {
            add(it)
        }
    }

    /**
     * Creates and returns a new [Query] that ends before the provided fields relative to the order of the query.
     * The field values are configured using a [FieldValuesDSL].
     * The order of the field values must match the order of the [orderBy] clauses of the query
     * @param builder closure for configuring the [FieldValuesDSL]
     */
    public fun endBeforeFieldValues(builder: FieldValuesDSL.() -> Unit): Query = Query(compat.endBefore(*FieldValuesDSL().apply(builder).fieldValues.toTypedArray()))

    public fun endAt(document: DocumentSnapshot): Query = Query(compat.endAt(document.compat))

    @Deprecated("Deprecated. Use `endAtFieldValues` instead", replaceWith = ReplaceWith("endAtFieldValues { fieldValues.forEach { add(it) } }"))
    public fun endAt(vararg fieldValues: Any?): Query = endAt(*fieldValues) {}

    @Deprecated("Deprecated. Use `endAt` instead", replaceWith = ReplaceWith("endAt {\napply(buildSettings)\nfieldValues.forEach { add(it) }\n}"))
    public fun endAt(vararg fieldValues: Any?, buildSettings: EncodeSettings.Builder.() -> Unit): Query = endAtFieldValues {
        apply(buildSettings)
        fieldValues.forEach {
            add(it)
        }
    }

    /**
     * Creates and returns a new [Query] that ends at the provided fields relative to the order of the query.
     * The field values are configured using a [FieldValuesDSL].
     * The order of the field values must match the order of the [orderBy] clauses of the query
     * @param builder closure for configuring the [FieldValuesDSL]
     */
    public fun endAtFieldValues(builder: FieldValuesDSL.() -> Unit): Query = Query(compat.endAt(*FieldValuesDSL().apply(builder).fieldValues.toTypedArray()))

    override fun equals(other: Any?): Boolean = other is Query && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { field equalTo equalTo }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, equalTo: Any?): Query = where {
    field equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { path equalTo equalTo }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, equalTo: Any?): Query = where {
    path equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = where {
    all(
        *listOfNotNull(
            lessThan?.let { field lessThan it },
            greaterThan?.let { field greaterThan it },
            arrayContains?.let { field contains it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = where {
    all(
        *listOfNotNull(
            lessThan?.let { path lessThan it },
            greaterThan?.let { path greaterThan it },
            arrayContains?.let { path contains it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = where {
    all(
        *listOfNotNull(
            inArray?.let { field inArray it },
            arrayContainsAny?.let { field containsAny it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = where {
    all(
        *listOfNotNull(
            inArray?.let { path inArray it },
            arrayContainsAny?.let { path containsAny it },
        ).toTypedArray(),
    )
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.WriteBatch] this wraps. */
public data class WriteBatch internal constructor(public val compat: CompatWriteBatch) {

    public companion object {}

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean = false): WriteBatch = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String): WriteBatch = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): WriteBatch = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false): WriteBatch = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String): WriteBatch = set(documentRef, strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): WriteBatch = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): WriteBatch = apply {
        setOptions.toCompat()?.let { compat.set(documentRef.compat, encodedData.toCompatMap(), it) } ?: compat.set(documentRef.compat, encodedData.toCompatMap())
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean): WriteBatch = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> update(documentRef: DocumentReference, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncoded(documentRef, encodeAsObject(data, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): WriteBatch = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncoded(documentRef, encodeAsObject(strategy, data, buildSettings))

    @JvmName("updateField")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) { fieldsAndValues.forEach { (field, value) -> field to value } }"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>): WriteBatch = update(documentRef, *fieldsAndValues) {}

    @JvmName("updateField")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) {\napply(buildSettings)\nfieldsAndValues.forEach { (field, value) -> field to value }\n}"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): WriteBatch = updateFields(
        documentRef,
    ) {
        apply(buildSettings)
        fieldsAndValues.forEach { (field, value) ->
            field to value
        }
    }

    @JvmName("updateFieldPath")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) { fieldsAndValues.forEach { (field, value) -> field to value } }"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>): WriteBatch = update(documentRef, *fieldsAndValues) {}

    @JvmName("updateFieldPath")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields(documentRef) {\napply(buildSettings)\nfieldsAndValues.forEach { (field, value) -> field to value }\n}"))
    public fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): WriteBatch = updateFields(
        documentRef,
    ) {
        apply(buildSettings)
        fieldsAndValues.forEach { (path, value) ->
            path to value
        }
    }

    /**
     * Updates Fields/[FieldPath] of a [DocumentReference] using a [FieldsAndValuesUpdateDSL].
     * @param documentRef the [DocumentReference] to update
     * @param fieldsAndValuesUpdateDSL closure for configuring the [FieldsAndValuesUpdateDSL]
     */
    public fun updateFields(
        documentRef: DocumentReference,
        fieldsAndValuesUpdateDSL: FieldsAndValuesUpdateDSL.() -> Unit,
    ): WriteBatch = apply {
        FieldsAndValuesUpdateDSL().apply(fieldsAndValuesUpdateDSL).fieldsAndValues.takeIf { it.isNotEmpty() }?.performUpdate(
            updateAsField = { field, value, moreFieldsAndValues -> compat.update(documentRef.compat, field, value, *moreFieldsAndValues) },
            updateAsFieldPath = { fieldPath, value, moreFieldsAndValues -> compat.update(documentRef.compat, fieldPath, value, *moreFieldsAndValues) },
        )
    }

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): WriteBatch = apply { compat.update(documentRef.compat, encodedData.toCompatMap()) }

    public fun delete(documentRef: DocumentReference): WriteBatch = apply { compat.delete(documentRef.compat) }
    public suspend fun commit() {
        compat.commit().await()
    }
}

/** The Android-SDK-shaped document reference the platform SDKs read and write. */
internal typealias NativeDocumentReferenceType = CompatDocumentReference

/**
 * A class representing a Firebase DocumentReference.
 * @property compat The Android-SDK-shaped [com.google.firebase.firestore.DocumentReference] this wraps.
 */
@Serializable(with = DocumentReferenceSerializer::class)
public data class DocumentReference internal constructor(public val compat: CompatDocumentReference) {

    public companion object {}

    internal val nativeValue: NativeDocumentReferenceType get() = compat

    val id: String get() = compat.id
    val path: String get() = compat.path
    val snapshots: Flow<DocumentSnapshot> get() = snapshots()
    val parent: CollectionReference get() = CollectionReference(compat.parent)
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot> = compat.wrapperSnapshots(includeMetadataChanges).map(::DocumentSnapshot)

    public fun collection(collectionPath: String): CollectionReference = CollectionReference(compat.collection(collectionPath))
    public suspend fun get(source: Source = Source.DEFAULT): DocumentSnapshot = DocumentSnapshot(compat.get(source.toCompat()).await())

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, merge) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, merge: Boolean = false) {
        set(data, merge) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            if (merge) SetOptions.Merge else SetOptions.Overwrite,
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) {
        set(data, *mergeFields) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            SetOptions.MergeFields(mergeFields.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) {
        set(data, *mergeFieldPaths) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            SetOptions.MergeFieldPaths(mergeFieldPaths.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) {
        set(strategy, data, merge) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            if (merge) SetOptions.Merge else SetOptions.Overwrite,
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) {
        set(strategy, data, *mergeFields) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            SetOptions.MergeFields(mergeFields.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) {
        set(strategy, data, *mergeFieldPaths) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            SetOptions.MergeFieldPaths(mergeFieldPaths.asList()),
        )
    }

    @PublishedApi
    internal suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) {
        (setOptions.toCompat()?.let { compat.set(encodedData.toCompatMap(), it) } ?: compat.set(encodedData.toCompatMap())).await()
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(data) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> update(data: T, encodeDefaults: Boolean) {
        update(data) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> update(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncoded(encodeAsObject(data, buildSettings))
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) {
        update(strategy, data) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> update(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncoded(
            encodeAsObject(strategy, data, buildSettings),
        )
    }

    @PublishedApi
    internal suspend fun updateEncoded(encodedData: EncodedObject) {
        compat.update(encodedData.toCompatMap()).await()
    }

    @JvmName("updateFields")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields { fieldsAndValues.forEach { (field, value) -> field to value } }"))
    public suspend fun update(vararg fieldsAndValues: Pair<String, Any?>): Unit = update(*fieldsAndValues) {}

    @JvmName("updateFields")
    @Deprecated("Deprecated. Use `updateFields` instead", replaceWith = ReplaceWith("updateFields {\napply(buildSettings)\nfieldsAndValues.forEach { (field, value) -> field to value }\n}"))
    public suspend fun update(vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): Unit = updateFields {
        apply(buildSettings)
        fieldsAndValues.forEach { (field, value) ->
            field to value
        }
    }

    @JvmName("updateFieldPaths")
    public suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>): Unit = update(*fieldsAndValues) {}

    @JvmName("updateFieldPaths")
    public suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit): Unit = updateFields {
        apply(buildSettings)
        fieldsAndValues.forEach { (fieldPath, value) ->
            fieldPath to value
        }
    }

    /**
     * Updates Fields/[FieldPath] using a [FieldsAndValuesUpdateDSL].
     * @param fieldsAndValuesUpdateDSL closure for configuring the [FieldsAndValuesUpdateDSL]
     */
    public suspend fun updateFields(
        fieldsAndValuesUpdateDSL: FieldsAndValuesUpdateDSL.() -> Unit,
    ) {
        FieldsAndValuesUpdateDSL().apply(fieldsAndValuesUpdateDSL).fieldsAndValues.takeIf { it.isNotEmpty() }?.performUpdate(
            updateAsField = { field, value, moreFieldsAndValues -> compat.update(field, value, *moreFieldsAndValues) },
            updateAsFieldPath = { fieldPath, value, moreFieldsAndValues -> compat.update(fieldPath, value, *moreFieldsAndValues) },
        )?.await()
    }

    public suspend fun delete() {
        compat.delete().await()
    }
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.CollectionReference] this wraps. */
public data class CollectionReference internal constructor(override val compat: CompatCollectionReference) : Query(compat) {

    public companion object {}

    val path: String get() = compat.path
    val document: DocumentReference get() = DocumentReference(compat.document())
    val parent: DocumentReference? get() = compat.parent?.let(::DocumentReference)

    public fun document(documentPath: String): DocumentReference = DocumentReference(compat.document(documentPath))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(data) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> add(data: T, encodeDefaults: Boolean): DocumentReference = add(data) {
        this.encodeDefaults = encodeDefaults
    }
    public suspend inline fun <reified T : Any> add(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): DocumentReference = addEncoded(
        encodeAsObject(data, buildSettings),
    )

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): DocumentReference = add(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public suspend inline fun <T : Any> add(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): DocumentReference = addEncoded(
        encodeAsObject(strategy, data, buildSettings),
    )

    @PublishedApi
    internal suspend fun addEncoded(data: EncodedObject): DocumentReference = DocumentReference(compat.add(data.toCompatMap()).await())
}

/** A failed Firestore operation; the Android-SDK-shaped [com.google.firebase.firestore.FirebaseFirestoreException]. */
public typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

/** The error codes of Firestore; the Android-SDK-shaped [com.google.firebase.firestore.FirebaseFirestoreException.Code]. */
public typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

/** The sort direction of an `orderBy`; the Android-SDK-shaped [com.google.firebase.firestore.Query.Direction]. */
public typealias Direction = com.google.firebase.firestore.Query.Direction

/** The kind of a [DocumentChange]; the Android-SDK-shaped [com.google.firebase.firestore.DocumentChange.Type]. */
public typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.QuerySnapshot] this wraps. */
public class QuerySnapshot(public val compat: CompatQuerySnapshot) {
    public val documents: List<DocumentSnapshot> get() = compat.documents.map { DocumentSnapshot(it) }
    public val documentChanges: List<DocumentChange> get() = compat.documentChanges.map { DocumentChange(it) }
    public val metadata: SnapshotMetadata get() = SnapshotMetadata(compat.metadata)

    override fun equals(other: Any?): Boolean = other is QuerySnapshot && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.DocumentChange] this wraps. */
public class DocumentChange(public val compat: CompatDocumentChange) {
    public val document: DocumentSnapshot get() = DocumentSnapshot(compat.document)
    public val newIndex: Int get() = compat.newIndex
    public val oldIndex: Int get() = compat.oldIndex
    public val type: ChangeType get() = compat.type

    override fun equals(other: Any?): Boolean = other is DocumentChange && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.DocumentSnapshot] this wraps. */
public data class DocumentSnapshot internal constructor(public val compat: CompatDocumentSnapshot) {

    public companion object {}

    val exists: Boolean get() = compat.exists()
    val id: String get() = compat.id
    val reference: DocumentReference get() = DocumentReference(compat.reference)
    val metadata: SnapshotMetadata get() = SnapshotMetadata(compat.metadata)

    public fun contains(field: String): Boolean = compat.contains(field)
    public fun contains(fieldPath: FieldPath): Boolean = compat.contains(fieldPath.compat)

    public inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(field, serverTimestampBehavior), buildSettings)
    public inline fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(field, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = compat.nativeGet(field, serverTimestampBehavior.toCompat())

    public inline fun <reified T> get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(fieldPath, serverTimestampBehavior), buildSettings)
    public inline fun <T> get(fieldPath: FieldPath, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(fieldPath, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun getEncoded(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = compat.nativeGet(fieldPath.compat, serverTimestampBehavior.toCompat())

    public inline fun <reified T> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(encodedData(serverTimestampBehavior), buildSettings)
    public inline fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, encodedData(serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = compat.nativeData(serverTimestampBehavior.toCompat())
}

public enum class ServerTimestampBehavior {
    ESTIMATE,
    NONE,
    PREVIOUS,
}

internal fun ServerTimestampBehavior.toCompat(): CompatDocumentSnapshot.ServerTimestampBehavior = when (this) {
    ServerTimestampBehavior.ESTIMATE -> CompatDocumentSnapshot.ServerTimestampBehavior.ESTIMATE
    ServerTimestampBehavior.NONE -> CompatDocumentSnapshot.ServerTimestampBehavior.NONE
    ServerTimestampBehavior.PREVIOUS -> CompatDocumentSnapshot.ServerTimestampBehavior.PREVIOUS
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.SnapshotMetadata] this wraps. */
public class SnapshotMetadata(public val compat: CompatSnapshotMetadata) {
    public val hasPendingWrites: Boolean get() = compat.hasPendingWrites()
    public val isFromCache: Boolean get() = compat.isFromCache

    override fun equals(other: Any?): Boolean = other is SnapshotMetadata && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/** @property compat The Android-SDK-shaped [com.google.firebase.firestore.FieldPath] this wraps. */
public class FieldPath internal constructor(public val compat: CompatFieldPath) {
    public constructor(vararg fieldNames: String) : this(fieldPathOf(*fieldNames))

    public companion object {
        public val documentId: FieldPath get() = FieldPath(fieldPathDocumentId())
    }

    @Deprecated("Use companion object instead", replaceWith = ReplaceWith("FieldPath.documentId"))
    public val documentId: FieldPath get() = FieldPath.documentId
    public val encoded: EncodedFieldPath get() = compat

    override fun equals(other: Any?): Boolean = other is FieldPath && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

/** The Android-SDK-shaped field path the platform SDKs take. */
public typealias EncodedFieldPath = CompatFieldPath

public enum class Source {
    CACHE,
    SERVER,
    DEFAULT,
}

internal fun Source.toCompat(): com.google.firebase.firestore.Source = when (this) {
    Source.CACHE -> com.google.firebase.firestore.Source.CACHE
    Source.SERVER -> com.google.firebase.firestore.Source.SERVER
    Source.DEFAULT -> com.google.firebase.firestore.Source.DEFAULT
}
