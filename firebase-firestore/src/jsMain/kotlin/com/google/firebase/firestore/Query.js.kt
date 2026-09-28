/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.firestore.externals.QueryConstraint
import dev.gitlive.firebase.firestore.externals.addDoc
import dev.gitlive.firebase.firestore.externals.deleteDoc
import dev.gitlive.firebase.firestore.externals.doc
import dev.gitlive.firebase.firestore.externals.getAggregateFromServer
import dev.gitlive.firebase.firestore.externals.getDoc
import dev.gitlive.firebase.firestore.externals.getDocFromCache
import dev.gitlive.firebase.firestore.externals.getDocFromServer
import dev.gitlive.firebase.firestore.externals.getDocs
import dev.gitlive.firebase.firestore.externals.getDocsFromCache
import dev.gitlive.firebase.firestore.externals.getDocsFromServer
import dev.gitlive.firebase.firestore.externals.onSnapshot
import dev.gitlive.firebase.firestore.externals.query
import dev.gitlive.firebase.firestore.externals.queryEqual
import dev.gitlive.firebase.firestore.externals.refEqual
import dev.gitlive.firebase.firestore.externals.setDoc
import dev.gitlive.firebase.firestore.externals.updateDoc
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.AggregateField as JsAggregateField
import dev.gitlive.firebase.firestore.externals.CollectionReference as JsCollectionReference
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.Query as JsQuery
import dev.gitlive.firebase.firestore.externals.Transaction as JsTransaction
import dev.gitlive.firebase.firestore.externals.WriteBatch as JsWriteBatch
import dev.gitlive.firebase.firestore.externals.average as jsAverage
import dev.gitlive.firebase.firestore.externals.collection as jsCollection
import dev.gitlive.firebase.firestore.externals.count as jsCount
import dev.gitlive.firebase.firestore.externals.endAt as jsEndAt
import dev.gitlive.firebase.firestore.externals.endBefore as jsEndBefore
import dev.gitlive.firebase.firestore.externals.limit as jsLimit
import dev.gitlive.firebase.firestore.externals.limitToLast as jsLimitToLast
import dev.gitlive.firebase.firestore.externals.orderBy as jsOrderBy
import dev.gitlive.firebase.firestore.externals.startAfter as jsStartAfter
import dev.gitlive.firebase.firestore.externals.startAt as jsStartAt
import dev.gitlive.firebase.firestore.externals.sum as jsSum
import dev.gitlive.firebase.firestore.externals.where as jsWhere

/** The `includeMetadataChanges` and `source` options of a snapshot listener as the JS SDK takes them. */
private fun listenOptions(metadataChanges: MetadataChanges, source: ListenSource = ListenSource.DEFAULT): Json = json("includeMetadataChanges" to (metadataChanges == MetadataChanges.INCLUDE), "source" to source.name.lowercase())

private fun Array<out Any?>.toJs(): Array<Any> = map { it.toJs().unsafeCast<Any>() }.toTypedArray()

