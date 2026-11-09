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
import dev.gitlive.firebase.externals.jsArrayOf
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.firestore.externals.execute
import dev.gitlive.firebase.firestore.externals.AggregateFunction as JsAggregateFunction
import dev.gitlive.firebase.firestore.externals.AliasedAggregate as JsAliasedAggregate
import dev.gitlive.firebase.firestore.externals.AliasedExpression as JsAliasedExpression
import dev.gitlive.firebase.firestore.externals.Expression as JsExpression
import dev.gitlive.firebase.firestore.externals.Firestore as JsFirestore
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

internal actual fun executePipeline(pipeline: Pipeline, options: Pipeline.ExecuteOptions?): Task<Pipeline.Snapshot> = task(
    start = {
        val js = pipeline.toJs()
        if (options == null || options.options.isEmpty()) execute(js) else execute(json("pipeline" to js, "rawOptions" to json(*options.options.toRawOptions())))
    },
    result = { snapshot -> Pipeline.Snapshot(snapshot.executionTime.toCompat() as Timestamp, snapshot.results.toList().map { PipelineResult(it) }) },
)

internal const val SUBCOLLECTION_PIPELINE = "This pipeline was created without a database (e.g., as a subcollection pipeline) and cannot be executed directly. It can only be used as part of another pipeline."

/** `firestore.pipeline()`, which the pipelines entry point adds to the SDK's Firestore. */
private fun jsPipelineSource(firestore: JsFirestore): JsPipelineSource = js("firestore.pipeline()")

@Suppress("UNCHECKED_CAST")
private fun List<Expression>.toJsArray(): JsArray<JsExpression> = jsArrayOf(map { it.toJs() }) as JsArray<JsExpression>

