/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.pipeline.AbstractOptions
import com.google.firebase.firestore.pipeline.AggregateFunction
import com.google.firebase.firestore.pipeline.AggregateOptions
import com.google.firebase.firestore.pipeline.AggregateStage
import com.google.firebase.firestore.pipeline.AliasedAggregate
import com.google.firebase.firestore.pipeline.AliasedExpression
import com.google.firebase.firestore.pipeline.BooleanExpression
import com.google.firebase.firestore.pipeline.CollectionGroupOptions
import com.google.firebase.firestore.pipeline.CollectionGroupSource
import com.google.firebase.firestore.pipeline.CollectionSource
import com.google.firebase.firestore.pipeline.CollectionSourceOptions
import com.google.firebase.firestore.pipeline.Expression
import com.google.firebase.firestore.pipeline.Field
import com.google.firebase.firestore.pipeline.FindNearestOptions
import com.google.firebase.firestore.pipeline.FindNearestStage
import com.google.firebase.firestore.pipeline.Ordering
import com.google.firebase.firestore.pipeline.RawStage
import com.google.firebase.firestore.pipeline.SampleStage
import com.google.firebase.firestore.pipeline.SearchStage
import com.google.firebase.firestore.pipeline.Selectable
import com.google.firebase.firestore.pipeline.Stage
import com.google.firebase.firestore.pipeline.SubcollectionSource
import com.google.firebase.firestore.pipeline.UnnestOptions
import com.google.firebase.firestore.pipeline.UnnestStage
import dev.gitlive.firebase.firestore.INTERNAL_CONSTRUCTOR
import dev.gitlive.firebase.firestore.internal.pipeline.AddFieldsStage
import dev.gitlive.firebase.firestore.internal.pipeline.DatabaseSource
import dev.gitlive.firebase.firestore.internal.pipeline.DefineStage
import dev.gitlive.firebase.firestore.internal.pipeline.DistinctStage
import dev.gitlive.firebase.firestore.internal.pipeline.DocumentsSource
import dev.gitlive.firebase.firestore.internal.pipeline.LimitStage
import dev.gitlive.firebase.firestore.internal.pipeline.OffsetStage
import dev.gitlive.firebase.firestore.internal.pipeline.OptionValues
import dev.gitlive.firebase.firestore.internal.pipeline.QuerySource
import dev.gitlive.firebase.firestore.internal.pipeline.RemoveFieldsStage
import dev.gitlive.firebase.firestore.internal.pipeline.ReplaceWithStage
import dev.gitlive.firebase.firestore.internal.pipeline.SelectStage
import dev.gitlive.firebase.firestore.internal.pipeline.SortStage
import dev.gitlive.firebase.firestore.internal.pipeline.UnionStage
import dev.gitlive.firebase.firestore.internal.pipeline.WhereStage
import dev.gitlive.firebase.firestore.internal.pipeline.executePipeline
import dev.gitlive.firebase.firestore.internal.pipeline.function
import dev.gitlive.firebase.firestore.internal.pipeline.selections
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/*
 * The Firestore pipeline API, as the Firebase Android SDK's: a source stage from a PipelineSource, transformation stages
 * added by the Pipeline functions, and execute(). The stages are kept as Kotlin objects (see the pipeline package) and
 * converted to the platform SDK's when the pipeline runs: the Apple SDK's bridge classes and the JS SDK's
 * firebase/firestore/pipelines entry point. On Android and the JVM these classes are header stubs over the SDK's own.
 */