/** @property js The underlying Firebase JS SDK object. */
public actual open class Query internal constructor(public open val js: JsQuery) {
    public actual val firestore: FirebaseFirestore get() = FirebaseFirestore.wrap(js.firestore)

    private fun constrained(constraint: QueryConstraint): Query = rethrow { Query(query(js, constraint)) }

    private fun where(field: String, operator: String, value: Any?): Query = constrained(jsWhere(field, operator, value.toJs()))

    private fun where(fieldPath: FieldPath, operator: String, value: Any?): Query = constrained(jsWhere(fieldPath.toJs(), operator, value.toJs()))

    public actual fun whereEqualTo(field: String, value: Any?): Query = where(field, "==", value)
    public actual fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query = where(fieldPath, "==", value)
    public actual fun whereNotEqualTo(field: String, value: Any?): Query = where(field, "!=", value)
    public actual fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query = where(fieldPath, "!=", value)
    public actual fun whereLessThan(field: String, value: Any): Query = where(field, "<", value)
    public actual fun whereLessThan(fieldPath: FieldPath, value: Any): Query = where(fieldPath, "<", value)
    public actual fun whereLessThanOrEqualTo(field: String, value: Any): Query = where(field, "<=", value)
    public actual fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = where(fieldPath, "<=", value)
    public actual fun whereGreaterThan(field: String, value: Any): Query = where(field, ">", value)
    public actual fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query = where(fieldPath, ">", value)
    public actual fun whereGreaterThanOrEqualTo(field: String, value: Any): Query = where(field, ">=", value)
    public actual fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = where(fieldPath, ">=", value)
    public actual fun whereArrayContains(field: String, value: Any): Query = where(field, "array-contains", value)
    public actual fun whereArrayContains(fieldPath: FieldPath, value: Any): Query = where(fieldPath, "array-contains", value)
    public actual fun whereArrayContainsAny(field: String, values: List<Any>): Query = where(field, "array-contains-any", values)
    public actual fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query = where(fieldPath, "array-contains-any", values)
    public actual fun whereIn(field: String, values: List<Any>): Query = where(field, "in", values)
    public actual fun whereIn(fieldPath: FieldPath, values: List<Any>): Query = where(fieldPath, "in", values)
    public actual fun whereNotIn(field: String, values: List<Any>): Query = where(field, "not-in", values)
    public actual fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query = where(fieldPath, "not-in", values)

    public actual fun where(filter: Filter): Query = filter.node?.let { constrained(it.toJs()) } ?: this

    public actual fun orderBy(field: String): Query = orderBy(field, Direction.ASCENDING)
    public actual fun orderBy(fieldPath: FieldPath): Query = orderBy(fieldPath, Direction.ASCENDING)
    public actual fun orderBy(field: String, direction: Direction): Query = constrained(jsOrderBy(field, direction.js))
    public actual fun orderBy(fieldPath: FieldPath, direction: Direction): Query = constrained(jsOrderBy(fieldPath.toJs(), direction.js))

    public actual fun limit(limit: Long): Query = constrained(jsLimit(limit.toInt()))
    public actual fun limitToLast(limit: Long): Query = constrained(jsLimitToLast(limit.toInt()))

    public actual fun startAt(snapshot: DocumentSnapshot): Query = constrained(jsStartAt(snapshot.js))
    public actual fun startAt(vararg fieldValues: Any?): Query = constrained(jsStartAt(*fieldValues.toJs()))
    public actual fun startAfter(snapshot: DocumentSnapshot): Query = constrained(jsStartAfter(snapshot.js))
    public actual fun startAfter(vararg fieldValues: Any?): Query = constrained(jsStartAfter(*fieldValues.toJs()))
    public actual fun endBefore(snapshot: DocumentSnapshot): Query = constrained(jsEndBefore(snapshot.js))
    public actual fun endBefore(vararg fieldValues: Any?): Query = constrained(jsEndBefore(*fieldValues.toJs()))
    public actual fun endAt(snapshot: DocumentSnapshot): Query = constrained(jsEndAt(snapshot.js))
    public actual fun endAt(vararg fieldValues: Any?): Query = constrained(jsEndAt(*fieldValues.toJs()))

    public actual fun get(): Task<QuerySnapshot> = get(Source.DEFAULT)

    public actual fun get(source: Source): Task<QuerySnapshot> = task {
        when (source) {
            Source.DEFAULT -> getDocs(js)
            Source.CACHE -> getDocsFromCache(js)
            Source.SERVER -> getDocsFromServer(js)
        }.then { QuerySnapshot(it) }
    }

    public actual fun addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(MetadataChanges.EXCLUDE, listener)

    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = listen(listenOptions(metadataChanges), listener)

    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<QuerySnapshot>): ListenerRegistration = listen(listenOptions(options.metadataChanges, options.source), listener)

    private fun listen(options: Json, listener: EventListener<QuerySnapshot>): ListenerRegistration {
        val unsubscribe = rethrow { onSnapshot(js, options, { listener.onEvent(QuerySnapshot(it), null) }, { listener.onEvent(null, it.toFirestoreException()) }) }
        return ListenerRegistration { rethrow { unsubscribe() } }
    }

    public actual fun count(): AggregateQuery = AggregateQuery(this, listOf(AggregateField.count()))

    public actual fun aggregate(aggregateField: AggregateField, vararg aggregateFields: AggregateField): AggregateQuery = AggregateQuery(this, listOf(aggregateField) + aggregateFields)

    /** The JS SDK creates a new object per call, so queries are equal when they describe the same collection and constraints. */
    override fun equals(other: Any?): Boolean = other is Query && (other.js === js || queryEqual(js, other.js))

    override fun hashCode(): Int = js.firestore.hashCode()

    public actual enum class Direction(internal val js: String) {
        ASCENDING("asc"),
        DESCENDING("desc"),
    }
}

/** @property js The underlying Firebase JS SDK object. */
public actual class CollectionReference internal constructor(override val js: JsCollectionReference) : Query(js) {
    public actual val id: String get() = js.id
    public actual val path: String get() = js.path
    public actual val parent: DocumentReference? get() = js.parent?.let { DocumentReference(it) }

    public actual fun document(): DocumentReference = rethrow { DocumentReference(doc(js)) }

    public actual fun document(documentPath: String): DocumentReference = rethrow { DocumentReference(doc(js, documentPath)) }

    public actual fun add(data: Any): Task<DocumentReference> = task { addDoc(js, data.toJs()!!).then { DocumentReference(it) } }

    override fun toString(): String = "CollectionReference($path)"
}

public actual class AggregateQuery internal constructor(public actual val query: Query, public actual val aggregateFields: List<AggregateField>) {
    public actual fun get(source: AggregateSource): Task<AggregateQuerySnapshot> = task {
        val spec = json(*aggregateFields.map { it.alias to it.toJs() }.toTypedArray())
        getAggregateFromServer(query.js, spec).then { snapshot ->
            val data = snapshot.data()
            AggregateQuerySnapshot(this, aggregateFields.associate { it.alias to data[it.alias].unsafeCast<Any?>()?.toCompat() })
        }
    }

    private fun AggregateField.toJs(): JsAggregateField = when (this) {
        is AggregateField.SumAggregateField -> jsSum(aggregatedField!!.toJs())
        is AggregateField.AverageAggregateField -> jsAverage(aggregatedField!!.toJs())
        else -> jsCount()
    }

    override fun equals(other: Any?): Boolean = other is AggregateQuery && other.query == query && other.aggregateFields == aggregateFields

    override fun hashCode(): Int = 31 * query.hashCode() + aggregateFields.hashCode()

    override fun toString(): String = "AggregateQuery($aggregateFields over $query)"
}

