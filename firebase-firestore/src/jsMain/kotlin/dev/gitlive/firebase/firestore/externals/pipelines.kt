@file:JsModule("firebase/firestore/pipelines")
@file:JsNonModule

package dev.gitlive.firebase.firestore.externals

import kotlin.js.Json
import kotlin.js.Promise

/*
 * The pipeline entry point of the Firebase JS SDK, which also adds `pipeline()` to the Firestore instances (see
 * JsFirestoreWithPipelines). Each stage is declared with its options-object overload, which carries the stage's own
 * arguments and the backend `rawOptions`.
 */

public open external class Expression

public external class Field : Expression

public external class FunctionExpression(name: String, params: Array<Expression>) : Expression {
    public fun asBoolean(): BooleanExpression
}

public external class BooleanExpression : Expression

public external class AggregateFunction(name: String, params: Array<Expression>)

public external class AliasedAggregate(aggregate: AggregateFunction, alias: String, methodName: String?)

public external class AliasedExpression(expr: Expression, alias: String, methodName: String?) : Expression

public external class Ordering(expr: Expression, direction: String, methodName: String?)

public external fun field(name: String): Field

public external fun field(path: FieldPath): Field

public external fun constant(value: Any?): Expression

public external fun variable(name: String): Expression

public external fun subcollection(path: String): Pipeline

public external fun execute(pipeline: Pipeline): Promise<PipelineSnapshot>

public external fun execute(options: Json): Promise<PipelineSnapshot>

public external class Pipeline {
    public fun addFields(options: Json): Pipeline
    public fun removeFields(options: Json): Pipeline
    public fun define(options: Json): Pipeline
    public fun select(options: Json): Pipeline
    public fun where(options: Json): Pipeline
    public fun offset(options: Json): Pipeline
    public fun limit(options: Json): Pipeline
    public fun distinct(options: Json): Pipeline
    public fun aggregate(options: Json): Pipeline
    public fun findNearest(options: Json): Pipeline
    public fun search(options: Json): Pipeline
    public fun sort(options: Json): Pipeline
    public fun replaceWith(options: Json): Pipeline
    public fun sample(options: Json): Pipeline
    public fun union(options: Json): Pipeline
    public fun unnest(options: Json): Pipeline
    public fun rawStage(name: String, params: Array<Any?>, options: Json = definedExternally): Pipeline
    public fun toArrayExpression(): Expression
    public fun toScalarExpression(): Expression
}

public external class PipelineSource {
    public fun collection(options: Json): Pipeline
    public fun collectionGroup(options: Json): Pipeline
    public fun database(options: Json): Pipeline
    public fun documents(options: Json): Pipeline
    public fun createFrom(query: Query): Pipeline
}

public external class PipelineSnapshot {
    public val results: Array<PipelineResult>
    public val executionTime: Timestamp
}

public external class PipelineResult {
    public val ref: DocumentReference?
    public val id: String?
    public val createTime: Timestamp?
    public val updateTime: Timestamp?
    public fun data(): Any?
    public fun get(field: Any): Any?
}
