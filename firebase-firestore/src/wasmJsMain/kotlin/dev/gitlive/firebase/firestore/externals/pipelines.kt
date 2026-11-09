@file:JsModule("firebase/firestore/pipelines")

package dev.gitlive.firebase.firestore.externals

import kotlin.js.Promise

/*
 * The pipeline entry point of the Firebase JS SDK, which also adds `pipeline()` to the Firestore instances. Each stage is
 * declared with its options-object overload, which carries the stage's own arguments and the backend `rawOptions`.
 */

public open external class Expression : JsAny {
    public fun asBoolean(): BooleanExpression
}

public external class Field : Expression

public external class FunctionExpression(name: String, params: JsArray<Expression>) : Expression

public external class BooleanExpression : Expression

public external class AggregateFunction(name: String, params: JsArray<Expression>) : JsAny

public external class AliasedAggregate(aggregate: AggregateFunction, alias: String, methodName: String?) : JsAny

public external class AliasedExpression(expr: Expression, alias: String, methodName: String?) : Expression

public external class Ordering(expr: Expression, direction: String, methodName: String?) : JsAny

public external fun field(name: String): Field

public external fun field(path: FieldPath): Field

public external fun constant(value: JsAny?): Expression

public external fun variable(name: String): Expression

public external fun subcollection(path: String): Pipeline

public external fun execute(pipeline: Pipeline): Promise<PipelineSnapshot>

public external fun execute(options: JsAny): Promise<PipelineSnapshot>

public external class Pipeline : JsAny {
    public fun addFields(options: JsAny): Pipeline
    public fun removeFields(options: JsAny): Pipeline
    public fun define(options: JsAny): Pipeline
    public fun select(options: JsAny): Pipeline
    public fun where(options: JsAny): Pipeline
    public fun offset(options: JsAny): Pipeline
    public fun limit(options: JsAny): Pipeline
    public fun distinct(options: JsAny): Pipeline
    public fun aggregate(options: JsAny): Pipeline
    public fun findNearest(options: JsAny): Pipeline
    public fun search(options: JsAny): Pipeline
    public fun sort(options: JsAny): Pipeline
    public fun replaceWith(options: JsAny): Pipeline
    public fun sample(options: JsAny): Pipeline
    public fun union(options: JsAny): Pipeline
    public fun unnest(options: JsAny): Pipeline
    public fun rawStage(name: String, params: JsArray<JsAny?>, options: JsAny = definedExternally): Pipeline
    public fun toArrayExpression(): Expression
    public fun toScalarExpression(): Expression
}

public external class PipelineSource : JsAny {
    public fun collection(options: JsAny): Pipeline
    public fun collectionGroup(options: JsAny): Pipeline
    public fun database(options: JsAny): Pipeline
    public fun documents(options: JsAny): Pipeline
    public fun createFrom(query: Query): Pipeline
}

public external class PipelineSnapshot : JsAny {
    public val results: JsArray<PipelineResult>
    public val executionTime: Timestamp
}

public external class PipelineResult : JsAny {
    public val ref: DocumentReference?
    public val id: String?
    public val createTime: Timestamp?
    public val updateTime: Timestamp?
    public fun data(): JsAny?
    public fun get(field: JsAny): JsAny?
}
