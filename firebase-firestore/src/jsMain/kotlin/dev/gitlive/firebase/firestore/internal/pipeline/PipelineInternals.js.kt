/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal.pipeline

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Pipeline
import com.google.firebase.firestore.PipelineResult
import com.google.firebase.firestore.pipeline.AggregateFunction
import com.google.firebase.firestore.pipeline.AggregateStage
import com.google.firebase.firestore.pipeline.AliasedExpression
import com.google.firebase.firestore.pipeline.CollectionGroupSource
import com.google.firebase.firestore.pipeline.CollectionSource
import com.google.firebase.firestore.pipeline.Expression
import com.google.firebase.firestore.pipeline.Field
import com.google.firebase.firestore.pipeline.FindNearestStage
import com.google.firebase.firestore.pipeline.FunctionExpression
import com.google.firebase.firestore.pipeline.Ordering
import com.google.firebase.firestore.pipeline.RawStage
import com.google.firebase.firestore.pipeline.SampleStage
import com.google.firebase.firestore.pipeline.SearchStage
import com.google.firebase.firestore.pipeline.Selectable
import com.google.firebase.firestore.pipeline.Stage
import com.google.firebase.firestore.pipeline.SubcollectionSource
import com.google.firebase.firestore.pipeline.UnnestStage
import com.google.firebase.firestore.task
import com.google.firebase.firestore.toCompat
import com.google.firebase.firestore.toJs as toJsValue
import dev.gitlive.firebase.firestore.externals.execute
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.AggregateFunction as JsAggregateFunction
import dev.gitlive.firebase.firestore.externals.AliasedAggregate as JsAliasedAggregate
import dev.gitlive.firebase.firestore.externals.AliasedExpression as JsAliasedExpression
import dev.gitlive.firebase.firestore.externals.Expression as JsExpression
import dev.gitlive.firebase.firestore.externals.FunctionExpression as JsFunctionExpression
import dev.gitlive.firebase.firestore.externals.Ordering as JsOrdering
import dev.gitlive.firebase.firestore.externals.Pipeline as JsPipeline
import dev.gitlive.firebase.firestore.externals.PipelineSource as JsPipelineSource
import dev.gitlive.firebase.firestore.externals.constant as jsConstant
import dev.gitlive.firebase.firestore.externals.field as jsField
import dev.gitlive.firebase.firestore.externals.subcollection as jsSubcollection
import dev.gitlive.firebase.firestore.externals.variable as jsVariable

/*
 * The pipeline API over the Firebase JS SDK's firebase/firestore/pipelines entry point. The Kotlin expression tree and
 * stages are converted to the SDK's when the pipeline runs; stage options go to the backend as the SDK's `rawOptions`.
 */

internal actual fun executePipeline(pipeline: Pipeline, options: Pipeline.ExecuteOptions?): Task<Pipeline.Snapshot> = task {
    val js = pipeline.toJs()
    val promise = if (options == null || options.options.isEmpty()) execute(js) else execute(json("pipeline" to js, *options.options.toJsOptions()))
    promise.then { snapshot ->
        Pipeline.Snapshot(snapshot.executionTime.toCompat() as Timestamp, snapshot.results.map { PipelineResult(it) })
    }
}

internal const val SUBCOLLECTION_PIPELINE = "This pipeline was created without a database (e.g., as a subcollection pipeline) and cannot be executed directly. It can only be used as part of another pipeline."

/** `firestore.pipeline()`, which the pipelines entry point adds to the SDK's Firestore. */
private fun com.google.firebase.firestore.FirebaseFirestore.jsPipelineSource(): JsPipelineSource = this.js.asDynamic().pipeline().unsafeCast<JsPipelineSource>()

