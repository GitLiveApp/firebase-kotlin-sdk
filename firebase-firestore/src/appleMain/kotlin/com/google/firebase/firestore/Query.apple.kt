/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRAggregateField
import cocoapods.FirebaseFirestoreInternal.FIRAggregateQuerySnapshot
import cocoapods.FirebaseFirestoreInternal.FIRAggregateSource
import cocoapods.FirebaseFirestoreInternal.FIRCollectionReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentSnapshot
import cocoapods.FirebaseFirestoreInternal.FIRFirestoreSource
import cocoapods.FirebaseFirestoreInternal.FIRListenSource
import cocoapods.FirebaseFirestoreInternal.FIRQuery
import cocoapods.FirebaseFirestoreInternal.FIRQuerySnapshot
import cocoapods.FirebaseFirestoreInternal.FIRSnapshotListenOptions
import cocoapods.FirebaseFirestoreInternal.FIRTransaction
import cocoapods.FirebaseFirestoreInternal.FIRWriteBatch
import com.google.android.gms.tasks.Task
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSNull
import platform.Foundation.NSNumber

private fun Source.toIos(): FIRFirestoreSource = when (this) {
    Source.DEFAULT -> FIRFirestoreSource.FIRFirestoreSourceDefault
    Source.SERVER -> FIRFirestoreSource.FIRFirestoreSourceServer
    Source.CACHE -> FIRFirestoreSource.FIRFirestoreSourceCache
}

private fun SnapshotListenOptions.toIos(): FIRSnapshotListenOptions = FIRSnapshotListenOptions()
    .optionsWithIncludeMetadataChanges(metadataChanges == MetadataChanges.INCLUDE)
    .optionsWithSource(if (source == ListenSource.CACHE) FIRListenSource.FIRListenSourceCache else FIRListenSource.FIRListenSourceDefault)

private fun Array<out Any?>.toIos(): List<Any?> = map { it.toIos() }

