/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal.pipeline

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Pipeline
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.VectorValue
import com.google.firebase.firestore.pipeline.AggregateFunction
import com.google.firebase.firestore.pipeline.BooleanExpression
import com.google.firebase.firestore.pipeline.Expression
import com.google.firebase.firestore.pipeline.Field
import com.google.firebase.firestore.pipeline.FunctionExpression
import com.google.firebase.firestore.pipeline.Ordering
import com.google.firebase.firestore.pipeline.Selectable
import com.google.firebase.firestore.pipeline.Stage

/*
 * The internals of the com.google.firebase.firestore.pipeline layer: the expression nodes the SDK's classes do not
 * expose, the stages its Pipeline functions add, and the helpers the generated Expression builders call. They live
 * outside the com.google packages because those are header stubs on Android and the JVM (compiled, verified against the
 * SDK's classes and stripped), and nothing here has an SDK counterpart.
 */

/** The options of a stage or of an options object, keyed as on the wire; a value is a String, Boolean, Long, Double, [Field], List of Strings or nested map. */
internal typealias OptionValues = Map<String, Any?>

// Expression nodes

/** A [FunctionExpression] that evaluates to a boolean. */
internal class BooleanFunctionExpression(internal val name: String, internal val params: List<Expression>) : BooleanExpression()

/** A literal value: a String, Number, Boolean, [Timestamp], [GeoPoint], [Blob], [DocumentReference], [VectorValue] or null. */
internal class Constant(internal val value: Any?) : Expression()

/** A literal boolean, usable where a condition is. */
internal class BooleanConstant(internal val value: Boolean) : BooleanExpression()

/** A [Field] or [Constant] used as a condition, as `asBoolean()` wraps it (the SDK's BooleanField and BooleanConstant). */
internal class BooleanWrapper(internal val expr: Expression) : BooleanExpression()

/** A reference to a variable defined by an earlier `define` stage or an array transform. */
internal class Variable(internal val name: String) : Expression()

/** A [Pipeline] used as a value in an enclosing pipeline's expression (see `Pipeline.toArrayExpression`). */
internal class PipelineExpression(internal val pipeline: Pipeline) : Expression()

/** The expression for [value]: an [Expression] as is, a List as an `array`, a Map as a `map`, a [Pipeline] as a sub-pipeline and any other value as a [Constant]. */
internal fun toExprOrConstant(value: Any?): Expression = when (value) {
    null -> Constant(null)
    is Expression -> value
    is Pipeline -> PipelineExpression(value)
    is String, is Number, is Boolean, is Timestamp, is GeoPoint, is Blob, is DocumentReference, is VectorValue -> Constant(value)
    is ByteArray -> Constant(Blob.fromBytes(value))
    is DoubleArray -> Constant(VectorValue.of(value))
    is List<*> -> Expression.array(value)
    is Map<*, *> -> {
        require(value.keys.all { it is String }) { "Maps with non-string keys are not supported" }
        @Suppress("UNCHECKED_CAST")
        Expression.map(value as Map<String, *>)
    }
    else -> throw IllegalArgumentException("Unsupported value type: ${value::class.simpleName}")
}

@Suppress("DEPRECATION_ERROR")
internal fun function(name: String, vararg args: Any?): Expression = FunctionExpression(name, args.map(::toExprOrConstant))

internal fun booleanFunction(name: String, vararg args: Any?): BooleanExpression = BooleanFunctionExpression(name, args.map(::toExprOrConstant))

@Suppress("DEPRECATION_ERROR")
internal fun aggregate(name: String, vararg args: Any?): AggregateFunction = AggregateFunction(name, args.map(::toExprOrConstant))

internal fun field(name: String): Field = Field.of(name)

/** The selections of a `select`, `distinct` or `addFields` stage: a [Selectable] or a field name, by alias. */
internal fun selections(first: Any, additional: Array<out Any>): Map<String, Expression> = (listOf(first) + additional).associate { selection ->
    when (selection) {
        is Selectable -> selection.selectableAlias to selection.selectableExpr
        is String -> selection to Field.of(selection)
        else -> throw IllegalArgumentException("A selection must be a Selectable or a field name, not ${selection::class.simpleName}")
    }
}

