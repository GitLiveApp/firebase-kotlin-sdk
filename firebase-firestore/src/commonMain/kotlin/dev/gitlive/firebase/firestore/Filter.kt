package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firestore.internal.safeValue

sealed interface WhereConstraint {

    sealed interface ForNullableObject : WhereConstraint {
        val value: Any?
        val safeValue get() = value?.safeValue
    }

    sealed interface ForObject : WhereConstraint {
        val value: Any
        val safeValue get() = value.safeValue
    }
    sealed interface ForArray : WhereConstraint {
        val values: List<Any>
        val safeValues get() = values.map { it.safeValue }
    }

    data class EqualTo internal constructor(override val value: Any?) : ForNullableObject
    data class NotEqualTo internal constructor(override val value: Any?) : ForNullableObject
    data class LessThan internal constructor(override val value: Any) : ForObject
    data class GreaterThan internal constructor(override val value: Any) : ForObject
    data class LessThanOrEqualTo internal constructor(override val value: Any) : ForObject
    data class GreaterThanOrEqualTo internal constructor(override val value: Any) : ForObject
    data class ArrayContains internal constructor(override val value: Any) : ForObject
    data class ArrayContainsAny internal constructor(override val values: List<Any>) : ForArray
    data class InArray internal constructor(override val values: List<Any>) : ForArray
    data class NotInArray internal constructor(override val values: List<Any>) : ForArray
}

sealed class Filter {
    data class And internal constructor(val filters: List<Filter>) : Filter()
    data class Or internal constructor(val filters: List<Filter>) : Filter()
    sealed class WithConstraint : Filter() {
        abstract val constraint: WhereConstraint
    }

    data class Field internal constructor(val field: String, override val constraint: WhereConstraint) : WithConstraint()
    data class Path internal constructor(val path: FieldPath, override val constraint: WhereConstraint) : WithConstraint()
}

class FilterBuilder internal constructor() {

    infix fun String.equalTo(value: Any?): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.EqualTo(value))
    }

    infix fun FieldPath.equalTo(value: Any?): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.EqualTo(value))
    }

    infix fun String.notEqualTo(value: Any?): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.NotEqualTo(value))
    }

    infix fun FieldPath.notEqualTo(value: Any?): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.NotEqualTo(value))
    }

    infix fun String.lessThan(value: Any): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.LessThan(value))
    }

    infix fun FieldPath.lessThan(value: Any): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.LessThan(value))
    }

    infix fun String.greaterThan(value: Any): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.GreaterThan(value))
    }

    infix fun FieldPath.greaterThan(value: Any): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.GreaterThan(value))
    }

    infix fun String.lessThanOrEqualTo(value: Any): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.LessThanOrEqualTo(value))
    }

    infix fun FieldPath.lessThanOrEqualTo(value: Any): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.LessThanOrEqualTo(value))
    }

    infix fun String.greaterThanOrEqualTo(value: Any): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.GreaterThanOrEqualTo(value))
    }

    infix fun FieldPath.greaterThanOrEqualTo(value: Any): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.GreaterThanOrEqualTo(value))
    }

    infix fun String.contains(value: Any): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.ArrayContains(value))
    }

    infix fun FieldPath.contains(value: Any): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.ArrayContains(value))
    }

    infix fun String.containsAny(values: List<Any>): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.ArrayContainsAny(values))
    }

    infix fun FieldPath.containsAny(values: List<Any>): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.ArrayContainsAny(values))
    }

    infix fun String.inArray(values: List<Any>): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.InArray(values))
    }

    infix fun FieldPath.inArray(values: List<Any>): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.InArray(values))
    }

    infix fun String.notInArray(values: List<Any>): Filter.WithConstraint {
        return Filter.Field(this, WhereConstraint.NotInArray(values))
    }

    infix fun FieldPath.notInArray(values: List<Any>): Filter.WithConstraint {
        return Filter.Path(this, WhereConstraint.NotInArray(values))
    }

    infix fun Filter.and(right: Filter): Filter.And {
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

    infix fun Filter.or(right: Filter): Filter.Or {
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

    fun all(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left and right }
    fun any(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left or right }

    private fun Collection<Filter>.combine(over: (Filter, Filter) -> Filter): Filter? = fold<Filter, Filter?>(null) { acc, filter ->
        acc?.let { over(acc, filter) } ?: filter
    }
}