/** The field/value pairs of an `update` as the iOS SDK's `updateData` takes them (keys are field names or field paths). */
private fun updateData(field: Any, value: Any?, moreFieldsAndValues: Array<out Any?>): Map<Any?, *> {
    require(moreFieldsAndValues.size % 2 == 0) { "Missing value in call to update(). There must be an even number of arguments that alternate between field names and values" }
    val data = mutableMapOf<Any?, Any?>(field.toIos() to value.toIos())
    moreFieldsAndValues.toList().chunked(2).forEach { (key, value) -> data[key.toIos()] = value.toIos() }
    return data
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual open class Query internal constructor(public open val ios: FIRQuery) {
    public actual val firestore: FirebaseFirestore get() = FirebaseFirestore(ios.firestore)

    public actual fun whereEqualTo(field: String, value: Any?): Query = Query(ios.queryWhereField(field, isEqualTo = value.toIos() ?: NSNull.`null`()))
    public actual fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isEqualTo = value.toIos() ?: NSNull.`null`()))
    public actual fun whereNotEqualTo(field: String, value: Any?): Query = Query(ios.queryWhereField(field, isNotEqualTo = value.toIos() ?: NSNull.`null`()))
    public actual fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isNotEqualTo = value.toIos() ?: NSNull.`null`()))
    public actual fun whereLessThan(field: String, value: Any): Query = Query(ios.queryWhereField(field, isLessThan = value.toIos()!!))
    public actual fun whereLessThan(fieldPath: FieldPath, value: Any): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isLessThan = value.toIos()!!))
    public actual fun whereLessThanOrEqualTo(field: String, value: Any): Query = Query(ios.queryWhereField(field, isLessThanOrEqualTo = value.toIos()!!))
    public actual fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isLessThanOrEqualTo = value.toIos()!!))
    public actual fun whereGreaterThan(field: String, value: Any): Query = Query(ios.queryWhereField(field, isGreaterThan = value.toIos()!!))
    public actual fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isGreaterThan = value.toIos()!!))
    public actual fun whereGreaterThanOrEqualTo(field: String, value: Any): Query = Query(ios.queryWhereField(field, isGreaterThanOrEqualTo = value.toIos()!!))
    public actual fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), isGreaterThanOrEqualTo = value.toIos()!!))
    public actual fun whereArrayContains(field: String, value: Any): Query = Query(ios.queryWhereField(field, arrayContains = value.toIos()!!))
    public actual fun whereArrayContains(fieldPath: FieldPath, value: Any): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), arrayContains = value.toIos()!!))
    public actual fun whereArrayContainsAny(field: String, values: List<Any>): Query = Query(ios.queryWhereField(field, arrayContainsAny = values.toIos() as List<*>))
    public actual fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), arrayContainsAny = values.toIos() as List<*>))
    public actual fun whereIn(field: String, values: List<Any>): Query = Query(ios.queryWhereField(field, `in` = values.toIos() as List<*>))
    public actual fun whereIn(fieldPath: FieldPath, values: List<Any>): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), `in` = values.toIos() as List<*>))
    public actual fun whereNotIn(field: String, values: List<Any>): Query = Query(ios.queryWhereField(field, notIn = values.toIos() as List<*>))
    public actual fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query = Query(ios.queryWhereFieldPath(fieldPath.toIos(), notIn = values.toIos() as List<*>))

    public actual fun where(filter: Filter): Query = filter.node?.let { Query(ios.queryWhereFilter(it.toIos())) } ?: this

    public actual fun orderBy(field: String): Query = Query(ios.queryOrderedByField(field))
    public actual fun orderBy(fieldPath: FieldPath): Query = Query(ios.queryOrderedByFieldPath(fieldPath.toIos()))
    public actual fun orderBy(field: String, direction: Direction): Query = Query(ios.queryOrderedByField(field, direction == Direction.DESCENDING))
    public actual fun orderBy(fieldPath: FieldPath, direction: Direction): Query = Query(ios.queryOrderedByFieldPath(fieldPath.toIos(), direction == Direction.DESCENDING))

    public actual fun limit(limit: Long): Query = Query(ios.queryLimitedTo(limit))
    public actual fun limitToLast(limit: Long): Query = Query(ios.queryLimitedToLast(limit))

    public actual fun startAt(snapshot: DocumentSnapshot): Query = Query(ios.queryStartingAtDocument(snapshot.ios))
    public actual fun startAt(vararg fieldValues: Any?): Query = Query(ios.queryStartingAtValues(fieldValues.toIos()))
    public actual fun startAfter(snapshot: DocumentSnapshot): Query = Query(ios.queryStartingAfterDocument(snapshot.ios))
    public actual fun startAfter(vararg fieldValues: Any?): Query = Query(ios.queryStartingAfterValues(fieldValues.toIos()))
    public actual fun endBefore(snapshot: DocumentSnapshot): Query = Query(ios.queryEndingBeforeDocument(snapshot.ios))
    public actual fun endBefore(vararg fieldValues: Any?): Query = Query(ios.queryEndingBeforeValues(fieldValues.toIos()))
    public actual fun endAt(snapshot: DocumentSnapshot): Query = Query(ios.queryEndingAtDocument(snapshot.ios))
    public actual fun endAt(vararg fieldValues: Any?): Query = Query(ios.queryEndingAtValues(fieldValues.toIos()))

    public actual fun get(): Task<QuerySnapshot> = get(Source.DEFAULT)

    public actual fun get(source: Source): Task<QuerySnapshot> = task { completion ->
        ios.getDocumentsWithSource(source.toIos()) { snapshot, error -> completion(snapshot?.let { QuerySnapshot(it) }, error) }
    }

    public actual fun addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(MetadataChanges.EXCLUDE, listener)

    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration {
        val registration = ios.addSnapshotListenerWithIncludeMetadataChanges(metadataChanges == MetadataChanges.INCLUDE, listener.toIos())
        return ListenerRegistration { registration.remove() }
    }

    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<QuerySnapshot>): ListenerRegistration {
        val registration = ios.addSnapshotListenerWithOptions(options.toIos(), listener.toIos())
        return ListenerRegistration { registration.remove() }
    }

    private fun EventListener<QuerySnapshot>.toIos(): (FIRQuerySnapshot?, NSError?) -> Unit = { snapshot, error ->
        onEvent(snapshot?.let { QuerySnapshot(it) }, error?.toFirestoreException())
    }

    public actual fun count(): AggregateQuery = AggregateQuery(this, listOf(AggregateField.count()))

    public actual fun aggregate(aggregateField: AggregateField, vararg aggregateFields: AggregateField): AggregateQuery = AggregateQuery(this, listOf(aggregateField) + aggregateFields)

    override fun equals(other: Any?): Boolean = other is Query && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "Query"

    public actual enum class Direction {
        ASCENDING,
        DESCENDING,
    }
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class CollectionReference internal constructor(override val ios: FIRCollectionReference) : Query(ios) {
    public actual val id: String get() = ios.collectionID
    public actual val path: String get() = ios.path
    public actual val parent: DocumentReference? get() = ios.parent?.let { DocumentReference(it) }

    public actual fun document(): DocumentReference = DocumentReference(ios.documentWithAutoID())

    public actual fun document(documentPath: String): DocumentReference = DocumentReference(ios.documentWithPath(documentPath))

    /** Completes once the write is acknowledged, as on Android (addDocumentWithData returns the reference before that). */
    public actual fun add(data: Any): Task<DocumentReference> = task { completion ->
        val reference = ios.documentWithAutoID()
        reference.setData(data.toIosData()) { error -> completion(if (error == null) DocumentReference(reference) else null, error) }
    }
}