// The stages the Pipeline functions add, which the SDK has no public class for

@Suppress("DEPRECATION_ERROR")
internal class DatabaseSource(options: OptionValues) : Stage<DatabaseSource>("database", options) {
    override fun self(options: OptionValues): DatabaseSource = DatabaseSource(options)
}

@Suppress("DEPRECATION_ERROR")
internal class DocumentsSource(internal val documents: List<DocumentReference>, options: OptionValues) : Stage<DocumentsSource>("documents", options) {
    override fun self(options: OptionValues): DocumentsSource = DocumentsSource(documents, options)
}

/** The stages equivalent to a [Query], which the platform SDK derives. */
@Suppress("DEPRECATION_ERROR")
internal class QuerySource(internal val query: Query, options: OptionValues) : Stage<QuerySource>("query", options) {
    override fun self(options: OptionValues): QuerySource = QuerySource(query, options)
}

@Suppress("DEPRECATION_ERROR")
internal class WhereStage(internal val condition: BooleanExpression, options: OptionValues) : Stage<WhereStage>("where", options) {
    override fun self(options: OptionValues): WhereStage = WhereStage(condition, options)
}

@Suppress("DEPRECATION_ERROR")
internal class LimitStage(internal val limit: Int, options: OptionValues) : Stage<LimitStage>("limit", options) {
    override fun self(options: OptionValues): LimitStage = LimitStage(limit, options)
}

@Suppress("DEPRECATION_ERROR")
internal class OffsetStage(internal val offset: Int, options: OptionValues) : Stage<OffsetStage>("offset", options) {
    override fun self(options: OptionValues): OffsetStage = OffsetStage(offset, options)
}

@Suppress("DEPRECATION_ERROR")
internal class SelectStage(internal val selections: Map<String, Expression>, options: OptionValues) : Stage<SelectStage>("select", options) {
    override fun self(options: OptionValues): SelectStage = SelectStage(selections, options)
}

@Suppress("DEPRECATION_ERROR")
internal class AddFieldsStage(internal val fields: Map<String, Expression>, options: OptionValues) : Stage<AddFieldsStage>("add_fields", options) {
    override fun self(options: OptionValues): AddFieldsStage = AddFieldsStage(fields, options)
}

@Suppress("DEPRECATION_ERROR")
internal class RemoveFieldsStage(internal val fields: List<Field>, options: OptionValues) : Stage<RemoveFieldsStage>("remove_fields", options) {
    override fun self(options: OptionValues): RemoveFieldsStage = RemoveFieldsStage(fields, options)
}

@Suppress("DEPRECATION_ERROR")
internal class DefineStage(internal val variables: Map<String, Expression>, options: OptionValues) : Stage<DefineStage>("define", options) {
    override fun self(options: OptionValues): DefineStage = DefineStage(variables, options)
}

@Suppress("DEPRECATION_ERROR")
internal class DistinctStage(internal val groups: Map<String, Expression>, options: OptionValues) : Stage<DistinctStage>("distinct", options) {
    override fun self(options: OptionValues): DistinctStage = DistinctStage(groups, options)
}

@Suppress("DEPRECATION_ERROR")
internal class SortStage(internal val orders: List<Ordering>, options: OptionValues) : Stage<SortStage>("sort", options) {
    override fun self(options: OptionValues): SortStage = SortStage(orders, options)
}

@Suppress("DEPRECATION_ERROR")
internal class ReplaceWithStage(internal val expr: Expression, options: OptionValues) : Stage<ReplaceWithStage>("replace_with", options) {
    override fun self(options: OptionValues): ReplaceWithStage = ReplaceWithStage(expr, options)
}

@Suppress("DEPRECATION_ERROR")
internal class UnionStage(internal val other: Pipeline, options: OptionValues) : Stage<UnionStage>("union", options) {
    override fun self(options: OptionValues): UnionStage = UnionStage(other, options)
}

// The platform parts

/** Runs [pipeline] on the platform SDK with [options]. */
internal expect fun executePipeline(pipeline: Pipeline, options: Pipeline.ExecuteOptions?): Task<Pipeline.Snapshot>

/** The path `.`-separated, as the Android SDK's canonical string; the pipeline API names a field by it. */
internal expect fun FieldPath.canonicalPath(): String
