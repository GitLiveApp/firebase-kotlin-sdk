/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/**
 * @property segments The field names of the path.
 * @property isDocumentId Whether this is the sentinel path of the document id.
 */
public actual class FieldPath internal constructor(internal val segments: List<String>, internal val isDocumentId: Boolean = false) {
    /** The path `.`-separated, as the Android SDK's canonical string. */
    internal val canonicalString: String get() = if (isDocumentId) DOCUMENT_ID else segments.joinToString(".")

    override fun equals(other: Any?): Boolean = other is FieldPath && other.isDocumentId == isDocumentId && other.segments == segments

    override fun hashCode(): Int = 31 * segments.hashCode() + isDocumentId.hashCode()

    override fun toString(): String = "FieldPath{$canonicalString}"

    public actual companion object {
        private const val DOCUMENT_ID = "__name__"

        public actual fun of(vararg fieldNames: String): FieldPath {
            require(fieldNames.isNotEmpty()) { "Invalid field path. Provided path must not be empty." }
            fieldNames.forEachIndexed { index, name -> require(name.isNotEmpty()) { "Invalid field name at argument ${index + 1}. Field names must not be null or empty." } }
            return FieldPath(fieldNames.asList())
        }

        public actual fun documentId(): FieldPath = FieldPath(listOf(DOCUMENT_ID), isDocumentId = true)

        public actual fun fromDotSeparatedPath(path: String): FieldPath {
            require(path.isNotEmpty()) { "Invalid field path (). Paths must not be empty, begin with '.', end with '.', or contain '..'" }
            return if (path == DOCUMENT_ID) documentId() else FieldPath(path.split('.'))
        }
    }
}

/** A sentinel value; [operation] and [arguments] are converted to the platform SDK's field value when written. */
public actual abstract class FieldValue internal constructor(internal val operation: Operation, internal val arguments: List<Any?>) {
    internal enum class Operation { SERVER_TIMESTAMP, DELETE, ARRAY_UNION, ARRAY_REMOVE, INCREMENT }

    override fun equals(other: Any?): Boolean = other is FieldValue && other.operation == operation && other.arguments == arguments

    override fun hashCode(): Int = 31 * operation.hashCode() + arguments.hashCode()

    override fun toString(): String = "FieldValue{$operation${if (arguments.isEmpty()) "" else " $arguments"}}"

    private class Impl(operation: Operation, arguments: List<Any?>) : FieldValue(operation, arguments)

    public actual companion object {
        public actual fun serverTimestamp(): FieldValue = Impl(Operation.SERVER_TIMESTAMP, emptyList())
        public actual fun delete(): FieldValue = Impl(Operation.DELETE, emptyList())
        public actual fun arrayUnion(vararg elements: Any): FieldValue = Impl(Operation.ARRAY_UNION, elements.asList())
        public actual fun arrayRemove(vararg elements: Any): FieldValue = Impl(Operation.ARRAY_REMOVE, elements.asList())
        public actual fun increment(l: Long): FieldValue = Impl(Operation.INCREMENT, listOf(l))
        public actual fun increment(d: Double): FieldValue = Impl(Operation.INCREMENT, listOf(d))
    }
}

/**
 * @property merge Whether every field is merged.
 * @property mergeFields The fields merged, or null when [merge] applies or the document is replaced.
 */
public actual class SetOptions private constructor(internal val merge: Boolean, internal val mergeFields: List<FieldPath>?) {
    override fun equals(other: Any?): Boolean = other is SetOptions && other.merge == merge && other.mergeFields == mergeFields

    override fun hashCode(): Int = 31 * merge.hashCode() + mergeFields.hashCode()

    override fun toString(): String = "SetOptions{merge=$merge, mergeFields=$mergeFields}"

    public actual companion object {
        public actual fun merge(): SetOptions = SetOptions(true, null)
        public actual fun mergeFields(vararg fields: String): SetOptions = mergeFields(fields.asList())
        public actual fun mergeFields(fields: List<String>): SetOptions = SetOptions(false, fields.map { FieldPath.fromDotSeparatedPath(it) })
        public actual fun mergeFieldPaths(fields: List<FieldPath>): SetOptions = SetOptions(false, fields.toList())
    }
}

