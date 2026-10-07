/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task

/**
 * A query over the documents of a collection or collection group, mirroring `com.google.firebase.firestore.Query`
 * from the Firebase Android SDK: immutable, every method returns a new query with the constraint added.
 *
 * Not mirrored: the `Activity` and `Executor` overloads of `addSnapshotListener` (see `api/android-sdk/exclusions.txt`).
 */
public expect open class Query {
    /** The [FirebaseFirestore] this query belongs to. */
    public val firestore: FirebaseFirestore

    public fun whereEqualTo(field: String, value: Any?): Query
    public fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query
    public fun whereNotEqualTo(field: String, value: Any?): Query
    public fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query
    public fun whereLessThan(field: String, value: Any): Query
    public fun whereLessThan(fieldPath: FieldPath, value: Any): Query
    public fun whereLessThanOrEqualTo(field: String, value: Any): Query
    public fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query
    public fun whereGreaterThan(field: String, value: Any): Query
    public fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query
    public fun whereGreaterThanOrEqualTo(field: String, value: Any): Query
    public fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query
    public fun whereArrayContains(field: String, value: Any): Query
    public fun whereArrayContains(fieldPath: FieldPath, value: Any): Query
    public fun whereArrayContainsAny(field: String, values: List<Any>): Query
    public fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query
    public fun whereIn(field: String, values: List<Any>): Query
    public fun whereIn(fieldPath: FieldPath, values: List<Any>): Query
    public fun whereNotIn(field: String, values: List<Any>): Query
    public fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query

    /** The query with [filter] added, which may combine conditions with `Filter.and` and `Filter.or`. */
    public fun where(filter: Filter): Query

    public fun orderBy(field: String): Query
    public fun orderBy(fieldPath: FieldPath): Query
    public fun orderBy(field: String, direction: Direction): Query
    public fun orderBy(fieldPath: FieldPath, direction: Direction): Query

    /** The query limited to the first [limit] results. */
    public fun limit(limit: Long): Query

    /** The query limited to the last [limit] results; requires an `orderBy`. */
    public fun limitToLast(limit: Long): Query

    public fun startAt(snapshot: DocumentSnapshot): Query
    public fun startAt(vararg fieldValues: Any?): Query
    public fun startAfter(snapshot: DocumentSnapshot): Query
    public fun startAfter(vararg fieldValues: Any?): Query
    public fun endBefore(snapshot: DocumentSnapshot): Query
    public fun endBefore(vararg fieldValues: Any?): Query
    public fun endAt(snapshot: DocumentSnapshot): Query
    public fun endAt(vararg fieldValues: Any?): Query

    /** Reads the results, from the cache and the backend as available. */
    public fun get(): Task<QuerySnapshot>

    /** Reads the results from [source]. */
    public fun get(source: Source): Task<QuerySnapshot>

    /** Listens to the results and every change to them. */
    public fun addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration

    /** Listens to the results, also notifying [listener] of metadata changes when [metadataChanges] is `INCLUDE`. */
    public fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration

    /** Listens to the results with [options]. */
    public fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<QuerySnapshot>): ListenerRegistration

    /** An [AggregateQuery] counting the results. */
    public fun count(): AggregateQuery

    /** An [AggregateQuery] computing [aggregateField] and [aggregateFields] over the results. */
    public fun aggregate(aggregateField: AggregateField, vararg aggregateFields: AggregateField): AggregateQuery

    public enum class Direction {
        ASCENDING,
        DESCENDING,
    }
}

/** A collection, mirroring the Android SDK's `CollectionReference`: a [Query] over its documents that can also add to it. */
public expect class CollectionReference : Query {
    /** The last segment of the path. */
    public val id: String

    /** The path from the root, `/`-separated. */
    public val path: String

    /** The document containing this collection, or null for a root collection. */
    public val parent: DocumentReference?

    /** A reference to a new document with an automatically generated id. */
    public fun document(): DocumentReference

    /** A reference to the document at [documentPath], relative to this collection. */
    public fun document(documentPath: String): DocumentReference

    /** Adds a new document with an automatically generated id and [data] (a map of field values). */
    public fun add(data: Any): Task<DocumentReference>
}

/** An aggregation over the results of a [Query], mirroring the Android SDK's `AggregateQuery`. */
public expect class AggregateQuery {
    /** The query aggregated over. */
    public val query: Query

    /** The aggregations computed. */
    public val aggregateFields: List<AggregateField>

    /** Computes the aggregations on [source]. */
    public fun get(source: AggregateSource): Task<AggregateQuerySnapshot>
}

/** The results of an [AggregateQuery], mirroring the Android SDK's `AggregateQuerySnapshot`. */
public expect class AggregateQuerySnapshot {
    /** The query the results belong to. */
    public val query: AggregateQuery

    /** The result of `count()`. */
    public val count: Long

    /** The result of [aggregateField]: a number, or null for an average over no values. */
    public fun get(aggregateField: AggregateField): Any?

    /** The result of an average, or null over no values. */
    public fun get(averageAggregateField: AggregateField.AverageAggregateField): Double?

    /** The result of a count. */
    public fun get(countAggregateField: AggregateField.CountAggregateField): Long

    /** The result of [aggregateField] as a [Double]. */
    public fun getDouble(aggregateField: AggregateField): Double?

    /** The result of [aggregateField] as a [Long]. */
    public fun getLong(aggregateField: AggregateField): Long?
}

/** An aggregation of a field, mirroring the Android SDK's `AggregateField`; created with [count], [sum] and [average]. */
public expect abstract class AggregateField {
    /** The name of the result in an [AggregateQuerySnapshot]. */
    public val alias: String

    /** The aggregated field, `.`-separated, or empty for a count. */
    public val fieldPath: String

    /** The aggregation operator: `count`, `sum` or `average`. */
    public val operator: String

    public class CountAggregateField : AggregateField
    public class SumAggregateField : AggregateField
    public class AverageAggregateField : AggregateField

    public companion object {
        public fun count(): CountAggregateField
        public fun sum(field: String): SumAggregateField
        public fun sum(fieldPath: FieldPath): SumAggregateField
        public fun average(field: String): AverageAggregateField
        public fun average(fieldPath: FieldPath): AverageAggregateField
    }
}