public actual class AggregateQuery internal constructor(public actual val query: Query, public actual val aggregateFields: List<AggregateField>) {
    public actual fun get(source: AggregateSource): Task<AggregateQuerySnapshot> {
        val fields = aggregateFields.map { it to it.toIos() }
        return task { completion ->
            query.ios.aggregate(fields.map { it.second }).aggregationWithSource(FIRAggregateSource.FIRAggregateSourceServer) { snapshot: FIRAggregateQuerySnapshot?, error ->
                completion(
                    snapshot?.let { AggregateQuerySnapshot(this, fields.associate { (field, ios) -> field.alias to (it.valueForAggregateField(ios) as? NSNumber)?.toCompatNumber() }) },
                    error,
                )
            }
        }
    }

    private fun AggregateField.toIos(): FIRAggregateField = when (this) {
        is AggregateField.SumAggregateField -> FIRAggregateField.aggregateFieldForSumOfFieldPath(aggregatedField!!.toIos())
        is AggregateField.AverageAggregateField -> FIRAggregateField.aggregateFieldForAverageOfFieldPath(aggregatedField!!.toIos())
        else -> FIRAggregateField.aggregateFieldForCount()
    }

    /** The iOS SDK reports every aggregate as a number; integral ones read as [Long] like the Android SDK's. */
    private fun NSNumber.toCompatNumber(): Number = if (doubleValue == longLongValue.toDouble()) longLongValue else doubleValue

    override fun equals(other: Any?): Boolean = other is AggregateQuery && other.query == query && other.aggregateFields == aggregateFields

    override fun hashCode(): Int = 31 * query.hashCode() + aggregateFields.hashCode()

    override fun toString(): String = "AggregateQuery($aggregateFields over $query)"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class DocumentReference internal constructor(public val ios: FIRDocumentReference) {
    public actual val firestore: FirebaseFirestore get() = FirebaseFirestore(ios.firestore)
    public actual val id: String get() = ios.documentID
    public actual val path: String get() = ios.path
    public actual val parent: CollectionReference get() = CollectionReference(ios.parent)

    public actual fun collection(collectionPath: String): CollectionReference = CollectionReference(ios.collectionWithPath(collectionPath))

    public actual fun get(): Task<DocumentSnapshot> = get(Source.DEFAULT)

    public actual fun get(source: Source): Task<DocumentSnapshot> = task { completion ->
        ios.getDocumentWithSource(source.toIos()) { snapshot, error -> completion(snapshot?.let { DocumentSnapshot(it) }, error) }
    }

    public actual fun set(data: Any): Task<Nothing?> = write { ios.setData(data.toIosData(), it) }

    public actual fun set(data: Any, options: SetOptions): Task<Nothing?> = write {
        if (options.merge) ios.setData(data.toIosData(), true, it) else ios.setData(data.toIosData(), options.iosMergeFields(), it)
    }

    public actual fun update(data: Map<String, Any?>): Task<Nothing?> = write { ios.updateData(data.toIosData(), it) }

    public actual fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = write { ios.updateData(updateData(field, value, moreFieldsAndValues), it) }

    public actual fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = write { ios.updateData(updateData(fieldPath, value, moreFieldsAndValues), it) }

    public actual fun delete(): Task<Nothing?> = write { ios.deleteDocumentWithCompletion(it) }

    public actual fun addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(MetadataChanges.EXCLUDE, listener)

    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration {
        val registration = ios.addSnapshotListenerWithIncludeMetadataChanges(metadataChanges == MetadataChanges.INCLUDE, listener.toIos())
        return ListenerRegistration { registration.remove() }
    }

    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<DocumentSnapshot>): ListenerRegistration {
        val registration = ios.addSnapshotListenerWithOptions(options.toIos(), listener.toIos())
        return ListenerRegistration { registration.remove() }
    }

    private fun EventListener<DocumentSnapshot>.toIos(): (FIRDocumentSnapshot?, NSError?) -> Unit = { snapshot, error ->
        onEvent(snapshot?.let { DocumentSnapshot(it) }, error?.toFirestoreException())
    }

    override fun equals(other: Any?): Boolean = other is DocumentReference && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "DocumentReference($path)"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class Transaction internal constructor(public val ios: FIRTransaction) {
    /** Reads [documentRef] inside the transaction; the nonJsMain `Transaction.get` extension. */
    internal fun getDocument(documentRef: DocumentReference): DocumentSnapshot = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val snapshot = ios.getDocument(documentRef.ios, errorPointer)
        errorPointer.pointed.value?.let { throw it.toFirestoreException() }
        DocumentSnapshot(snapshot!!)
    }

    public actual fun set(documentRef: DocumentReference, data: Any): Transaction = apply { ios.setData(data.toIosData(), documentRef.ios) }

    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): Transaction = apply {
        if (options.merge) ios.setData(data.toIosData(), documentRef.ios, true) else ios.setData(data.toIosData(), documentRef.ios, options.iosMergeFields())
    }

    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): Transaction = apply { ios.updateData(data.toIosData(), documentRef.ios) }

    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = apply {
        ios.updateData(updateData(field, value, moreFieldsAndValues), documentRef.ios)
    }

    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = apply {
        ios.updateData(updateData(fieldPath, value, moreFieldsAndValues), documentRef.ios)
    }

    public actual fun delete(documentRef: DocumentReference): Transaction = apply { ios.deleteDocument(documentRef.ios) }

    public actual fun interface Function<TResult> {
        public actual fun apply(transaction: Transaction): TResult
    }
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class WriteBatch internal constructor(public val ios: FIRWriteBatch) {
    public actual fun set(documentRef: DocumentReference, data: Any): WriteBatch = apply { ios.setData(data.toIosData(), documentRef.ios) }

    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): WriteBatch = apply {
        if (options.merge) ios.setData(data.toIosData(), documentRef.ios, true) else ios.setData(data.toIosData(), documentRef.ios, options.iosMergeFields())
    }

    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): WriteBatch = apply { ios.updateData(data.toIosData(), documentRef.ios) }

    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = apply {
        ios.updateData(updateData(field, value, moreFieldsAndValues), documentRef.ios)
    }

    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = apply {
        ios.updateData(updateData(fieldPath, value, moreFieldsAndValues), documentRef.ios)
    }

    public actual fun delete(documentRef: DocumentReference): WriteBatch = apply { ios.deleteDocument(documentRef.ios) }

    public actual fun commit(): Task<Nothing?> = write { ios.commitWithCompletion(it) }

    public actual fun interface Function {
        public actual fun apply(batch: WriteBatch)
    }
}