/** @property aggregatedField The aggregated field, or null for a count. */
public actual abstract class AggregateField internal constructor(public actual val operator: String, internal val aggregatedField: FieldPath?) {
    public actual val fieldPath: String get() = aggregatedField?.canonicalString.orEmpty()
    public actual val alias: String get() = if (aggregatedField == null) operator else "${operator}_$fieldPath"

    override fun equals(other: Any?): Boolean = other is AggregateField && other.operator == operator && other.aggregatedField == aggregatedField

    override fun hashCode(): Int = 31 * operator.hashCode() + aggregatedField.hashCode()

    override fun toString(): String = "AggregateField{$alias}"

    public actual class CountAggregateField internal constructor() : AggregateField("count", null)
    public actual class SumAggregateField internal constructor(field: FieldPath) : AggregateField("sum", field)
    public actual class AverageAggregateField internal constructor(field: FieldPath) : AggregateField("average", field)

    public actual companion object {
        public actual fun count(): CountAggregateField = CountAggregateField()
        public actual fun sum(field: String): SumAggregateField = SumAggregateField(FieldPath.fromDotSeparatedPath(field))
        public actual fun sum(fieldPath: FieldPath): SumAggregateField = SumAggregateField(fieldPath)
        public actual fun average(field: String): AverageAggregateField = AverageAggregateField(FieldPath.fromDotSeparatedPath(field))
        public actual fun average(fieldPath: FieldPath): AverageAggregateField = AverageAggregateField(fieldPath)
    }
}

/** A condition tree; [node] is null for the empty filter of the public constructor, which matches everything. */
public actual class Filter internal constructor(internal val node: Node?) {
    public actual constructor() : this(null)

    internal sealed class Node {
        data class Field(val fieldPath: FieldPath, val operator: Operator, val value: Any?) : Node()
        data class Composite(val operator: Operator, val filters: List<Node>) : Node()
    }

    internal enum class Operator(val jsOperator: String) {
        EQUAL("=="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        ARRAY_CONTAINS("array-contains"),
        ARRAY_CONTAINS_ANY("array-contains-any"),
        IN("in"),
        NOT_IN("not-in"),
        AND("and"),
        OR("or"),
    }

    override fun equals(other: Any?): Boolean = other is Filter && other.node == node

    override fun hashCode(): Int = node.hashCode()

    override fun toString(): String = "Filter{$node}"

    public actual companion object {
        private fun field(field: String, operator: Operator, value: Any?) = Filter(Node.Field(FieldPath.fromDotSeparatedPath(field), operator, value))
        private fun field(fieldPath: FieldPath, operator: Operator, value: Any?) = Filter(Node.Field(fieldPath, operator, value))
        private fun composite(operator: Operator, filters: Array<out Filter>) = Filter(Node.Composite(operator, filters.mapNotNull { it.node }))

        public actual fun equalTo(field: String, value: Any?): Filter = field(field, Operator.EQUAL, value)
        public actual fun equalTo(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.EQUAL, value)
        public actual fun notEqualTo(field: String, value: Any?): Filter = field(field, Operator.NOT_EQUAL, value)
        public actual fun notEqualTo(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.NOT_EQUAL, value)
        public actual fun greaterThan(field: String, value: Any?): Filter = field(field, Operator.GREATER_THAN, value)
        public actual fun greaterThan(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.GREATER_THAN, value)
        public actual fun greaterThanOrEqualTo(field: String, value: Any?): Filter = field(field, Operator.GREATER_THAN_OR_EQUAL, value)
        public actual fun greaterThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.GREATER_THAN_OR_EQUAL, value)
        public actual fun lessThan(field: String, value: Any?): Filter = field(field, Operator.LESS_THAN, value)
        public actual fun lessThan(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.LESS_THAN, value)
        public actual fun lessThanOrEqualTo(field: String, value: Any?): Filter = field(field, Operator.LESS_THAN_OR_EQUAL, value)
        public actual fun lessThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.LESS_THAN_OR_EQUAL, value)
        public actual fun arrayContains(field: String, value: Any?): Filter = field(field, Operator.ARRAY_CONTAINS, value)
        public actual fun arrayContains(fieldPath: FieldPath, value: Any?): Filter = field(fieldPath, Operator.ARRAY_CONTAINS, value)
        public actual fun arrayContainsAny(field: String, values: List<Any>): Filter = field(field, Operator.ARRAY_CONTAINS_ANY, values)
        public actual fun arrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter = field(fieldPath, Operator.ARRAY_CONTAINS_ANY, values)
        public actual fun inArray(field: String, values: List<Any>): Filter = field(field, Operator.IN, values)
        public actual fun inArray(fieldPath: FieldPath, values: List<Any>): Filter = field(fieldPath, Operator.IN, values)
        public actual fun notInArray(field: String, values: List<Any>): Filter = field(field, Operator.NOT_IN, values)
        public actual fun notInArray(fieldPath: FieldPath, values: List<Any>): Filter = field(fieldPath, Operator.NOT_IN, values)
        public actual fun and(vararg filters: Filter): Filter = composite(Operator.AND, filters)
        public actual fun or(vararg filters: Filter): Filter = composite(Operator.OR, filters)
    }
}
