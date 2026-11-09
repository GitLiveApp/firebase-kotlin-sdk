/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal.pipeline

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Pipeline
import com.google.firebase.firestore.PipelineResult
import com.google.firebase.firestore.VectorValue
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
import com.google.firebase.firestore.toIos as toIosValue
import swiftPMImport.dev.gitlive.firebase.firestore.FIRFirestore
import swiftPMImport.dev.gitlive.firebase.firestore.FIRVectorValue
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRAddFieldsStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRAggregateFunctionBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRAggregateStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRCollectionGroupSourceStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRCollectionSourceStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRConstantBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRDatabaseSourceStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRDefineStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRDistinctStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRDocumentsSourceStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRExprBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRFieldBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRFindNearestStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRFunctionExprBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRLimitStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIROffsetStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIROrderingBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRPipelineBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRPipelineExprBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRPipelineResultBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRRawStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRRemoveFieldsStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRReplaceWithStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRSampleStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRSearchStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRSelectStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRSortStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRSubcollectionSourceStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRUnionStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRUnnestStageBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRVariableBridge
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRWhereStageBridge
import platform.Foundation.NSNull
import platform.Foundation.NSNumber
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithLongLong

/*
 * The pipeline API over the Firebase iOS SDK's Objective-C bridge (FIRPipelineBridge.h): the `__FIR*Bridge` classes
 * the SDK's Swift pipeline API is built on. The Kotlin expression tree and stages are converted to bridge objects when
 * the pipeline runs; stage options the bridge takes no parameter for are rejected rather than dropped.
 */

internal actual fun executePipeline(pipeline: Pipeline, options: Pipeline.ExecuteOptions?): Task<Pipeline.Snapshot> {
    val firestore = pipeline.firestore ?: throw IllegalStateException(SUBCOLLECTION_PIPELINE)
    if (!options?.options.isNullOrEmpty()) throw UnsupportedOperationException("Execute options are not supported by the Firebase iOS SDK's pipeline bridge")
    val bridge = __FIRPipelineBridge(stages = pipeline.stages.flatMap { it.toIos(firestore) }, db = firestore.ios)
    return task { completion ->
        bridge.executeWithCompletion { snapshot, error ->
            completion(snapshot?.let { Pipeline.Snapshot(it.execution_time.toCompat() as Timestamp, it.results.map { result -> PipelineResult(result as __FIRPipelineResultBridge) }) }, error)
        }
    }
}

internal const val SUBCOLLECTION_PIPELINE = "This pipeline was created without a database (e.g., as a subcollection pipeline) and cannot be executed directly. It can only be used as part of another pipeline."

internal fun Expression.toIos(): __FIRExprBridge = when (this) {
    is Field -> __FIRFieldBridge(path = fieldPath.toIosValue())
    is Constant -> __FIRConstantBridge(input = value.toIosValue() ?: NSNull.`null`())
    is BooleanConstant -> __FIRConstantBridge(input = value)
    is BooleanWrapper -> expr.toIos()
    is Variable -> __FIRVariableBridge(name = name)
    is PipelineExpression -> __FIRPipelineExprBridge(stages = pipeline.stagesToIos())
    is BooleanFunctionExpression -> __FIRFunctionExprBridge(name = name, Args = params.map { it.toIos() }, Options = null)
    is FunctionExpression -> __FIRFunctionExprBridge(name = name, Args = params.map { it.toIos() }, Options = null)
    is Selectable -> selectableExpr.toIos()
    else -> throw IllegalArgumentException("Unsupported expression: $this")
}

/** The stages as bridges; a subcollection pipeline has no database of its own and takes the [enclosing] pipeline's where a stage needs one. */
private fun Pipeline.stagesToIos(enclosing: FirebaseFirestore? = null): List<__FIRStageBridge> {
    val database = firestore ?: enclosing
    return stages.flatMap { it.toIos(database) }
}

private fun AggregateFunction.toIos(): __FIRAggregateFunctionBridge = __FIRAggregateFunctionBridge(name = name, Args = params.map { it.toIos() })

private fun Ordering.toIos(): __FIROrderingBridge = __FIROrderingBridge(expr = expr.toIos(), Direction = dir.wire)

private fun Map<String, Expression>.toBridges(): Map<Any?, *> = mapValues { it.value.toIos() }

/** A stage option as a bridge expression: a field as a field, anything else as a constant. */
private fun Any?.toIosOption(): __FIRExprBridge = when (this) {
    is Field -> toIos()
    is Expression -> toIos()
    is Map<*, *> -> __FIRFunctionExprBridge(name = "map", Args = flatMap { (key, value) -> listOf(__FIRConstantBridge(input = key.toString()), value.toIosOption()) }, Options = null)
    else -> __FIRConstantBridge(input = toIosValue() ?: NSNull.`null`())
}

private fun OptionValues.toBridgeOptions(): Map<Any?, *> = mapValues { it.value.toIosOption() }

/** The bridge's stage classes take no options; a stage carrying some cannot be sent faithfully. */
private fun Stage<*>.rejectOptions(vararg handled: String) {
    val rest = options.keys - handled.toSet()
    if (rest.isNotEmpty()) throw UnsupportedOperationException("The Firebase iOS SDK's pipeline bridge does not take the options $rest on a $name stage")
}