internal fun Expression.toJs(): JsExpression = when (this) {
    is Field -> jsField(fieldPath.toJsValue())
    is Constant -> jsConstant(value.toJsValue())
    is BooleanConstant -> jsConstant(value.toJsValue())
    is BooleanWrapper -> expr.toJs().asBoolean()
    is Variable -> jsVariable(name)
    is PipelineExpression -> throw IllegalArgumentException("A pipeline is used in an expression through toArrayExpression() or toScalarExpression()")
    is BooleanFunctionExpression -> JsFunctionExpression(name, params.toJsArray()).asBoolean()
    is FunctionExpression -> pipelineValue() ?: JsFunctionExpression(name, params.toJsArray())
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

private fun AggregateFunction.toJs(): JsAggregateFunction = JsAggregateFunction(name, params.toJsArray())

private fun Ordering.toJs(): JsOrdering = JsOrdering(expr.toJs(), dir.wire, null)

/** A selection as the SDK's `Selectable`: a field as is, anything else aliased. */
private fun Map.Entry<String, Expression>.toJsSelectable(): JsAny = (value as? Field)?.takeIf { it.fieldPath.canonicalPath() == key }?.toJs() ?: JsAliasedExpression(value.toJs(), key, null)

private fun Selectable.toJsSelectable(): JsAny = if (this is Field) toJs() else JsAliasedExpression(selectableExpr.toJs(), selectableAlias, null)

/** A stage option as the backend takes it: a field as the SDK's field, a nested map as an object, anything else as is. */
private fun Any?.toJsOption(): Any? = when (this) {
    is Field -> toJs()
    is Expression -> toJs()
    is Map<*, *> -> json(*entries.map { (key, value) -> key.toString() to value.toJsOption() }.toTypedArray())
    is List<*> -> map { it.toJsOption() }
    else -> this
}

/** The options as the SDK's `rawOptions`, which it sends to the backend as they are, keyed as on the wire. */
private fun OptionValues.toRawOptions(): Array<Pair<String, Any?>> = entries.map { (key, value) -> key to value.toJsOption() }.toTypedArray()

/** The stage's own arguments plus its options as the SDK's `rawOptions`. */
private fun Stage<*>.stageOptions(vararg arguments: Pair<String, Any?>): JsAny = if (options.isEmpty()) json(*arguments) else json(*arguments, "rawOptions" to json(*options.toRawOptions()))

internal fun Pipeline.toJs(): JsPipeline {
    var pipeline: JsPipeline? = null
    for (stage in stages) pipeline = stage.toJs(pipeline, this)
    return pipeline ?: throw IllegalStateException("A pipeline needs a source stage")
}

private fun Stage<*>.toJs(pipeline: JsPipeline?, owner: Pipeline): JsPipeline {
    fun source(): JsPipelineSource = jsPipelineSource((owner.firestore ?: throw IllegalStateException(SUBCOLLECTION_PIPELINE)).js)
    fun current(): JsPipeline = pipeline ?: throw IllegalStateException("A pipeline needs a source stage before a $name stage")
    return when (this) {
        is CollectionSource -> source().collection(stageOptions("collection" to path))
        is CollectionGroupSource -> source().collectionGroup(stageOptions("collectionId" to collectionId))
        is SubcollectionSource -> if (options.isEmpty()) jsSubcollection(path) else throw UnsupportedOperationException("The JS SDK takes no options on a subcollection source")
        is DatabaseSource -> source().database(stageOptions())
        is DocumentsSource -> source().documents(stageOptions("docs" to documents.map { it.js }))
        is QuerySource -> source().createFrom(query.js)
        is WhereStage -> current().where(stageOptions("condition" to condition.toJs()))
        is LimitStage -> current().limit(stageOptions("limit" to limit))
        is OffsetStage -> current().offset(stageOptions("offset" to offset))
        is SelectStage -> current().select(stageOptions("selections" to selections.map { it.toJsSelectable() }))
        is AddFieldsStage -> current().addFields(stageOptions("fields" to fields.map { it.toJsSelectable() }))
        is RemoveFieldsStage -> current().removeFields(stageOptions("fields" to fields.map { it.toJs() }))
        is DefineStage -> current().define(stageOptions("variables" to variables.map { (alias, expr) -> JsAliasedExpression(expr.toJs(), alias, null) }))
        is DistinctStage -> current().distinct(stageOptions("groups" to groups.map { it.toJsSelectable() }))
        is SortStage -> current().sort(stageOptions("orderings" to orders.map { it.toJs() }))
        is ReplaceWithStage -> current().replaceWith(stageOptions("map" to expr.toJs()))
        is UnionStage -> current().union(stageOptions("other" to other.toJs()))
        is AggregateStage -> current().aggregate(
            stageOptions(
                "accumulators" to accumulators.map { (alias, function) -> JsAliasedAggregate(function.toJs(), alias, null) },
                "groups" to groups.map { it.toJsSelectable() },
            ),
        )
        is FindNearestStage -> current().findNearest(
            json(
                "field" to vectorField.toJs(),
                "vectorValue" to ((vectorValue as? Constant)?.value?.toJsValue() ?: vectorValue.toJs()),
                "distanceMeasure" to distanceMeasure.wire,
                *(options["limit"]?.let { arrayOf("limit" to it) } ?: emptyArray()),
                *((options["distance_field"] as? Field)?.let { arrayOf("distanceField" to it.fieldPath.canonicalPath()) } ?: emptyArray()),
                *((options - "limit" - "distance_field").takeIf { it.isNotEmpty() }?.let { arrayOf("rawOptions" to json(*it.toRawOptions())) } ?: emptyArray()),
            ),
        )
        is SampleStage -> current().sample(stageOptions((if (mode == SampleStage.Mode.DOCUMENTS) "documents" else "percentage") to count))
        is UnnestStage -> current().unnest(
            json(
                "selectable" to arrayWithAlias.toJsSelectable(),
                *((options["index_field"] as? Field)?.let { arrayOf("indexField" to it.fieldPath.canonicalPath()) } ?: emptyArray()),
                *((options - "index_field").takeIf { it.isNotEmpty() }?.let { arrayOf("rawOptions" to json(*it.toRawOptions())) } ?: emptyArray()),
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
                    sort.takeIf { it.isNotEmpty() }?.let { orderings -> "sort" to orderings.map { it.toJs() } },
                    addFields.takeIf { it.isNotEmpty() }?.let { fields -> "addFields" to fields.map { it.toJsSelectable() } },
                ).toTypedArray(),
            ),
        )
        is RawStage -> {
            val params = jsArrayOf(arguments.map { toExprOrConstant(it).toJs() })
            if (options.isEmpty()) current().rawStage(name, params) else current().rawStage(name, params, json(*options.toRawOptions()))
        }
        else -> throw IllegalArgumentException("Unsupported stage: $name")
    }
}