/** @property js The underlying Firebase JS SDK object. */
public actual class DocumentReference internal constructor(public val js: JsDocumentReference) {
    public actual val firestore: FirebaseFirestore get() = FirebaseFirestore.wrap(js.firestore)
    public actual val id: String get() = js.id
    public actual val path: String get() = js.path
    public actual val parent: CollectionReference get() = CollectionReference(js.parent)

    public actual fun collection(collectionPath: String): CollectionReference = rethrow { CollectionReference(jsCollection(js, collectionPath)) }

    public actual fun get(): Task<DocumentSnapshot> = get(Source.DEFAULT)

    public actual fun get(source: Source): Task<DocumentSnapshot> = task {
        when (source) {
            Source.DEFAULT -> getDoc(js)
            Source.CACHE -> getDocFromCache(js)
            Source.SERVER -> getDocFromServer(js)
        }.then { DocumentSnapshot(it) }
    }

    public actual fun set(data: Any): Task<Nothing?> = write { setDoc(js, data.toJs()!!) }

    public actual fun set(data: Any, options: SetOptions): Task<Nothing?> = write { setDoc(js, data.toJs()!!, options.toJs()) }

    public actual fun update(data: Map<String, Any?>): Task<Nothing?> = write { updateDoc(js, data.toJs()!!) }

    public actual fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = write { updateDoc(js, field, value.toJs(), *moreFieldsAndValues.toJs()) }

    public actual fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = write { updateDoc(js, fieldPath.toJs(), value.toJs(), *moreFieldsAndValues.toJs()) }

    public actual fun delete(): Task<Nothing?> = write { deleteDoc(js) }

    public actual fun addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(MetadataChanges.EXCLUDE, listener)

    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = listen(listenOptions(metadataChanges), listener)

    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<DocumentSnapshot>): ListenerRegistration = listen(listenOptions(options.metadataChanges, options.source), listener)

    private fun listen(options: Json, listener: EventListener<DocumentSnapshot>): ListenerRegistration {
        val unsubscribe = rethrow { onSnapshot(js, options, { listener.onEvent(DocumentSnapshot(it), null) }, { listener.onEvent(null, it.toFirestoreException()) }) }
        return ListenerRegistration { rethrow { unsubscribe() } }
    }

    override fun equals(other: Any?): Boolean = other is DocumentReference && (other.js === js || refEqual(js, other.js))

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "DocumentReference($path)"
}

/** @property js The underlying Firebase JS SDK object. */
public actual class Transaction internal constructor(public val js: JsTransaction) {
    public actual fun set(documentRef: DocumentReference, data: Any): Transaction = rethrow { js.set(documentRef.js, data.toJs()!!) }.let { this }

    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): Transaction = rethrow { js.set(documentRef.js, data.toJs()!!, options.toJs()) }.let { this }

    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): Transaction = rethrow { js.update(documentRef.js, data.toJs()!!) }.let { this }

    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = rethrow { js.update(documentRef.js, field, value.toJs(), *moreFieldsAndValues.toJs()) }.let { this }

    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = rethrow { js.update(documentRef.js, fieldPath.toJs(), value.toJs(), *moreFieldsAndValues.toJs()) }.let { this }

    public actual fun delete(documentRef: DocumentReference): Transaction = rethrow { js.delete(documentRef.js) }.let { this }

    public actual fun interface Function<TResult> {
        public actual fun apply(transaction: Transaction): TResult
    }
}

/** @property js The underlying Firebase JS SDK object. */
public actual class WriteBatch internal constructor(public val js: JsWriteBatch) {
    public actual fun set(documentRef: DocumentReference, data: Any): WriteBatch = rethrow { js.set(documentRef.js, data.toJs()!!) }.let { this }

    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): WriteBatch = rethrow { js.set(documentRef.js, data.toJs()!!, options.toJs()) }.let { this }

    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): WriteBatch = rethrow { js.update(documentRef.js, data.toJs()!!) }.let { this }

    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = rethrow { js.update(documentRef.js, field, value.toJs(), *moreFieldsAndValues.toJs()) }.let { this }

    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = rethrow { js.update(documentRef.js, fieldPath.toJs(), value.toJs(), *moreFieldsAndValues.toJs()) }.let { this }

    public actual fun delete(documentRef: DocumentReference): WriteBatch = rethrow { js.delete(documentRef.js) }.let { this }

    public actual fun commit(): Task<Nothing?> = write { js.commit() }

    public actual fun interface Function {
        public actual fun apply(batch: WriteBatch)
    }
}