/** A sequence of stages that transforms a set of documents; built from a [PipelineSource] and run with [execute]. */
public class Pipeline
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val firestore: FirebaseFirestore?,
    internal val stages: List<Stage<*>>,
) {

    @Suppress("DEPRECATION_ERROR")
    private fun append(stage: Stage<*>): Pipeline = Pipeline(firestore, stages + stage)

    public fun addFields(field: Selectable, vararg additionalFields: Selectable): Pipeline = append(AddFieldsStage(selections(field, additionalFields), emptyMap()))

    public fun removeFields(field: Field, vararg additionalFields: Field): Pipeline = append(RemoveFieldsStage(listOf(field) + additionalFields, emptyMap()))

    public fun removeFields(field: String, vararg additionalFields: String): Pipeline = append(RemoveFieldsStage((listOf(field) + additionalFields).map { Field.of(it) }, emptyMap()))

    public fun select(selection: Selectable, vararg additionalSelections: Any): Pipeline = append(SelectStage(selections(selection, additionalSelections), emptyMap()))

    public fun select(fieldName: String, vararg additionalSelections: Any): Pipeline = append(SelectStage(selections(fieldName, additionalSelections), emptyMap()))

    public fun where(condition: BooleanExpression): Pipeline = append(WhereStage(condition, emptyMap()))

    public fun offset(offset: Int): Pipeline = append(OffsetStage(offset, emptyMap()))

    public fun limit(limit: Int): Pipeline = append(LimitStage(limit, emptyMap()))

    public fun distinct(group: Selectable, vararg additionalGroups: Any): Pipeline = append(DistinctStage(selections(group, additionalGroups), emptyMap()))

    public fun distinct(groupField: String, vararg additionalGroups: Any): Pipeline = append(DistinctStage(selections(groupField, additionalGroups), emptyMap()))

    public fun aggregate(accumulator: AliasedAggregate, vararg additionalAccumulators: AliasedAggregate): Pipeline = append(AggregateStage.withAccumulators(accumulator, *additionalAccumulators))

    public fun aggregate(aggregateStage: AggregateStage): Pipeline = append(aggregateStage)

    public fun aggregate(aggregateStage: AggregateStage, options: AggregateOptions): Pipeline = append(aggregateStage.withOptions(options))

    public fun findNearest(vectorField: String, vectorValue: DoubleArray, distanceMeasure: FindNearestStage.DistanceMeasure): Pipeline = findNearest(Field.of(vectorField), vectorValue, distanceMeasure)

    @Suppress("DEPRECATION_ERROR")
    public fun findNearest(vectorField: Field, vectorValue: DoubleArray, distanceMeasure: FindNearestStage.DistanceMeasure): Pipeline = append(FindNearestStage(vectorField, Expression.vector(vectorValue), distanceMeasure, emptyMap()))

    public fun findNearest(vectorField: String, vectorValue: Expression, distanceMeasure: FindNearestStage.DistanceMeasure, options: FindNearestOptions): Pipeline = append(FindNearestStage(Field.of(vectorField), vectorValue, distanceMeasure, options.options))

    public fun search(searchStage: SearchStage): Pipeline = append(searchStage)

    public fun sort(order: Ordering, vararg additionalOrders: Ordering): Pipeline = append(SortStage(listOf(order) + additionalOrders, emptyMap()))

    public fun replaceWith(field: String): Pipeline = append(ReplaceWithStage(Field.of(field), emptyMap()))

    public fun replaceWith(mapValue: Expression): Pipeline = append(ReplaceWithStage(mapValue, emptyMap()))

    public fun sample(documents: Int): Pipeline = append(SampleStage.withDocLimit(documents))

    public fun sample(sample: SampleStage): Pipeline = append(sample)

    public fun union(other: Pipeline): Pipeline = append(UnionStage(other, emptyMap()))

    public fun unnest(arrayField: String, alias: String): Pipeline = append(UnnestStage.withField(arrayField, alias))

    public fun unnest(arrayWithAlias: Selectable): Pipeline = append(UnnestStage.withField(arrayWithAlias))

    public fun unnest(arrayWithAlias: Selectable, options: UnnestOptions): Pipeline = append(UnnestStage.withField(arrayWithAlias).withOptions(options))

    public fun unnest(unnestStage: UnnestStage): Pipeline = append(unnestStage)

    public fun define(aliasedExpression: AliasedExpression, vararg additionalExpressions: AliasedExpression): Pipeline = append(DefineStage((listOf(aliasedExpression) + additionalExpressions).associate { it.alias to it.expr }, emptyMap()))

    public fun rawStage(rawStage: RawStage): Pipeline = append(rawStage)

    /** This pipeline's results as an array, for use in an enclosing pipeline's expression. */
    public fun toArrayExpression(): Expression = function("array", this)

    /** This pipeline's single result, for use in an enclosing pipeline's expression. */
    public fun toScalarExpression(): Expression = function("scalar", this)

    /** Runs the pipeline and completes with its results. */
    public fun execute(): Task<Snapshot> = executePipeline(this, null)

    /** Runs the pipeline with [options] and completes with its results. */
    public fun execute(options: ExecuteOptions): Task<Snapshot> = executePipeline(this, options)

    /** Options of [execute]. */
    @Suppress("DEPRECATION_ERROR")
    public class ExecuteOptions private constructor(options: OptionValues) : AbstractOptions<ExecuteOptions>(options) {
        public constructor() : this(emptyMap())

        override fun self(options: OptionValues): ExecuteOptions = ExecuteOptions(options)

        public fun withIndexMode(indexMode: IndexMode): ExecuteOptions = with("index_mode", indexMode.wire)

        /** How the backend uses indexes to run the pipeline. */
        public class IndexMode private constructor(internal val wire: String) {
            override fun toString(): String = wire

            public companion object {
                /** The backend picks the indexes it recommends. */
                @JvmField
                public val RECOMMENDED: IndexMode = IndexMode("recommended")
            }
        }
    }

    /** The results of an execution, in order. */
    public class Snapshot public constructor(public val executionTime: Timestamp, public val results: List<PipelineResult>) : Iterable<PipelineResult> {
        override fun iterator(): Iterator<PipelineResult> = results.iterator()
    }
}