/** The `force_index` hint of a collection source, the one hint the bridge takes; any other hint is rejected rather than dropped. */
@Suppress("UNCHECKED_CAST")
private fun Stage<*>.forceIndex(): String? {
    val hints = options["hints"] as? Map<String, Any?> ?: return null
    val rest = hints.keys - "force_index"
    if (rest.isNotEmpty()) throw UnsupportedOperationException("The Firebase iOS SDK's pipeline bridge does not take the hints $rest on a $name stage")
    return hints["force_index"] as? String
}

/** The stage as bridges; [firestore] is null in a subcollection pipeline, which only the stages that read the database reject. */
private fun Stage<*>.toIos(firestore: FirebaseFirestore?): List<__FIRStageBridge> {
    fun database(): FirebaseFirestore = firestore ?: throw IllegalStateException(SUBCOLLECTION_PIPELINE)
    return when (this) {
        is CollectionSource -> listOf(__FIRCollectionSourceStageBridge(ref = database().ios.collectionWithPath(path), firestore = database().ios, forceIndex = forceIndex()).also { rejectOptions("hints") })
        is CollectionGroupSource -> listOf(__FIRCollectionGroupSourceStageBridge(collectionId = collectionId, forceIndex = forceIndex()).also { rejectOptions("hints") })
        is SubcollectionSource -> listOf(__FIRSubcollectionSourceStageBridge(path = path).also { rejectOptions() })
        is DatabaseSource -> listOf(__FIRDatabaseSourceStageBridge().also { rejectOptions() })
        is DocumentsSource -> listOf(__FIRDocumentsSourceStageBridge(documents = documents.map { it.ios }, firestore = database().ios).also { rejectOptions() })
        is QuerySource -> __FIRPipelineBridge.createStageBridgesFromQuery(query.ios).map { it as __FIRStageBridge }.also { rejectOptions() }
        is WhereStage -> listOf(__FIRWhereStageBridge(expr = condition.toIos()).also { rejectOptions() })
        is LimitStage -> listOf(__FIRLimitStageBridge(limit = limit.toLong()).also { rejectOptions() })
        is OffsetStage -> listOf(__FIROffsetStageBridge(offset = offset.toLong()).also { rejectOptions() })
        is SelectStage -> listOf(__FIRSelectStageBridge(selections = selections.toBridges()).also { rejectOptions() })
        is AddFieldsStage -> listOf(__FIRAddFieldsStageBridge(fields = fields.toBridges()).also { rejectOptions() })
        is RemoveFieldsStage -> listOf(__FIRRemoveFieldsStageBridge(fields = fields.map { it.fieldPath.canonicalPath() }).also { rejectOptions() })
        is DefineStage -> listOf(__FIRDefineStageBridge(variables = variables.toBridges()).also { rejectOptions() })
        is DistinctStage -> listOf(__FIRDistinctStageBridge(groups = groups.toBridges()).also { rejectOptions() })
        is SortStage -> listOf(__FIRSortStageBridge(orderings = orders.map { it.toIos() }).also { rejectOptions() })
        is ReplaceWithStage -> listOf(__FIRReplaceWithStageBridge(expr = expr.toIos()).also { rejectOptions() })
        is UnionStage -> listOf(__FIRUnionStageBridge(other = __FIRPipelineBridge(stages = other.stagesToIos(firestore), db = database().ios)).also { rejectOptions() })
        is AggregateStage -> listOf(__FIRAggregateStageBridge(accumulators = accumulators.mapValues { it.value.toIos() }, groups = groups.toBridges()).also { rejectOptions() })
        is FindNearestStage -> listOf(
            __FIRFindNearestStageBridge(
                field = vectorField.toIos() as __FIRFieldBridge,
                vectorValue = ((vectorValue as? Constant)?.value as? VectorValue ?: throw UnsupportedOperationException("The Firebase iOS SDK's pipeline bridge takes the vector of a findNearest stage as a value, not an expression")).toIosValue() as FIRVectorValue,
                distanceMeasure = distanceMeasure.wire,
                limit = (options["limit"] as? Long)?.let { NSNumber.numberWithLongLong(it) },
                distanceField = (options["distance_field"] as? Field)?.toIos(),
            ).also { rejectOptions("limit", "distance_field") },
        )
        is SampleStage -> listOf((if (mode == SampleStage.Mode.DOCUMENTS) __FIRSampleStageBridge(count = count.toLong()) else __FIRSampleStageBridge(percentage = count.toDouble())).also { rejectOptions() })
        is UnnestStage -> listOf(
            __FIRUnnestStageBridge(
                field = arrayWithAlias.selectableExpr.toIos(),
                alias = __FIRFieldBridge(name = arrayWithAlias.selectableAlias),
                indexField = (options["index_field"] as? Field)?.toIos(),
            ).also { rejectOptions("index_field") },
        )
        is SearchStage -> listOf(
            __FIRSearchStageBridge(
                options = buildMap<String, Any?> {
                    query?.let { put("query", it) } ?: rquery?.let { put("query", it) }
                    limit?.let { put("limit", it) }
                    offset?.let { put("offset", it) }
                    retrievalDepth?.let { put("retrieval_depth", it) }
                    languageCode?.let { put("language_code", it) }
                }.toBridgeOptions(),
                addFields = addFields.toBridges(),
                select = emptyMap<Any?, Any?>(),
                sort = sort.map { it.toIos() },
            ).also { rejectOptions() },
        )
        is RawStage -> listOf(__FIRRawStageBridge(name = name, params = arguments.map { toExprOrConstant(it).toIos() }, options = options.toBridgeOptions()))
        else -> throw IllegalArgumentException("Unsupported stage: $name")
    }
}

@Suppress("unused")
private val keepNumberImports = NSNumber.numberWithDouble(0.0)