internal fun Expression.toJs(): JsExpression = when (this) {
    is Field -> jsField(fieldPath.toJsValue())
    is Constant -> jsConstant(value.toJsValue())
    is BooleanConstant -> jsConstant(value)
    is Variable -> jsVariable(name)
    is PipelineExpression -> throw IllegalArgumentException("A pipeline is used in an expression through toArrayExpression() or toScalarExpression()")
    is BooleanFunctionExpression -> JsFunctionExpression(name, params.map { it.toJs() }.toTypedArray()).asBoolean()
    is FunctionExpression -> pipelineValue() ?: JsFunctionExpression(name, params.map { it.toJs() }.toTypedArray())
    is Selectable -> selectableExpr.toJs()
    else -> throw IllegalArgumentException("Unsupported expression: $this")
}

/** `array(pipeline)` and `scalar(pipeline)`, which the JS SDK builds from the pipeline itself. */
private fun FunctionExpression.pipelineValue(): JsExpression? {
    val pipeline = (params.singleOrNull() as? PipelineExpression)?.pipeline ?: return null
    return when (name) {
        "array" -> pipeline.toJs().toArrayExpression()
        "scalar" -> pipeline.toJs().toScalarExpression()
        else -> null
    }
}

private fun AggregateFunction.toJs(): JsAggregateFunction = JsAggregateFunction(name, params.map { it.toJs() }.toTypedArray())

private fun Ordering.toJs(): JsOrdering = JsOrdering(expr.toJs(), dir.wire, undefined)

/** A selection as the SDK's `Selectable`: a field as is, anything else aliased. */
private fun Map.Entry<String, Expression>.toJsSelectable(): Any = (value as? Field)?.takeIf { it.fieldPath.canonicalPath() == key }?.toJs() ?: JsAliasedExpression(value.toJs(), key, undefined)

private fun Selectable.toJsSelectable(): Any = if (this is Field) toJs() else JsAliasedExpression(selectableExpr.toJs(), selectableAlias, undefined)

/** A stage option as the backend takes it: a field as the SDK's field, a nested map as an object, anything else as is. */
private fun Any?.toJsOption(): Any? = when (this) {
    is Field -> toJs()
    is Expression -> toJs()
    is Map<*, *> -> json(*entries.map { (key, value) -> key.toString() to value.toJsOption() }.toTypedArray())
    is List<*> -> map { it.toJsOption() }.toTypedArray()
    else -> this
}

private fun OptionValues.toJsOptions(): Array<Pair<String, Any?>> = entries.map { (key, value) -> key.toJsOptionKey() to value.toJsOption() }.toTypedArray()

/** The SDK names its options in camel case (`indexMode`) where the wire, and the Android SDK, use snake case. */
private fun String.toJsOptionKey(): String = replace(Regex("_([a-z])")) { it.groupValues[1].uppercase() }

/** The stage's own arguments plus its options as the SDK's `rawOptions`. */
private fun Stage<*>.stageOptions(vararg arguments: Pair<String, Any?>): Json = if (options.isEmpty()) json(*arguments) else json(*arguments, "rawOptions" to json(*options.toJsOptions()))

internal fun Pipeline.toJs(): JsPipeline {
    var pipeline: JsPipeline? = null
    for (stage in stages) pipeline = stage.toJs(pipeline, this)
    return pipeline ?: throw IllegalStateException("A pipeline needs a source stage")
}