/** A stage with [options] added to its own. */
private fun <T : Stage<T>> Stage<T>.withOptions(options: AbstractOptions<*>): T = self(this.options + options.options)

/** A document, or a computed row, of a [Pipeline.Snapshot]. */
public expect class PipelineResult {
    /** The document, or null for a row that is not a document. */
    public val ref: DocumentReference?

    /** The document's id, or null for a row that is not a document. */
    public fun getId(): String?

    public val createTime: Timestamp?

    public val updateTime: Timestamp?

    /** The row's fields. */
    public fun getData(): Map<String, Any?>

    /** The value at [field], a `.`-separated path, or null. */
    public fun get(field: String): Any?

    public fun get(fieldPath: FieldPath): Any?
}

/** The source stages a [Pipeline] can start from, from [FirebaseFirestore.pipeline]. */
public class PipelineSource public constructor(internal val firestore: FirebaseFirestore) {

    @Suppress("DEPRECATION_ERROR")
    private fun from(source: Stage<*>): Pipeline = Pipeline(firestore, listOf(source))

    public fun createFrom(query: Query): Pipeline = from(QuerySource(query, emptyMap()))

    /** The query's pipeline with an `aggregate` stage of its aggregate fields, as the Android SDK converts it. */
    public fun createFrom(aggregateQuery: AggregateQuery): Pipeline {
        val accumulators = aggregateQuery.aggregateFields.map { field ->
            when (field) {
                is AggregateField.CountAggregateField -> AggregateFunction.countAll()
                is AggregateField.SumAggregateField -> AggregateFunction.sum(field.fieldPath)
                is AggregateField.AverageAggregateField -> AggregateFunction.average(field.fieldPath)
                else -> throw IllegalArgumentException("Unsupported aggregate field: ${field.operator}")
            }.alias(field.alias)
        }
        return createFrom(aggregateQuery.query).aggregate(accumulators.first(), *accumulators.drop(1).toTypedArray())
    }

    public fun collection(path: String): Pipeline = from(CollectionSource(path, emptyMap()))

    public fun collection(ref: CollectionReference): Pipeline = from(CollectionSource(ref.path, emptyMap()))

    public fun collection(ref: CollectionReference, options: CollectionSourceOptions): Pipeline = from(CollectionSource(ref.path, options.options))

    public fun collectionGroup(collectionId: String): Pipeline = from(CollectionGroupSource(collectionId, emptyMap()))

    public fun collectionGroup(collectionId: String, options: CollectionGroupOptions): Pipeline = from(CollectionGroupSource(collectionId, options.options))

    public fun database(): Pipeline = from(DatabaseSource(emptyMap()))

    public fun documents(vararg documents: String): Pipeline = from(DocumentsSource(documents.map { firestore.document(it) }, emptyMap()))

    public fun documents(vararg documents: DocumentReference): Pipeline = from(DocumentsSource(documents.toList(), emptyMap()))

    public companion object {
        /** A pipeline over a subcollection of the enclosing pipeline's documents, for `union` and `unnest`; it has no database and cannot be executed on its own. */
        @JvmStatic
        @Suppress("DEPRECATION_ERROR")
        public fun subcollection(path: String): Pipeline = Pipeline(null, listOf(SubcollectionSource.of(path)))

        @JvmStatic
        @Suppress("DEPRECATION_ERROR")
        public fun subcollection(source: SubcollectionSource): Pipeline = Pipeline(null, listOf(source))
    }
}
