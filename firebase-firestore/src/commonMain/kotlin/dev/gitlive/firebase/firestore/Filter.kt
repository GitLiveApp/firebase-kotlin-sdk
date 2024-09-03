package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firestore.internal.safeValue

public sealed interface WhereConstraint {

    public sealed interface ForNullableObject : WhereConstraint {
        public val value: Any?
        public val safeValue: Any? get() = value?.safeValue
    }

    public sealed interface ForObject : WhereConstraint {
        public val value: Any
        public val safeValue: Any get() = value.safeValue
    }
    public sealed interface ForArray : WhereConstraint {
        public val values: List<Any>
        public val safeValues: List<Any> get() = values.map { it.safeValue }
    }

    public data class EqualTo internal constructor(override val value: Any?) : ForNullableObject
    public data class NotEqualTo internal constructor(override val value: Any?) : ForNullableObject
    public data class LessThan internal constructor(override val value: Any) : ForObject
    public data class GreaterThan internal constructor(override val value: Any) : ForObject
    public data class LessThanOrEqualTo internal constructor(override val value: Any) : ForObject
    public data class GreaterThanOrEqualTo internal constructor(override val value: Any) : ForObject
    public data class ArrayContains internal constructor(override val value: Any) : ForObject
    public data class ArrayContainsAny internal constructor(override val values: List<Any>) : ForArray
    public data class InArray internal constructor(override val values: List<Any>) : ForArray
    public data class NotInArray internal constructor(override val values: List<Any>) : ForArray
}

public sealed class Filter {
    public data class And internal constructor(val filters: List<Filter>) : Filter()
    public data class Or internal constructor(val filters: List<Filter>) : Filter()
    public sealed class WithConstraint : Filter() {
        public abstract val constraint: WhereConstraint
    }

    public data class Field internal constructor(val field: String, override val constraint: WhereConstraint) : WithConstraint()
    public data class Path internal constructor(val path: FieldPath, override val constraint: WhereConstraint) : WithConstraint()
}

public class FilterBuilder internal constructor() {

    public infix fun String.equalTo(value: Any?): Filter.WithConstraint = Filter.Field(this, WhereConstraint.EqualTo(value))

    public infix fun FieldPath.equalTo(value: Any?): Filter.WithConstraint = Filter.Path(this, WhereConstraint.EqualTo(value))

    public infix fun String.notEqualTo(value: Any?): Filter.WithConstraint = Filter.Field(this, WhereConstraint.NotEqualTo(value))

    public infix fun FieldPath.notEqualTo(value: Any?): Filter.WithConstraint = Filter.Path(this, WhereConstraint.NotEqualTo(value))

    public infix fun String.lessThan(value: Any): Filter.WithConstraint = Filter.Field(this, WhereConstraint.LessThan(value))

    public infix fun FieldPath.lessThan(value: Any): Filter.WithConstraint = Filter.Path(this, WhereConstraint.LessThan(value))

    public infix fun String.greaterThan(value: Any): Filter.WithConstraint = Filter.Field(this, WhereConstraint.GreaterThan(value))

    public infix fun FieldPath.greaterThan(value: Any): Filter.WithConstraint = Filter.Path(this, WhereConstraint.GreaterThan(value))

    public infix fun String.lessThanOrEqualTo(value: Any): Filter.WithConstraint = Filter.Field(this, WhereConstraint.LessThanOrEqualTo(value))

    public infix fun FieldPath.lessThanOrEqualTo(value: Any): Filter.WithConstraint = Filter.Path(this, WhereConstraint.LessThanOrEqualTo(value))

    public infix fun String.greaterThanOrEqualTo(value: Any): Filter.WithConstraint = Filter.Field(this, WhereConstraint.GreaterThanOrEqualTo(value))

    public infix fun FieldPath.greaterThanOrEqualTo(value: Any): Filter.WithConstraint = Filter.Path(this, WhereConstraint.GreaterThanOrEqualTo(value))

    public infix fun String.contains(value: Any): Filter.WithConstraint = Filter.Field(this, WhereConstraint.ArrayContains(value))

    public infix fun FieldPath.contains(value: Any): Filter.WithConstraint = Filter.Path(this, WhereConstraint.ArrayContains(value))

    public infix fun String.containsAny(values: List<Any>): Filter.WithConstraint = Filter.Field(this, WhereConstraint.ArrayContainsAny(values))

    public infix fun FieldPath.containsAny(values: List<Any>): Filter.WithConstraint = Filter.Path(this, WhereConstraint.ArrayContainsAny(values))

    public infix fun String.inArray(values: List<Any>): Filter.WithConstraint = Filter.Field(this, WhereConstraint.InArray(values))

    public infix fun FieldPath.inArray(values: List<Any>): Filter.WithConstraint = Filter.Path(this, WhereConstraint.InArray(values))

    public infix fun String.notInArray(values: List<Any>): Filter.WithConstraint = Filter.Field(this, WhereConstraint.NotInArray(values))

    public infix fun FieldPath.notInArray(values: List<Any>): Filter.WithConstraint = Filter.Path(this, WhereConstraint.NotInArray(values))

    public infix fun Filter.and(right: Filter): Filter.And {
        val leftList = when (this) {
            is Filter.And -> filters
            else -> listOf(this)
        }
        val rightList = when (right) {
            is Filter.And -> right.filters
            else -> listOf(right)
        }
        return Filter.And(leftList + rightList)
    }

    public infix fun Filter.or(right: Filter): Filter.Or {
        val leftList = when (this) {
            is Filter.Or -> filters
            else -> listOf(this)
        }
        val rightList = when (right) {
            is Filter.Or -> right.filters
            else -> listOf(right)
        }
        return Filter.Or(leftList + rightList)
    }

    public fun all(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left and right }
    public fun any(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left or right }

    private fun Collection<Filter>.combine(over: (Filter, Filter) -> Filter): Filter? = fold<Filter, Filter?>(null) { acc, filter ->
        acc?.let { over(acc, filter) } ?: filter
    }
}