private fun Stage<*>.toJs(pipeline: JsPipeline?, owner: Pipeline): JsPipeline {
    fun source(): JsPipelineSource = (owner.firestore ?: throw IllegalStateException(SUBCOLLECTION_PIPELINE)).jsPipelineSource()
    fun current(): JsPipeline = pipeline ?: throw IllegalStateException("A pipeline needs a source stage before a $name stage")
    return when (this) {
        is CollectionSource -> source().collection(stageOptions("collection" to path))
        is CollectionGroupSource -> source().collectionGroup(stageOptions("collectionId" to collectionId))
        is SubcollectionSource -> if (options.isEmpty()) jsSubcollection(path) else throw UnsupportedOperationException("The JS SDK takes no options on a subcollection source")
        is DatabaseSource -> source().database(stageOptions())
        is DocumentsSource -> source().documents(stageOptions("docs" to documents.map { it.js }.toTypedArray()))
        is QuerySource -> source().createFrom(query.js)
        is WhereStage -> current().where(stageOptions("condition" to condition.toJs()))
        is LimitStage -> current().limit(stageOptions("limit" to limit))
        is OffsetStage -> current().offset(stageOptions("offset" to offset))
        is SelectStage -> current().select(stageOptions("selections" to selections.map { it.toJsSelectable() }.toTypedArray()))
        is AddFieldsStage -> current().addFields(stageOptions("fields" to fields.map { it.toJsSelectable() }.toTypedArray()))
        is RemoveFieldsStage -> current().removeFields(stageOptions("fields" to fields.map { it.toJs() }.toTypedArray()))
        is DefineStage -> current().define(stageOptions("variables" to variables.map { (alias, expr) -> JsAliasedExpression(expr.toJs(), alias, undefined) }.toTypedArray()))
        is DistinctStage -> current().distinct(stageOptions("groups" to groups.map { it.toJsSelectable() }.toTypedArray()))
        is SortStage -> current().sort(stageOptions("orderings" to orders.map { it.toJs() }.toTypedArray()))
        is ReplaceWithStage -> current().replaceWith(stageOptions("map" to expr.toJs()))
        is UnionStage -> current().union(stageOptions("other" to other.toJs()))
        is AggregateStage -> current().aggregate(
            stageOptions(
                "accumulators" to accumulators.map { (alias, function) -> JsAliasedAggregate(function.toJs(), alias, undefined) }.toTypedArray(),
                "groups" to groups.map { it.toJsSelectable() }.toTypedArray(),
            ),
        )
        is FindNearestStage -> current().findNearest(
            json(
                "field" to vectorField.toJs(),
                "vectorValue" to ((vectorValue as? Constant)?.value?.toJsValue() ?: vectorValue.toJs()),
                "distanceMeasure" to distanceMeasure.wire,
                *(options["limit"]?.let { arrayOf("limit" to it) } ?: emptyArray()),
                *((options["distance_field"] as? Field)?.let { arrayOf("distanceField" to it.fieldPath.canonicalPath()) } ?: emptyArray()),
                *((options - "limit" - "distance_field").takeIf { it.isNotEmpty() }?.let { arrayOf("rawOptions" to json(*it.toJsOptions())) } ?: emptyArray()),
            ),
        )
        is SampleStage -> current().sample(stageOptions((if (mode == SampleStage.Mode.DOCUMENTS) "documents" else "percentage") to count))
        is UnnestStage -> current().unnest(
            json(
                "selectable" to arrayWithAlias.toJsSelectable(),
                *((options["index_field"] as? Field)?.let { arrayOf("indexField" to it.fieldPath.canonicalPath()) } ?: emptyArray()),
                *((options - "index_field").takeIf { it.isNotEmpty() }?.let { arrayOf("rawOptions" to json(*it.toJsOptions())) } ?: emptyArray()),
            ),
        )
        is SearchStage -> current().search(
            stageOptions(
                "query" to (query?.toJs() ?: rquery),
                *listOfNotNull(
                    languageCode?.let { "languageCode" to it },
                    retrievalDepth?.let { "retrievalDepth" to it },
                    offset?.let { "offset" to it },
                    limit?.let { "limit" to it },
                    sort.takeIf { it.isNotEmpty() }?.let { orderings -> "sort" to orderings.map { it.toJs() }.toTypedArray() },
                    addFields.takeIf { it.isNotEmpty() }?.let { fields -> "addFields" to fields.map { it.toJsSelectable() }.toTypedArray() },
                ).toTypedArray(),
            ),
        )
        is RawStage -> if (options.isEmpty()) current().rawStage(name, arguments.map { toExprOrConstant(it).toJs() }.toTypedArray()) else current().rawStage(name, arguments.map { toExprOrConstant(it).toJs() }.toTypedArray(), json(*options.toJsOptions()))
        else -> throw IllegalArgumentException("Unsupported stage: $name")
    }
}
