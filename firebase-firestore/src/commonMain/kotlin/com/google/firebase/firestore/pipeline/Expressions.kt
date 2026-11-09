/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore.pipeline

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import dev.gitlive.firebase.firestore.INTERNAL_CONSTRUCTOR
import dev.gitlive.firebase.firestore.internal.pipeline.canonicalPath
import kotlin.jvm.JvmStatic

/*
 * The expression tree of the pipeline API. The Android SDK's Expression builders are mirrored by the generated
 * Expression.kt; the nodes they build are a function of other expressions (named as on the wire), a field, a constant or
 * a variable (the latter in dev.gitlive.firebase.firestore.internal.pipeline, as the SDK has no public class for them).
 * On Apple and JS the tree is converted to the platform SDK's expressions when the pipeline runs; on Android and the JVM
 * these classes are header stubs and the SDK's own classes are used.
 *
 * The constructors that the SDK's classes do not have are internal and error-deprecated, so that the header stub
 * verification (which skips deprecated members) does not look for them on the real classes.
 */

/** A function applied to [params], named as the backend knows it. */
public open class FunctionExpression
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val name: String,
    internal val params: List<Expression>,
) : Expression()

/** An expression that can be given an alias in `select`, `addFields` and similar stages. */
public abstract class Selectable public constructor() : Expression() {
    /** The name the expression's value is selected as. */
    internal abstract val selectableAlias: String

    /** The expression selected. */
    internal abstract val selectableExpr: Expression
}

/** A document field. */
public class Field
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val fieldPath: FieldPath,
) : Selectable() {
    override val selectableAlias: String get() = fieldPath.canonicalPath()
    override val selectableExpr: Expression get() = this

    /** The distance in meters between this field's geo point and [location]. */
    public fun geoDistance(location: GeoPoint): Expression = Expression.geoDistance(this, location)

    override fun equals(other: Any?): Boolean = other is Field && other.fieldPath == fieldPath

    override fun hashCode(): Int = fieldPath.hashCode()

    override fun toString(): String = "Field(${fieldPath.canonicalPath()})"

    public companion object {
        @Suppress("DEPRECATION_ERROR")
        internal fun of(name: String): Field = Field(if (name == "__name__") FieldPath.documentId() else FieldPath.fromDotSeparatedPath(name))

        @Suppress("DEPRECATION_ERROR")
        internal fun of(path: FieldPath): Field = Field(path)
    }
}

/** An [Expression] selected under an alias. */
public class AliasedExpression public constructor(internal val alias: String, internal val expr: Expression) : Selectable() {
    override val selectableAlias: String get() = alias
    override val selectableExpr: Expression get() = expr
}

/** An [AggregateFunction] whose result is named in an `aggregate` stage. */
public class AliasedAggregate public constructor(internal val alias: String, internal val aggregate: AggregateFunction)

/** An ordering of a `sort` stage: [expr] in [dir]. */
public class Ordering public constructor(public val expr: Expression, public val dir: Direction) {
    public enum class Direction(internal val wire: String) {
        ASCENDING("ascending"),
        DESCENDING("descending"),
    }

    public companion object {
        @JvmStatic
        public fun ascending(expr: Expression): Ordering = Ordering(expr, Direction.ASCENDING)

        @JvmStatic
        public fun ascending(fieldName: String): Ordering = Ordering(Field.of(fieldName), Direction.ASCENDING)

        @JvmStatic
        public fun descending(expr: Expression): Ordering = Ordering(expr, Direction.DESCENDING)

        @JvmStatic
        public fun descending(fieldName: String): Ordering = Ordering(Field.of(fieldName), Direction.DESCENDING)
    }
}
