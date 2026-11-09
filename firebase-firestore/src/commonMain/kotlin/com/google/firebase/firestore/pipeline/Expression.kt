/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

// Generated from the Firebase Android SDK's api.txt and classes (see the PR description); the function names on the wire
// and the way each argument is wrapped (a leading String is a field name, other values are constants) follow the SDK.

package com.google.firebase.firestore.pipeline

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.VectorValue
import dev.gitlive.firebase.firestore.CONSTANT_OF_DATE_ANDROID_ONLY
import dev.gitlive.firebase.firestore.INTERNAL_CONSTRUCTOR
import dev.gitlive.firebase.firestore.internal.pipeline.BooleanConstant
import dev.gitlive.firebase.firestore.internal.pipeline.BooleanFunctionExpression
import dev.gitlive.firebase.firestore.internal.pipeline.BooleanWrapper
import dev.gitlive.firebase.firestore.internal.pipeline.Constant
import dev.gitlive.firebase.firestore.internal.pipeline.Variable
import dev.gitlive.firebase.firestore.internal.pipeline.aggregate
import dev.gitlive.firebase.firestore.internal.pipeline.booleanFunction
import dev.gitlive.firebase.firestore.internal.pipeline.field
import dev.gitlive.firebase.firestore.internal.pipeline.function
import java.util.Date
import kotlin.jvm.JvmStatic

/**
 * An expression of the Firestore pipeline API: a field, a constant or a function of other expressions, built here and
 * sent to the backend as part of a stage. Mirrors the Firebase Android SDK's `Expression`, whose static functions are
 * the companion's.
 */
public abstract class Expression {
    public fun abs(): Expression = Companion.abs(this)

    public fun add(second: Expression): Expression = Companion.add(this, second)

    public fun add(second: Number): Expression = Companion.add(this, second)

    public fun alias(alias: String): AliasedExpression = AliasedExpression(alias, this)

    public fun arrayAgg(): AggregateFunction = AggregateFunction.arrayAgg(this)

    public fun arrayAggDistinct(): AggregateFunction = AggregateFunction.arrayAggDistinct(this)

    public fun arrayConcat(secondArray: Expression, vararg otherArrays: Any): Expression = Companion.arrayConcat(this, secondArray, *otherArrays)

    public fun arrayConcat(secondArray: Any, vararg otherArrays: Any): Expression = Companion.arrayConcat(this, secondArray, *otherArrays)

    public fun arrayContains(element: Expression): BooleanExpression = Companion.arrayContains(this, element)

    public fun arrayContains(element: Any): BooleanExpression = Companion.arrayContains(this, element)

    public fun arrayContainsAll(arrayExpression: Expression): BooleanExpression = Companion.arrayContainsAll(this, arrayExpression)

    public fun arrayContainsAll(values: List<*>): BooleanExpression = Companion.arrayContainsAll(this, values)

    public fun arrayContainsAny(arrayExpression: Expression): BooleanExpression = Companion.arrayContainsAny(this, arrayExpression)

    public fun arrayContainsAny(values: List<*>): BooleanExpression = Companion.arrayContainsAny(this, values)

    public fun arrayFilter(alias: String, filter: BooleanExpression): Expression = Companion.arrayFilter(this, alias, filter)

    public fun arrayFirst(): Expression = Companion.arrayFirst(this)

    public fun arrayFirstN(n: Expression): Expression = Companion.arrayFirstN(this, n)

    public fun arrayFirstN(n: Int): Expression = Companion.arrayFirstN(this, n)

    public fun arrayGet(offset: Expression): Expression = Companion.arrayGet(this, offset)

    public fun arrayGet(offset: Int): Expression = Companion.arrayGet(this, offset)

    public fun arrayIndexOf(value: Any?): Expression = Companion.arrayIndexOf(this, value)

    public fun arrayIndexOfAll(value: Any?): Expression = Companion.arrayIndexOfAll(this, value)

    public fun arrayLast(): Expression = Companion.arrayLast(this)

    public fun arrayLastIndexOf(value: Any?): Expression = Companion.arrayLastIndexOf(this, value)

    public fun arrayLastN(n: Expression): Expression = Companion.arrayLastN(this, n)

    public fun arrayLastN(n: Int): Expression = Companion.arrayLastN(this, n)

    public fun arrayLength(): Expression = Companion.arrayLength(this)

    public fun arrayMaximum(): Expression = Companion.arrayMaximum(this)

    public fun arrayMaximumN(n: Expression): Expression = Companion.arrayMaximumN(this, n)

    public fun arrayMaximumN(n: Int): Expression = Companion.arrayMaximumN(this, n)

    public fun arrayMinimum(): Expression = Companion.arrayMinimum(this)

    public fun arrayMinimumN(n: Expression): Expression = Companion.arrayMinimumN(this, n)

    public fun arrayMinimumN(n: Int): Expression = Companion.arrayMinimumN(this, n)

    public fun arrayReverse(): Expression = Companion.arrayReverse(this)

    public fun arraySlice(offset: Expression, length: Expression): Expression = Companion.arraySlice(this, offset, length)

    public fun arraySlice(offset: Int, length: Int): Expression = Companion.arraySlice(this, offset, length)

    public fun arraySliceToEnd(offset: Expression): Expression = Companion.arraySliceToEnd(this, offset)

    public fun arraySliceToEnd(offset: Int): Expression = Companion.arraySliceToEnd(this, offset)

    public fun arraySum(): Expression = Companion.arraySum(this)

    public fun arrayTransform(elementAlias: String, transform: Expression): Expression = Companion.arrayTransform(this, elementAlias, transform)

    public fun arrayTransformWithIndex(elementAlias: String, indexAlias: String, transform: Expression): Expression = Companion.arrayTransformWithIndex(this, elementAlias, indexAlias, transform)

    public fun asBoolean(): BooleanExpression = when (this) {
        is BooleanExpression -> this
        is Field, is Constant -> BooleanWrapper(this)
        else -> (this as FunctionExpression).let { BooleanFunctionExpression(it.name, it.params) }
    }

    public fun ascending(): Ordering = Ordering.ascending(this)

    public fun average(): AggregateFunction = AggregateFunction.average(this)

    public fun bitAnd(bitsOther: ByteArray): Expression = Companion.bitAnd(this, bitsOther)

    public fun bitAnd(bitsOther: Expression): Expression = Companion.bitAnd(this, bitsOther)

    public fun bitLeftShift(numberExpr: Expression): Expression = Companion.bitLeftShift(this, numberExpr)

    public fun bitLeftShift(number: Int): Expression = Companion.bitLeftShift(this, number)

    public fun bitNot(): Expression = Companion.bitNot(this)

    public fun bitOr(bitsOther: ByteArray): Expression = Companion.bitOr(this, bitsOther)

    public fun bitOr(bitsOther: Expression): Expression = Companion.bitOr(this, bitsOther)

    public fun bitRightShift(numberExpr: Expression): Expression = Companion.bitRightShift(this, numberExpr)

    public fun bitRightShift(number: Int): Expression = Companion.bitRightShift(this, number)

    public fun bitXor(bitsOther: ByteArray): Expression = Companion.bitXor(this, bitsOther)

    public fun bitXor(bitsOther: Expression): Expression = Companion.bitXor(this, bitsOther)

    public fun byteLength(): Expression = Companion.byteLength(this)

    public fun ceil(): Expression = Companion.ceil(this)

    public fun charLength(): Expression = Companion.charLength(this)

    public fun coalesce(replacement: Any, vararg others: Any): Expression = Companion.coalesce(this, replacement, *others)

    public fun collectionId(): Expression = Companion.collectionId(this)

    public fun concat(second: Expression, vararg others: Any): Expression = Companion.concat(this, second, *others)

    public fun concat(second: Any, vararg others: Any): Expression = Companion.concat(this, second, *others)

    public fun cosineDistance(vector: Expression): Expression = Companion.cosineDistance(this, vector)

    public fun cosineDistance(vector: VectorValue): Expression = Companion.cosineDistance(this, vector)

    public fun cosineDistance(vector: DoubleArray): Expression = Companion.cosineDistance(this, vector)

    public fun count(): AggregateFunction = AggregateFunction.count(this)

    public fun countDistinct(): AggregateFunction = AggregateFunction.countDistinct(this)

    public fun descending(): Ordering = Ordering.descending(this)

    public fun divide(divisor: Expression): Expression = Companion.divide(this, divisor)

    public fun divide(divisor: Number): Expression = Companion.divide(this, divisor)

    public fun documentId(): Expression = Companion.documentId(this)

    public fun dotProduct(vector: Expression): Expression = Companion.dotProduct(this, vector)

    public fun dotProduct(vector: VectorValue): Expression = Companion.dotProduct(this, vector)

    public fun dotProduct(vector: DoubleArray): Expression = Companion.dotProduct(this, vector)

    public fun endsWith(suffix: Expression): BooleanExpression = Companion.endsWith(this, suffix)

    public fun endsWith(suffix: String): BooleanExpression = Companion.endsWith(this, suffix)

    public fun equal(other: Expression): BooleanExpression = Companion.equal(this, other)

    public fun equal(value: Any): BooleanExpression = Companion.equal(this, value)

    public fun equalAny(arrayExpression: Expression): BooleanExpression = Companion.equalAny(this, arrayExpression)

    public fun equalAny(values: List<*>): BooleanExpression = Companion.equalAny(this, values)

    public fun euclideanDistance(vector: Expression): Expression = Companion.euclideanDistance(this, vector)

    public fun euclideanDistance(vector: VectorValue): Expression = Companion.euclideanDistance(this, vector)

    public fun euclideanDistance(vector: DoubleArray): Expression = Companion.euclideanDistance(this, vector)

    public fun exists(): BooleanExpression = Companion.exists(this)

    public fun exp(): Expression = Companion.exp(this)

    public fun first(): AggregateFunction = AggregateFunction.first(this)

    public fun floor(): Expression = Companion.floor(this)

    public fun getField(keyExpression: Expression): Expression = Companion.getField(this, keyExpression)

    public fun getField(key: String): Expression = Companion.getField(this, key)

    public fun greaterThan(other: Expression): BooleanExpression = Companion.greaterThan(this, other)

    public fun greaterThan(value: Any): BooleanExpression = Companion.greaterThan(this, value)

    public fun greaterThanOrEqual(other: Expression): BooleanExpression = Companion.greaterThanOrEqual(this, other)

    public fun greaterThanOrEqual(value: Any): BooleanExpression = Companion.greaterThanOrEqual(this, value)

    public fun ifAbsent(elseExpr: Expression): Expression = Companion.ifAbsent(this, elseExpr)

    public fun ifAbsent(elseValue: Any): Expression = Companion.ifAbsent(this, elseValue)

    public fun ifError(catchExpr: Expression): Expression = Companion.ifError(this, catchExpr)

    public fun ifError(catchValue: Any): Expression = Companion.ifError(this, catchValue)

    public fun ifNull(elseExpression: Expression): Expression = Companion.ifNull(this, elseExpression)

    public fun ifNull(elseValue: Any): Expression = Companion.ifNull(this, elseValue)

    public fun isAbsent(): BooleanExpression = Companion.isAbsent(this)

    public fun isError(): BooleanExpression = Companion.isError(this)

    public fun isType(type: String): BooleanExpression = Companion.isType(this, type)

    public fun join(delimiterExpression: Expression): Expression = Companion.join(this, delimiterExpression)

    public fun join(delimiter: String): Expression = Companion.join(this, delimiter)

    public fun last(): AggregateFunction = AggregateFunction.last(this)

    public fun length(): Expression = Companion.length(this)

    public fun lessThan(other: Expression): BooleanExpression = Companion.lessThan(this, other)

    public fun lessThan(value: Any): BooleanExpression = Companion.lessThan(this, value)

    public fun lessThanOrEqual(other: Expression): BooleanExpression = Companion.lessThanOrEqual(this, other)

    public fun lessThanOrEqual(value: Any): BooleanExpression = Companion.lessThanOrEqual(this, value)

    public fun like(pattern: Expression): BooleanExpression = Companion.like(this, pattern)

    public fun like(pattern: String): BooleanExpression = Companion.like(this, pattern)

    public fun ln(): Expression = Companion.ln(this)

    public fun log10(): Expression = Companion.log10(this)

    public fun logicalMaximum(vararg others: Expression): Expression = Companion.logicalMaximum(this, *others)

    public fun logicalMaximum(vararg others: Any): Expression = Companion.logicalMaximum(this, *others)

    public fun logicalMinimum(vararg others: Expression): Expression = Companion.logicalMinimum(this, *others)

    public fun logicalMinimum(vararg others: Any): Expression = Companion.logicalMinimum(this, *others)

    public fun ltrim(): Expression = Companion.ltrim(this)

    public fun ltrimValue(valuesToTrim: Expression): Expression = Companion.ltrimValue(this, valuesToTrim)

    public fun ltrimValue(valuesToTrim: String): Expression = Companion.ltrimValue(this, valuesToTrim)

    public fun mapEntries(): Expression = Companion.mapEntries(this)

    public fun mapGet(keyExpression: Expression): Expression = Companion.mapGet(this, keyExpression)

    public fun mapGet(key: String): Expression = Companion.mapGet(this, key)

    public fun mapKeys(): Expression = Companion.mapKeys(this)

    public fun mapMerge(mapExpr: Expression, vararg otherMaps: Expression): Expression = Companion.mapMerge(this, mapExpr, *otherMaps)

    public fun mapRemove(keyExpression: Expression): Expression = Companion.mapRemove(this, keyExpression)

    public fun mapRemove(key: String): Expression = Companion.mapRemove(this, key)

    public fun mapSet(key: Expression, value: Expression, vararg moreKeyValues: Expression): Expression = Companion.mapSet(this, key, value, *moreKeyValues)

    public fun mapSet(key: String, value: Any?, vararg moreKeyValues: Any): Expression = Companion.mapSet(this, key, value, *moreKeyValues)

    public fun mapValues(): Expression = Companion.mapValues(this)

    public fun maximum(): AggregateFunction = AggregateFunction.maximum(this)

    public fun minimum(): AggregateFunction = AggregateFunction.minimum(this)

    public fun mod(divisor: Expression): Expression = Companion.mod(this, divisor)

    public fun mod(divisor: Number): Expression = Companion.mod(this, divisor)

    public fun multiply(second: Expression): Expression = Companion.multiply(this, second)

    public fun multiply(second: Number): Expression = Companion.multiply(this, second)

    public fun notEqual(other: Expression): BooleanExpression = Companion.notEqual(this, other)

    public fun notEqual(value: Any): BooleanExpression = Companion.notEqual(this, value)

    public fun notEqualAny(arrayExpression: Expression): BooleanExpression = Companion.notEqualAny(this, arrayExpression)

    public fun notEqualAny(values: List<*>): BooleanExpression = Companion.notEqualAny(this, values)

    public fun parent(): Expression = Companion.parent(this)

    public fun pow(exponent: Expression): Expression = Companion.pow(this, exponent)

    public fun pow(exponent: Number): Expression = Companion.pow(this, exponent)

    public fun regexContains(pattern: Expression): BooleanExpression = Companion.regexContains(this, pattern)

    public fun regexContains(pattern: String): BooleanExpression = Companion.regexContains(this, pattern)

    public fun regexMatch(pattern: Expression): BooleanExpression = Companion.regexMatch(this, pattern)

    public fun regexMatch(pattern: String): BooleanExpression = Companion.regexMatch(this, pattern)

    public fun reverse(): Expression = Companion.reverse(this)

    public fun round(): Expression = Companion.round(this)

    public fun roundToPrecision(decimalPlace: Expression): Expression = Companion.roundToPrecision(this, decimalPlace)

    public fun roundToPrecision(decimalPlace: Int): Expression = Companion.roundToPrecision(this, decimalPlace)

    public fun rtrim(): Expression = Companion.rtrim(this)

    public fun rtrimValue(valuesToTrim: Expression): Expression = Companion.rtrimValue(this, valuesToTrim)

    public fun rtrimValue(valuesToTrim: String): Expression = Companion.rtrimValue(this, valuesToTrim)

    public fun split(delimiter: Blob): Expression = Companion.split(this, delimiter)

    public fun split(delimiter: Expression): Expression = Companion.split(this, delimiter)

    public fun split(delimiter: String): Expression = Companion.split(this, delimiter)

    public fun sqrt(): Expression = Companion.sqrt(this)

    public fun startsWith(prefix: Expression): BooleanExpression = Companion.startsWith(this, prefix)

    public fun startsWith(prefix: String): BooleanExpression = Companion.startsWith(this, prefix)

    public fun stringConcat(vararg stringExpressions: Expression): Expression = Companion.stringConcat(this, *stringExpressions)

    public fun stringConcat(vararg strings: Any): Expression = Companion.stringConcat(this, *strings)

    public fun stringConcat(vararg strings: String): Expression = Companion.stringConcat(this, *strings)

    public fun stringContains(substring: Expression): BooleanExpression = Companion.stringContains(this, substring)

    public fun stringContains(substring: String): BooleanExpression = Companion.stringContains(this, substring)

    public fun stringIndexOf(substring: Expression): Expression = Companion.stringIndexOf(this, substring)

    public fun stringIndexOf(substring: String): Expression = Companion.stringIndexOf(this, substring)

    public fun stringRepeat(count: Expression): Expression = Companion.stringRepeat(this, count)

    public fun stringRepeat(count: Int): Expression = Companion.stringRepeat(this, count)

    public fun stringReplaceAll(oldValue: Expression, newValue: Expression): Expression = Companion.stringReplaceAll(this, oldValue, newValue)

    public fun stringReplaceAll(oldValue: String, newValue: String): Expression = Companion.stringReplaceAll(this, oldValue, newValue)

    public fun stringReplaceOne(oldValue: Expression, newValue: Expression): Expression = Companion.stringReplaceOne(this, oldValue, newValue)

    public fun stringReplaceOne(oldValue: String, newValue: String): Expression = Companion.stringReplaceOne(this, oldValue, newValue)

    public fun stringReverse(): Expression = Companion.stringReverse(this)

    public fun substring(start: Expression, length: Expression): Expression = Companion.substring(this, start, length)

    public fun substring(start: Int, length: Int): Expression = Companion.substring(this, constant(start), constant(length))

    public fun subtract(subtrahend: Expression): Expression = Companion.subtract(this, subtrahend)

    public fun subtract(subtrahend: Number): Expression = Companion.subtract(this, subtrahend)

    public fun sum(): AggregateFunction = AggregateFunction.sum(this)

    public fun timestampAdd(unit: Expression, amount: Expression): Expression = Companion.timestampAdd(this, unit, amount)

    public fun timestampAdd(unit: String, amount: Long): Expression = Companion.timestampAdd(this, unit, amount)

    public fun timestampDiff(start: Expression, unit: Expression): Expression = Companion.timestampDiff(this, start, unit)

    public fun timestampDiff(start: Expression, unit: String): Expression = Companion.timestampDiff(this, start, unit)

    public fun timestampDiff(startFieldName: String, unit: String): Expression = Companion.timestampDiff(this, startFieldName, unit)

    public fun timestampExtract(part: Expression): Expression = Companion.timestampExtract(this, part)

    public fun timestampExtract(part: String): Expression = Companion.timestampExtract(this, part)

    public fun timestampExtractWithTimezone(part: Expression, timezone: Expression): Expression = Companion.timestampExtractWithTimezone(this, part, timezone)

    public fun timestampExtractWithTimezone(part: Expression, timezone: String): Expression = Companion.timestampExtractWithTimezone(this, part, timezone)

    public fun timestampExtractWithTimezone(part: String, timezone: Expression): Expression = Companion.timestampExtractWithTimezone(this, part, timezone)

    public fun timestampExtractWithTimezone(part: String, timezone: String): Expression = Companion.timestampExtractWithTimezone(this, part, timezone)

    public fun timestampSubtract(unit: Expression, amount: Expression): Expression = Companion.timestampSubtract(this, unit, amount)

    public fun timestampSubtract(unit: String, amount: Long): Expression = Companion.timestampSubtract(this, unit, amount)

    public fun timestampToUnixMicros(): Expression = Companion.timestampToUnixMicros(this)

    public fun timestampToUnixMillis(): Expression = Companion.timestampToUnixMillis(this)

    public fun timestampToUnixSeconds(): Expression = Companion.timestampToUnixSeconds(this)

    public fun timestampTruncate(granularity: Expression): Expression = Companion.timestampTruncate(this, granularity)

    public fun timestampTruncate(granularity: String): Expression = Companion.timestampTruncate(this, granularity)

    public fun timestampTruncateWithTimezone(granularity: Expression, timezone: Expression): Expression = Companion.timestampTruncateWithTimezone(this, granularity, timezone)

    public fun timestampTruncateWithTimezone(granularity: Expression, timezone: String): Expression = Companion.timestampTruncateWithTimezone(this, granularity, timezone)

    public fun timestampTruncateWithTimezone(granularity: String, timezone: Expression): Expression = Companion.timestampTruncateWithTimezone(this, granularity, timezone)

    public fun timestampTruncateWithTimezone(granularity: String, timezone: String): Expression = Companion.timestampTruncateWithTimezone(this, granularity, timezone)

    public fun toLower(): Expression = Companion.toLower(this)

    public fun toUpper(): Expression = Companion.toUpper(this)

    public fun trim(): Expression = Companion.trim(this)

    public fun trimValue(valueToTrim: Expression): Expression = Companion.trimValue(this, valueToTrim)

    public fun trimValue(valueToTrim: String): Expression = Companion.trimValue(this, constant(valueToTrim))

    public fun trunc(): Expression = Companion.trunc(this)

    public fun truncToPrecision(decimalPlace: Expression): Expression = Companion.truncToPrecision(this, decimalPlace)

    public fun truncToPrecision(decimalPlace: Int): Expression = Companion.truncToPrecision(this, decimalPlace)

    public fun type(): Expression = Companion.type(this)

    public fun unixMicrosToTimestamp(): Expression = Companion.unixMicrosToTimestamp(this)

    public fun unixMillisToTimestamp(): Expression = Companion.unixMillisToTimestamp(this)

    public fun unixSecondsToTimestamp(): Expression = Companion.unixSecondsToTimestamp(this)

    public fun vectorLength(): Expression = Companion.vectorLength(this)

    public companion object {
        @JvmStatic
        public fun abs(numericExpr: Expression): Expression = function("abs", numericExpr)

        @JvmStatic
        public fun abs(numericField: String): Expression = function("abs", field(numericField))

        @JvmStatic
        public fun add(first: Expression, second: Expression): Expression = function("add", first, second)

        @JvmStatic
        public fun add(first: Expression, second: Number): Expression = function("add", first, second)

        @JvmStatic
        public fun add(numericFieldName: String, second: Expression): Expression = function("add", field(numericFieldName), second)

        @JvmStatic
        public fun add(numericFieldName: String, second: Number): Expression = function("add", field(numericFieldName), second)

        @JvmStatic
        public fun and(condition: BooleanExpression, vararg conditions: BooleanExpression): BooleanExpression = booleanFunction("and", condition, *conditions)

        @JvmStatic
        public fun array(vararg elements: Any?): Expression = function("array", *elements)

        @JvmStatic
        public fun array(elements: List<Any?>): Expression = function("array", *elements.toTypedArray())

        @JvmStatic
        public fun arrayConcat(firstArray: Expression, secondArray: Expression, vararg otherArrays: Any): Expression = function("array_concat", firstArray, secondArray, *otherArrays)

        @JvmStatic
        public fun arrayConcat(firstArray: Expression, secondArray: Any, vararg otherArrays: Any): Expression = function("array_concat", firstArray, secondArray, *otherArrays)

        @JvmStatic
        public fun arrayConcat(firstArrayField: String, secondArray: Expression, vararg otherArrays: Any): Expression = function("array_concat", field(firstArrayField), secondArray, *otherArrays)

        @JvmStatic
        public fun arrayConcat(firstArrayField: String, secondArray: Any, vararg otherArrays: Any): Expression = function("array_concat", field(firstArrayField), secondArray, *otherArrays)

        @JvmStatic
        public fun arrayContains(array: Expression, element: Expression): BooleanExpression = booleanFunction("array_contains", array, element)

        @JvmStatic
        public fun arrayContains(array: Expression, element: Any): BooleanExpression = booleanFunction("array_contains", array, element)

        @JvmStatic
        public fun arrayContains(arrayFieldName: String, element: Expression): BooleanExpression = booleanFunction("array_contains", field(arrayFieldName), element)

        @JvmStatic
        public fun arrayContains(arrayFieldName: String, element: Any): BooleanExpression = booleanFunction("array_contains", field(arrayFieldName), element)

        @JvmStatic
        public fun arrayContainsAll(array: Expression, arrayExpression: Expression): BooleanExpression = booleanFunction("array_contains_all", array, arrayExpression)

        @JvmStatic
        public fun arrayContainsAll(array: Expression, values: List<*>): BooleanExpression = arrayContainsAll(array, array(values))

        @JvmStatic
        public fun arrayContainsAll(arrayFieldName: String, arrayExpression: Expression): BooleanExpression = booleanFunction("array_contains_all", field(arrayFieldName), arrayExpression)

        @JvmStatic
        public fun arrayContainsAll(arrayFieldName: String, values: List<*>): BooleanExpression = booleanFunction("array_contains_all", field(arrayFieldName), values)

        @JvmStatic
        public fun arrayContainsAny(array: Expression, arrayExpression: Expression): BooleanExpression = booleanFunction("array_contains_any", array, arrayExpression)

        @JvmStatic
        public fun arrayContainsAny(array: Expression, values: List<*>): BooleanExpression = booleanFunction("array_contains_any", array, values)

        @JvmStatic
        public fun arrayContainsAny(arrayFieldName: String, arrayExpression: Expression): BooleanExpression = booleanFunction("array_contains_any", field(arrayFieldName), arrayExpression)

        @JvmStatic
        public fun arrayContainsAny(arrayFieldName: String, values: List<*>): BooleanExpression = booleanFunction("array_contains_any", field(arrayFieldName), values)

        @JvmStatic
        public fun arrayFilter(array: Expression, alias: String, filter: BooleanExpression): Expression = function("array_filter", array, alias, filter)

        @JvmStatic
        public fun arrayFilter(arrayFieldName: String, alias: String, filter: BooleanExpression): Expression = function("array_filter", field(arrayFieldName), alias, filter)

        @JvmStatic
        public fun arrayFirst(array: Expression): Expression = function("array_first", array)

        @JvmStatic
        public fun arrayFirst(arrayFieldName: String): Expression = function("array_first", field(arrayFieldName))

        @JvmStatic
        public fun arrayFirstN(array: Expression, n: Expression): Expression = function("array_first_n", array, n)

        @JvmStatic
        public fun arrayFirstN(array: Expression, n: Int): Expression = function("array_first_n", array, n)

        @JvmStatic
        public fun arrayFirstN(arrayFieldName: String, n: Expression): Expression = function("array_first_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayFirstN(arrayFieldName: String, n: Int): Expression = function("array_first_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayGet(array: Expression, offset: Expression): Expression = function("array_get", array, offset)

        @JvmStatic
        public fun arrayGet(array: Expression, offset: Int): Expression = function("array_get", array, offset)

        @JvmStatic
        public fun arrayGet(arrayFieldName: String, offset: Expression): Expression = function("array_get", field(arrayFieldName), offset)

        @JvmStatic
        public fun arrayGet(arrayFieldName: String, offset: Int): Expression = function("array_get", field(arrayFieldName), offset)

        @JvmStatic
        public fun arrayIndexOf(array: Expression, value: Any?): Expression = function("array_index_of", array, value, constant("first"))

        @JvmStatic
        public fun arrayIndexOf(arrayFieldName: String, value: Any?): Expression = function("array_index_of", field(arrayFieldName), value, constant("first"))

        @JvmStatic
        public fun arrayIndexOfAll(array: Expression, value: Any?): Expression = function("array_index_of_all", array, value)

        @JvmStatic
        public fun arrayIndexOfAll(arrayFieldName: String, value: Any?): Expression = function("array_index_of_all", field(arrayFieldName), value)

        @JvmStatic
        public fun arrayLast(array: Expression): Expression = function("array_last", array)

        @JvmStatic
        public fun arrayLast(arrayFieldName: String): Expression = function("array_last", field(arrayFieldName))

        @JvmStatic
        public fun arrayLastIndexOf(array: Expression, value: Any?): Expression = function("array_index_of", array, value, constant("last"))

        @JvmStatic
        public fun arrayLastIndexOf(arrayFieldName: String, value: Any?): Expression = function("array_index_of", field(arrayFieldName), value, constant("last"))

        @JvmStatic
        public fun arrayLastN(array: Expression, n: Expression): Expression = function("array_last_n", array, n)

        @JvmStatic
        public fun arrayLastN(array: Expression, n: Int): Expression = function("array_last_n", array, n)

        @JvmStatic
        public fun arrayLastN(arrayFieldName: String, n: Expression): Expression = function("array_last_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayLastN(arrayFieldName: String, n: Int): Expression = function("array_last_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayLength(array: Expression): Expression = function("array_length", array)

        @JvmStatic
        public fun arrayLength(arrayFieldName: String): Expression = function("array_length", field(arrayFieldName))

        @JvmStatic
        public fun arrayMaximum(array: Expression): Expression = function("maximum", array)

        @JvmStatic
        public fun arrayMaximum(arrayFieldName: String): Expression = function("maximum", field(arrayFieldName))

        @JvmStatic
        public fun arrayMaximumN(array: Expression, n: Expression): Expression = function("maximum_n", array, n)

        @JvmStatic
        public fun arrayMaximumN(array: Expression, n: Int): Expression = function("maximum_n", array, n)

        @JvmStatic
        public fun arrayMaximumN(arrayFieldName: String, n: Expression): Expression = function("maximum_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayMaximumN(arrayFieldName: String, n: Int): Expression = function("maximum_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayMinimum(array: Expression): Expression = function("minimum", array)

        @JvmStatic
        public fun arrayMinimum(arrayFieldName: String): Expression = function("minimum", field(arrayFieldName))

        @JvmStatic
        public fun arrayMinimumN(array: Expression, n: Expression): Expression = function("minimum_n", array, n)

        @JvmStatic
        public fun arrayMinimumN(array: Expression, n: Int): Expression = function("minimum_n", array, n)

        @JvmStatic
        public fun arrayMinimumN(arrayFieldName: String, n: Expression): Expression = function("minimum_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayMinimumN(arrayFieldName: String, n: Int): Expression = function("minimum_n", field(arrayFieldName), n)

        @JvmStatic
        public fun arrayReverse(array: Expression): Expression = function("array_reverse", array)

        @JvmStatic
        public fun arrayReverse(arrayFieldName: String): Expression = function("array_reverse", field(arrayFieldName))

        @JvmStatic
        public fun arraySlice(array: Expression, offset: Expression, length: Expression): Expression = function("array_slice", array, offset, length)

        @JvmStatic
        public fun arraySlice(array: Expression, offset: Int, length: Int): Expression = function("array_slice", array, offset, length)

        @JvmStatic
        public fun arraySlice(arrayFieldName: String, offset: Expression, length: Expression): Expression = function("array_slice", field(arrayFieldName), offset, length)

        @JvmStatic
        public fun arraySlice(arrayFieldName: String, offset: Int, length: Int): Expression = function("array_slice", field(arrayFieldName), offset, length)

        @JvmStatic
        public fun arraySliceToEnd(array: Expression, offset: Expression): Expression = function("array_slice", array, offset)

        @JvmStatic
        public fun arraySliceToEnd(array: Expression, offset: Int): Expression = function("array_slice", array, offset)

        @JvmStatic
        public fun arraySliceToEnd(arrayFieldName: String, offset: Expression): Expression = function("array_slice", field(arrayFieldName), offset)

        @JvmStatic
        public fun arraySliceToEnd(arrayFieldName: String, offset: Int): Expression = function("array_slice", field(arrayFieldName), offset)

        @JvmStatic
        public fun arraySum(array: Expression): Expression = function("sum", array)

        @JvmStatic
        public fun arraySum(arrayFieldName: String): Expression = function("sum", field(arrayFieldName))

        @JvmStatic
        public fun arrayTransform(array: Expression, elementAlias: String, transform: Expression): Expression = function("array_transform", array, elementAlias, transform)

        @JvmStatic
        public fun arrayTransform(arrayFieldName: String, elementAlias: String, transform: Expression): Expression = function("array_transform", field(arrayFieldName), elementAlias, transform)

        @JvmStatic
        public fun arrayTransformWithIndex(array: Expression, elementAlias: String, indexAlias: String, transform: Expression): Expression = function("array_transform", array, elementAlias, indexAlias, transform)

        @JvmStatic
        public fun arrayTransformWithIndex(arrayFieldName: String, elementAlias: String, indexAlias: String, transform: Expression): Expression = function("array_transform", field(arrayFieldName), elementAlias, indexAlias, transform)

        @JvmStatic
        public fun bitAnd(bits: Expression, bitsOther: ByteArray): Expression = function("bit_and", bits, bitsOther)

        @JvmStatic
        public fun bitAnd(bits: Expression, bitsOther: Expression): Expression = function("bit_and", bits, bitsOther)

        @JvmStatic
        public fun bitAnd(bitsFieldName: String, bitsOther: ByteArray): Expression = function("bit_and", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun bitAnd(bitsFieldName: String, bitsOther: Expression): Expression = function("bit_and", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun bitLeftShift(bits: Expression, numberExpr: Expression): Expression = function("bit_left_shift", bits, numberExpr)

        @JvmStatic
        public fun bitLeftShift(bits: Expression, number: Int): Expression = function("bit_left_shift", bits, number)

        @JvmStatic
        public fun bitLeftShift(bitsFieldName: String, numberExpr: Expression): Expression = function("bit_left_shift", field(bitsFieldName), numberExpr)

        @JvmStatic
        public fun bitLeftShift(bitsFieldName: String, number: Int): Expression = function("bit_left_shift", field(bitsFieldName), number)

        @JvmStatic
        public fun bitNot(bits: Expression): Expression = function("bit_not", bits)

        @JvmStatic
        public fun bitNot(bitsFieldName: String): Expression = function("bit_not", field(bitsFieldName))

        @JvmStatic
        public fun bitOr(bits: Expression, bitsOther: ByteArray): Expression = function("bit_or", bits, bitsOther)

        @JvmStatic
        public fun bitOr(bits: Expression, bitsOther: Expression): Expression = function("bit_or", bits, bitsOther)

        @JvmStatic
        public fun bitOr(bitsFieldName: String, bitsOther: ByteArray): Expression = function("bit_or", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun bitOr(bitsFieldName: String, bitsOther: Expression): Expression = function("bit_or", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun bitRightShift(bits: Expression, numberExpr: Expression): Expression = function("bit_right_shift", bits, numberExpr)

        @JvmStatic
        public fun bitRightShift(bits: Expression, number: Int): Expression = function("bit_right_shift", bits, number)

        @JvmStatic
        public fun bitRightShift(bitsFieldName: String, numberExpr: Expression): Expression = function("bit_right_shift", field(bitsFieldName), numberExpr)

        @JvmStatic
        public fun bitRightShift(bitsFieldName: String, number: Int): Expression = function("bit_right_shift", field(bitsFieldName), number)

        @JvmStatic
        public fun bitXor(bits: Expression, bitsOther: ByteArray): Expression = function("bit_xor", bits, bitsOther)

        @JvmStatic
        public fun bitXor(bits: Expression, bitsOther: Expression): Expression = function("bit_xor", bits, bitsOther)

        @JvmStatic
        public fun bitXor(bitsFieldName: String, bitsOther: ByteArray): Expression = function("bit_xor", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun bitXor(bitsFieldName: String, bitsOther: Expression): Expression = function("bit_xor", field(bitsFieldName), bitsOther)

        @JvmStatic
        public fun byteLength(value: Expression): Expression = function("byte_length", value)

        @JvmStatic
        public fun byteLength(fieldName: String): Expression = function("byte_length", field(fieldName))

        @JvmStatic
        public fun ceil(numericExpr: Expression): Expression = function("ceil", numericExpr)

        @JvmStatic
        public fun ceil(numericField: String): Expression = function("ceil", field(numericField))

        @JvmStatic
        public fun charLength(expr: Expression): Expression = function("char_length", expr)

        @JvmStatic
        public fun charLength(fieldName: String): Expression = function("char_length", field(fieldName))

        @JvmStatic
        public fun coalesce(expression: Expression, replacement: Any, vararg others: Any): Expression = function("coalesce", expression, replacement, *others)

        @JvmStatic
        public fun coalesce(fieldName: String, replacement: Any, vararg others: Any): Expression = function("coalesce", field(fieldName), replacement, *others)

        @JvmStatic
        public fun collectionId(path: Expression): Expression = function("collection_id", path)

        @JvmStatic
        public fun collectionId(pathField: String): Expression = collectionId(field(pathField))

        @JvmStatic
        public fun concat(first: Expression, second: Expression, vararg others: Any): Expression = function("concat", first, second, *others)

        @JvmStatic
        public fun concat(first: Expression, second: Any, vararg others: Any): Expression = function("concat", first, second, *others)

        @JvmStatic
        public fun concat(first: String, second: Expression, vararg others: Any): Expression = function("concat", field(first), second, *others)

        @JvmStatic
        public fun concat(first: String, second: Any, vararg others: Any): Expression = function("concat", field(first), second, *others)

        @JvmStatic
        public fun conditional(condition: BooleanExpression, thenExpr: Expression, elseExpr: Expression): Expression = function("conditional", condition, thenExpr, elseExpr)

        @JvmStatic
        public fun conditional(condition: BooleanExpression, thenValue: Any, elseValue: Any): Expression = function("conditional", condition, thenValue, elseValue)

        @JvmStatic
        public fun constant(value: Boolean): BooleanExpression = BooleanConstant(value)

        @JvmStatic
        public fun constant(value: ByteArray): Expression = Constant(Blob.fromBytes(value))

        @JvmStatic
        public fun constant(value: Blob): Expression = Constant(value)

        @JvmStatic
        public fun constant(ref: DocumentReference): Expression = Constant(ref)

        @JvmStatic
        public fun constant(value: GeoPoint): Expression = Constant(value)

        @JvmStatic
        public fun constant(value: VectorValue): Expression = Constant(value)

        @JvmStatic
        public fun constant(value: Timestamp): Expression = Constant(value)

        @JvmStatic
        public fun constant(value: Number): Expression = Constant(value)

        @JvmStatic
        public fun constant(value: String): Expression = Constant(value)

        @JvmStatic
        @Deprecated(CONSTANT_OF_DATE_ANDROID_ONLY, ReplaceWith("constant(timestamp)"), DeprecationLevel.ERROR)
        public fun constant(value: Date): Expression = throw UnsupportedOperationException(CONSTANT_OF_DATE_ANDROID_ONLY)

        @JvmStatic
        public fun cosineDistance(vector1: Expression, vector2: Expression): Expression = function("cosine_distance", vector1, vector2)

        @JvmStatic
        public fun cosineDistance(vector1: Expression, vector2: VectorValue): Expression = function("cosine_distance", vector1, vector2)

        @JvmStatic
        public fun cosineDistance(vector1: Expression, vector2: DoubleArray): Expression = function("cosine_distance", vector1, vector2)

        @JvmStatic
        public fun cosineDistance(vectorFieldName: String, vector: Expression): Expression = function("cosine_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun cosineDistance(vectorFieldName: String, vector: VectorValue): Expression = function("cosine_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun cosineDistance(vectorFieldName: String, vector: DoubleArray): Expression = function("cosine_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun currentDocument(): Expression = function("current_document")

        @JvmStatic
        public fun currentTimestamp(): Expression = function("current_timestamp")

        @JvmStatic
        public fun divide(dividend: Expression, divisor: Expression): Expression = function("divide", dividend, divisor)

        @JvmStatic
        public fun divide(dividend: Expression, divisor: Number): Expression = function("divide", dividend, divisor)

        @JvmStatic
        public fun divide(dividendFieldName: String, divisor: Expression): Expression = function("divide", field(dividendFieldName), divisor)

        @JvmStatic
        public fun divide(dividendFieldName: String, divisor: Number): Expression = function("divide", field(dividendFieldName), divisor)

        @JvmStatic
        public fun documentId(docRef: DocumentReference): Expression = documentId(constant(docRef))

        @JvmStatic
        public fun documentId(documentPath: Expression): Expression = function("document_id", documentPath)

        @JvmStatic
        public fun documentId(documentPath: String): Expression = documentId(constant(documentPath))

        @JvmStatic
        public fun documentMatches(rquery: String): BooleanExpression = booleanFunction("document_matches", rquery)

        @JvmStatic
        public fun dotProduct(vector1: Expression, vector2: Expression): Expression = function("dot_product", vector1, vector2)

        @JvmStatic
        public fun dotProduct(vector1: Expression, vector2: VectorValue): Expression = function("dot_product", vector1, vector2)

        @JvmStatic
        public fun dotProduct(vector1: Expression, vector2: DoubleArray): Expression = function("dot_product", vector1, vector2)

        @JvmStatic
        public fun dotProduct(vectorFieldName: String, vector: Expression): Expression = function("dot_product", field(vectorFieldName), vector)

        @JvmStatic
        public fun dotProduct(vectorFieldName: String, vector: VectorValue): Expression = function("dot_product", field(vectorFieldName), vector)

        @JvmStatic
        public fun dotProduct(vectorFieldName: String, vector: DoubleArray): Expression = function("dot_product", field(vectorFieldName), vector)

        @JvmStatic
        public fun endsWith(stringExpr: Expression, suffix: Expression): BooleanExpression = booleanFunction("ends_with", stringExpr, suffix)

        @JvmStatic
        public fun endsWith(stringExpr: Expression, suffix: String): BooleanExpression = booleanFunction("ends_with", stringExpr, suffix)

        @JvmStatic
        public fun endsWith(fieldName: String, suffix: Expression): BooleanExpression = booleanFunction("ends_with", field(fieldName), suffix)

        @JvmStatic
        public fun endsWith(fieldName: String, suffix: String): BooleanExpression = booleanFunction("ends_with", field(fieldName), suffix)

        @JvmStatic
        public fun equal(left: Expression, right: Expression): BooleanExpression = booleanFunction("equal", left, right)

        @JvmStatic
        public fun equal(left: Expression, right: Any): BooleanExpression = booleanFunction("equal", left, right)

        @JvmStatic
        public fun equal(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("equal", field(fieldName), expression)

        @JvmStatic
        public fun equal(fieldName: String, value: Any): BooleanExpression = booleanFunction("equal", field(fieldName), value)

        @JvmStatic
        public fun equalAny(expression: Expression, arrayExpression: Expression): BooleanExpression = booleanFunction("equal_any", expression, arrayExpression)

        @JvmStatic
        public fun equalAny(expression: Expression, values: List<*>): BooleanExpression = equalAny(expression, array(values))

        @JvmStatic
        public fun equalAny(fieldName: String, arrayExpression: Expression): BooleanExpression = booleanFunction("equal_any", field(fieldName), arrayExpression)

        @JvmStatic
        public fun equalAny(fieldName: String, values: List<*>): BooleanExpression = equalAny(fieldName, array(values))

        @JvmStatic
        public fun euclideanDistance(vector1: Expression, vector2: Expression): Expression = function("euclidean_distance", vector1, vector2)

        @JvmStatic
        public fun euclideanDistance(vector1: Expression, vector2: VectorValue): Expression = function("euclidean_distance", vector1, vector2)

        @JvmStatic
        public fun euclideanDistance(vector1: Expression, vector2: DoubleArray): Expression = function("euclidean_distance", vector1, vector2)

        @JvmStatic
        public fun euclideanDistance(vectorFieldName: String, vector: Expression): Expression = function("euclidean_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun euclideanDistance(vectorFieldName: String, vector: VectorValue): Expression = function("euclidean_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun euclideanDistance(vectorFieldName: String, vector: DoubleArray): Expression = function("euclidean_distance", field(vectorFieldName), vector)

        @JvmStatic
        public fun exists(value: Expression): BooleanExpression = booleanFunction("exists", value)

        @JvmStatic
        public fun exists(fieldName: String): BooleanExpression = booleanFunction("exists", field(fieldName))

        @JvmStatic
        public fun exp(numericExpr: Expression): Expression = function("exp", numericExpr)

        @JvmStatic
        public fun exp(numericField: String): Expression = function("exp", field(numericField))

        @JvmStatic
        public fun field(fieldPath: FieldPath): Field = Field(fieldPath)

        @JvmStatic
        public fun field(name: String): Field = Field.of(name)

        @JvmStatic
        public fun floor(numericExpr: Expression): Expression = function("floor", numericExpr)

        @JvmStatic
        public fun floor(numericField: String): Expression = function("floor", field(numericField))

        @JvmStatic
        public fun geoDistance(field: Field, location: GeoPoint): Expression = function("geo_distance", field, location)

        @JvmStatic
        public fun geoDistance(fieldName: String, location: GeoPoint): Expression = geoDistance(field(fieldName), location)

        @JvmStatic
        public fun getField(expression: Expression, keyExpression: Expression): Expression = function("get_field", expression, keyExpression)

        @JvmStatic
        public fun getField(expression: Expression, key: String): Expression = function("get_field", expression, key)

        @JvmStatic
        public fun getField(fieldName: String, keyExpression: Expression): Expression = function("get_field", field(fieldName), keyExpression)

        @JvmStatic
        public fun getField(fieldName: String, key: String): Expression = function("get_field", field(fieldName), key)

        @JvmStatic
        public fun greaterThan(left: Expression, right: Expression): BooleanExpression = booleanFunction("greater_than", left, right)

        @JvmStatic
        public fun greaterThan(left: Expression, right: Any): BooleanExpression = booleanFunction("greater_than", left, right)

        @JvmStatic
        public fun greaterThan(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("greater_than", field(fieldName), expression)

        @JvmStatic
        public fun greaterThan(fieldName: String, value: Any): BooleanExpression = booleanFunction("greater_than", field(fieldName), value)

        @JvmStatic
        public fun greaterThanOrEqual(left: Expression, right: Expression): BooleanExpression = booleanFunction("greater_than_or_equal", left, right)

        @JvmStatic
        public fun greaterThanOrEqual(left: Expression, right: Any): BooleanExpression = booleanFunction("greater_than_or_equal", left, right)

        @JvmStatic
        public fun greaterThanOrEqual(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("greater_than_or_equal", field(fieldName), expression)

        @JvmStatic
        public fun greaterThanOrEqual(fieldName: String, value: Any): BooleanExpression = booleanFunction("greater_than_or_equal", field(fieldName), value)

        @JvmStatic
        public fun ifAbsent(ifExpr: Expression, elseExpr: Expression): Expression = function("if_absent", ifExpr, elseExpr)

        @JvmStatic
        public fun ifAbsent(ifExpr: Expression, elseValue: Any): Expression = function("if_absent", ifExpr, elseValue)

        @JvmStatic
        public fun ifAbsent(ifFieldName: String, elseExpr: Expression): Expression = function("if_absent", field(ifFieldName), elseExpr)

        @JvmStatic
        public fun ifAbsent(ifFieldName: String, elseValue: Any): Expression = function("if_absent", field(ifFieldName), elseValue)

        @JvmStatic
        public fun ifError(tryExpr: BooleanExpression, catchExpr: BooleanExpression): BooleanExpression = booleanFunction("if_error", tryExpr, catchExpr)

        @JvmStatic
        public fun ifError(tryExpr: Expression, catchExpr: Expression): Expression = function("if_error", tryExpr, catchExpr)

        @JvmStatic
        public fun ifError(tryExpr: Expression, catchValue: Any): Expression = function("if_error", tryExpr, catchValue)

        @JvmStatic
        public fun ifNull(ifExpr: Expression, elseExpr: Expression): Expression = function("if_null", ifExpr, elseExpr)

        @JvmStatic
        public fun ifNull(ifExpr: Expression, elseValue: Any): Expression = function("if_null", ifExpr, elseValue)

        @JvmStatic
        public fun ifNull(ifFieldName: String, elseExpr: Expression): Expression = function("if_null", field(ifFieldName), elseExpr)

        @JvmStatic
        public fun ifNull(ifFieldName: String, elseValue: Any): Expression = function("if_null", field(ifFieldName), elseValue)

        @JvmStatic
        public fun isAbsent(value: Expression): BooleanExpression = booleanFunction("is_absent", value)

        @JvmStatic
        public fun isAbsent(fieldName: String): BooleanExpression = booleanFunction("is_absent", field(fieldName))

        @JvmStatic
        public fun isError(expr: Expression): BooleanExpression = booleanFunction("is_error", expr)

        @JvmStatic
        public fun isType(expr: Expression, type: String): BooleanExpression = booleanFunction("is_type", expr, type)

        @JvmStatic
        public fun isType(fieldName: String, type: String): BooleanExpression = booleanFunction("is_type", field(fieldName), type)

        @JvmStatic
        public fun join(arrayExpression: Expression, delimiterExpression: Expression): Expression = function("join", arrayExpression, delimiterExpression)

        @JvmStatic
        public fun join(arrayExpression: Expression, delimiter: String): Expression = function("join", arrayExpression, delimiter)

        @JvmStatic
        public fun join(arrayFieldName: String, delimiterExpression: Expression): Expression = function("join", field(arrayFieldName), delimiterExpression)

        @JvmStatic
        public fun join(arrayFieldName: String, delimiter: String): Expression = function("join", field(arrayFieldName), delimiter)

        @JvmStatic
        public fun length(expr: Expression): Expression = function("length", expr)

        @JvmStatic
        public fun length(fieldName: String): Expression = function("length", field(fieldName))

        @JvmStatic
        public fun lessThan(left: Expression, right: Expression): BooleanExpression = booleanFunction("less_than", left, right)

        @JvmStatic
        public fun lessThan(left: Expression, right: Any): BooleanExpression = booleanFunction("less_than", left, right)

        @JvmStatic
        public fun lessThan(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("less_than", field(fieldName), expression)

        @JvmStatic
        public fun lessThan(fieldName: String, value: Any): BooleanExpression = booleanFunction("less_than", field(fieldName), value)

        @JvmStatic
        public fun lessThanOrEqual(left: Expression, right: Expression): BooleanExpression = booleanFunction("less_than_or_equal", left, right)

        @JvmStatic
        public fun lessThanOrEqual(left: Expression, right: Any): BooleanExpression = booleanFunction("less_than_or_equal", left, right)

        @JvmStatic
        public fun lessThanOrEqual(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("less_than_or_equal", field(fieldName), expression)

        @JvmStatic
        public fun lessThanOrEqual(fieldName: String, value: Any): BooleanExpression = booleanFunction("less_than_or_equal", field(fieldName), value)

        @JvmStatic
        public fun like(stringExpression: Expression, pattern: Expression): BooleanExpression = booleanFunction("like", stringExpression, pattern)

        @JvmStatic
        public fun like(stringExpression: Expression, pattern: String): BooleanExpression = booleanFunction("like", stringExpression, pattern)

        @JvmStatic
        public fun like(fieldName: String, pattern: Expression): BooleanExpression = booleanFunction("like", field(fieldName), pattern)

        @JvmStatic
        public fun like(fieldName: String, pattern: String): BooleanExpression = booleanFunction("like", field(fieldName), pattern)

        @JvmStatic
        public fun ln(numericExpr: Expression): Expression = function("ln", numericExpr)

        @JvmStatic
        public fun ln(numericField: String): Expression = function("ln", field(numericField))

        @JvmStatic
        public fun log(numericExpr: Expression, base: Expression): Expression = function("log", numericExpr, base)

        @JvmStatic
        public fun log(numericExpr: Expression, base: Number): Expression = function("log", numericExpr, base)

        @JvmStatic
        public fun log(numericField: String, base: Expression): Expression = function("log", field(numericField), base)

        @JvmStatic
        public fun log(numericField: String, base: Number): Expression = function("log", field(numericField), base)

        @JvmStatic
        public fun log10(numericExpr: Expression): Expression = function("log10", numericExpr)

        @JvmStatic
        public fun log10(numericField: String): Expression = function("log10", field(numericField))

        @JvmStatic
        public fun logicalMaximum(expr: Expression, vararg others: Any): Expression = function("maximum", expr, *others)

        @JvmStatic
        public fun logicalMaximum(fieldName: String, vararg others: Any): Expression = function("maximum", field(fieldName), *others)

        @JvmStatic
        public fun logicalMinimum(expr: Expression, vararg others: Any): Expression = function("minimum", expr, *others)

        @JvmStatic
        public fun logicalMinimum(fieldName: String, vararg others: Any): Expression = function("minimum", field(fieldName), *others)

        @JvmStatic
        public fun ltrim(stringExpression: Expression): Expression = function("ltrim", stringExpression)

        @JvmStatic
        public fun ltrim(fieldName: String): Expression = function("ltrim", field(fieldName))

        @JvmStatic
        public fun ltrimValue(stringExpression: Expression, valuesToTrim: Expression): Expression = function("ltrim", stringExpression, valuesToTrim)

        @JvmStatic
        public fun ltrimValue(stringExpression: Expression, valuesToTrim: String): Expression = function("ltrim", stringExpression, valuesToTrim)

        @JvmStatic
        public fun ltrimValue(fieldName: String, valuesToTrim: Expression): Expression = function("ltrim", field(fieldName), valuesToTrim)

        @JvmStatic
        public fun ltrimValue(fieldName: String, valuesToTrim: String): Expression = function("ltrim", field(fieldName), valuesToTrim)

        @JvmStatic
        public fun map(elements: Map<String, *>): Expression = function("map", *elements.flatMap { (key, value) -> listOf(constant(key), value) }.toTypedArray())

        @JvmStatic
        public fun mapEntries(mapExpr: Expression): Expression = function("map_entries", mapExpr)

        @JvmStatic
        public fun mapEntries(mapField: String): Expression = function("map_entries", field(mapField))

        @JvmStatic
        public fun mapGet(mapExpression: Expression, keyExpression: Expression): Expression = function("map_get", mapExpression, keyExpression)

        @JvmStatic
        public fun mapGet(mapExpression: Expression, key: String): Expression = function("map_get", mapExpression, key)

        @JvmStatic
        public fun mapGet(fieldName: String, keyExpression: Expression): Expression = function("map_get", field(fieldName), keyExpression)

        @JvmStatic
        public fun mapGet(fieldName: String, key: String): Expression = function("map_get", field(fieldName), key)

        @JvmStatic
        public fun mapKeys(mapExpr: Expression): Expression = function("map_keys", mapExpr)

        @JvmStatic
        public fun mapKeys(mapField: String): Expression = function("map_keys", field(mapField))

        @JvmStatic
        public fun mapMerge(firstMap: Expression, secondMap: Expression, vararg otherMaps: Expression): Expression = function("map_merge", firstMap, secondMap, *otherMaps)

        @JvmStatic
        public fun mapMerge(firstMapFieldName: String, secondMap: Expression, vararg otherMaps: Expression): Expression = function("map_merge", field(firstMapFieldName), secondMap, *otherMaps)

        @JvmStatic
        public fun mapRemove(mapExpr: Expression, key: Expression): Expression = function("map_remove", mapExpr, key)

        @JvmStatic
        public fun mapRemove(mapExpr: Expression, key: String): Expression = function("map_remove", mapExpr, key)

        @JvmStatic
        public fun mapRemove(mapField: String, key: Expression): Expression = function("map_remove", field(mapField), key)

        @JvmStatic
        public fun mapRemove(mapField: String, key: String): Expression = function("map_remove", field(mapField), key)

        @JvmStatic
        public fun mapSet(mapExpr: Expression, key: Expression, value: Expression, vararg moreKeyValues: Expression): Expression = function("map_set", mapExpr, key, value, *moreKeyValues)

        @JvmStatic
        public fun mapSet(mapExpr: Expression, key: String, value: Any?, vararg moreKeyValues: Any): Expression = function("map_set", mapExpr, key, value, *moreKeyValues)

        @JvmStatic
        public fun mapSet(mapField: String, key: Expression, value: Expression, vararg moreKeyValues: Expression): Expression = function("map_set", field(mapField), key, value, *moreKeyValues)

        @JvmStatic
        public fun mapSet(mapField: String, key: String, value: Any?, vararg moreKeyValues: Any): Expression = function("map_set", field(mapField), key, value, *moreKeyValues)

        @JvmStatic
        public fun mapValues(mapExpr: Expression): Expression = function("map_values", mapExpr)

        @JvmStatic
        public fun mapValues(mapField: String): Expression = function("map_values", field(mapField))

        @JvmStatic
        public fun mod(dividend: Expression, divisor: Expression): Expression = function("mod", dividend, divisor)

        @JvmStatic
        public fun mod(dividend: Expression, divisor: Number): Expression = function("mod", dividend, divisor)

        @JvmStatic
        public fun mod(dividendFieldName: String, divisor: Expression): Expression = function("mod", field(dividendFieldName), divisor)

        @JvmStatic
        public fun mod(dividendFieldName: String, divisor: Number): Expression = function("mod", field(dividendFieldName), divisor)

        @JvmStatic
        public fun multiply(first: Expression, second: Expression): Expression = function("multiply", first, second)

        @JvmStatic
        public fun multiply(first: Expression, second: Number): Expression = function("multiply", first, second)

        @JvmStatic
        public fun multiply(numericFieldName: String, second: Expression): Expression = function("multiply", field(numericFieldName), second)

        @JvmStatic
        public fun multiply(numericFieldName: String, second: Number): Expression = function("multiply", field(numericFieldName), second)

        @JvmStatic
        public fun nor(condition: BooleanExpression, vararg conditions: BooleanExpression): BooleanExpression = booleanFunction("nor", condition, *conditions)

        @JvmStatic
        public fun not(condition: BooleanExpression): BooleanExpression = booleanFunction("not", condition)

        @JvmStatic
        public fun notEqual(left: Expression, right: Expression): BooleanExpression = booleanFunction("not_equal", left, right)

        @JvmStatic
        public fun notEqual(left: Expression, right: Any): BooleanExpression = booleanFunction("not_equal", left, right)

        @JvmStatic
        public fun notEqual(fieldName: String, expression: Expression): BooleanExpression = booleanFunction("not_equal", field(fieldName), expression)

        @JvmStatic
        public fun notEqual(fieldName: String, value: Any): BooleanExpression = booleanFunction("not_equal", field(fieldName), value)

        @JvmStatic
        public fun notEqualAny(expression: Expression, arrayExpression: Expression): BooleanExpression = booleanFunction("not_equal_any", expression, arrayExpression)

        @JvmStatic
        public fun notEqualAny(expression: Expression, values: List<*>): BooleanExpression = notEqualAny(expression, array(values))

        @JvmStatic
        public fun notEqualAny(fieldName: String, arrayExpression: Expression): BooleanExpression = booleanFunction("not_equal_any", field(fieldName), arrayExpression)

        @JvmStatic
        public fun notEqualAny(fieldName: String, values: List<*>): BooleanExpression = notEqualAny(fieldName, array(values))

        @JvmStatic
        public fun nullValue(): Expression = Constant(null)

        @JvmStatic
        public fun or(condition: BooleanExpression, vararg conditions: BooleanExpression): BooleanExpression = booleanFunction("or", condition, *conditions)

        @JvmStatic
        public fun parent(docRef: DocumentReference): Expression = parent(constant(docRef))

        @JvmStatic
        public fun parent(documentPath: Expression): Expression = function("parent", documentPath)

        @JvmStatic
        public fun parent(documentPath: String): Expression = parent(constant(documentPath))

        @JvmStatic
        public fun pow(numericExpr: Expression, exponent: Expression): Expression = function("pow", numericExpr, exponent)

        @JvmStatic
        public fun pow(numericExpr: Expression, exponent: Number): Expression = function("pow", numericExpr, exponent)

        @JvmStatic
        public fun pow(numericField: String, exponent: Expression): Expression = function("pow", field(numericField), exponent)

        @JvmStatic
        public fun pow(numericField: String, exponent: Number): Expression = function("pow", field(numericField), exponent)

        @JvmStatic
        public fun rand(): Expression = function("rand")

        @JvmStatic
        public fun rawFunction(name: String, vararg expr: Expression): Expression = FunctionExpression(name, expr.toList())

        @JvmStatic
        public fun regexContains(stringExpression: Expression, pattern: Expression): BooleanExpression = booleanFunction("regex_contains", stringExpression, pattern)

        @JvmStatic
        public fun regexContains(stringExpression: Expression, pattern: String): BooleanExpression = booleanFunction("regex_contains", stringExpression, pattern)

        @JvmStatic
        public fun regexContains(fieldName: String, pattern: Expression): BooleanExpression = booleanFunction("regex_contains", field(fieldName), pattern)

        @JvmStatic
        public fun regexContains(fieldName: String, pattern: String): BooleanExpression = booleanFunction("regex_contains", field(fieldName), pattern)

        @JvmStatic
        public fun regexFind(stringExpression: Expression, pattern: Expression): Expression = function("regex_find", stringExpression, pattern)

        @JvmStatic
        public fun regexFind(stringExpression: Expression, pattern: String): Expression = function("regex_find", stringExpression, pattern)

        @JvmStatic
        public fun regexFind(fieldName: String, pattern: Expression): Expression = function("regex_find", field(fieldName), pattern)

        @JvmStatic
        public fun regexFind(fieldName: String, pattern: String): Expression = function("regex_find", field(fieldName), pattern)

        @JvmStatic
        public fun regexFindAll(stringExpression: Expression, pattern: Expression): Expression = function("regex_find_all", stringExpression, pattern)

        @JvmStatic
        public fun regexFindAll(stringExpression: Expression, pattern: String): Expression = function("regex_find_all", stringExpression, pattern)

        @JvmStatic
        public fun regexFindAll(fieldName: String, pattern: Expression): Expression = function("regex_find_all", field(fieldName), pattern)

        @JvmStatic
        public fun regexFindAll(fieldName: String, pattern: String): Expression = function("regex_find_all", field(fieldName), pattern)

        @JvmStatic
        public fun regexMatch(stringExpression: Expression, pattern: Expression): BooleanExpression = booleanFunction("regex_match", stringExpression, pattern)

        @JvmStatic
        public fun regexMatch(stringExpression: Expression, pattern: String): BooleanExpression = booleanFunction("regex_match", stringExpression, pattern)

        @JvmStatic
        public fun regexMatch(fieldName: String, pattern: Expression): BooleanExpression = booleanFunction("regex_match", field(fieldName), pattern)

        @JvmStatic
        public fun regexMatch(fieldName: String, pattern: String): BooleanExpression = booleanFunction("regex_match", field(fieldName), pattern)

        @JvmStatic
        public fun reverse(stringExpression: Expression): Expression = function("reverse", stringExpression)

        @JvmStatic
        public fun reverse(fieldName: String): Expression = function("reverse", field(fieldName))

        @JvmStatic
        public fun round(numericExpr: Expression): Expression = function("round", numericExpr)

        @JvmStatic
        public fun round(numericField: String): Expression = function("round", field(numericField))

        @JvmStatic
        public fun roundToPrecision(numericExpr: Expression, decimalPlace: Expression): Expression = function("round", numericExpr, decimalPlace)

        @JvmStatic
        public fun roundToPrecision(numericExpr: Expression, decimalPlace: Int): Expression = function("round", numericExpr, decimalPlace)

        @JvmStatic
        public fun roundToPrecision(numericField: String, decimalPlace: Expression): Expression = function("round", field(numericField), decimalPlace)

        @JvmStatic
        public fun roundToPrecision(numericField: String, decimalPlace: Int): Expression = function("round", field(numericField), decimalPlace)

        @JvmStatic
        public fun rtrim(stringExpression: Expression): Expression = function("rtrim", stringExpression)

        @JvmStatic
        public fun rtrim(fieldName: String): Expression = function("rtrim", field(fieldName))

        @JvmStatic
        public fun rtrimValue(stringExpression: Expression, valuesToTrim: Expression): Expression = function("rtrim", stringExpression, valuesToTrim)

        @JvmStatic
        public fun rtrimValue(stringExpression: Expression, valuesToTrim: String): Expression = function("rtrim", stringExpression, valuesToTrim)

        @JvmStatic
        public fun rtrimValue(fieldName: String, valuesToTrim: Expression): Expression = function("rtrim", field(fieldName), valuesToTrim)

        @JvmStatic
        public fun rtrimValue(fieldName: String, valuesToTrim: String): Expression = function("rtrim", field(fieldName), valuesToTrim)

        @JvmStatic
        public fun score(): Expression = function("score")

        @JvmStatic
        public fun split(value: Expression, delimiter: Blob): Expression = function("split", value, delimiter)

        @JvmStatic
        public fun split(value: Expression, delimiter: Expression): Expression = function("split", value, delimiter)

        @JvmStatic
        public fun split(value: Expression, delimiter: String): Expression = function("split", value, delimiter)

        @JvmStatic
        public fun split(fieldName: String, delimiter: Blob): Expression = function("split", field(fieldName), delimiter)

        @JvmStatic
        public fun split(fieldName: String, delimiter: Expression): Expression = function("split", field(fieldName), delimiter)

        @JvmStatic
        public fun split(fieldName: String, delimiter: String): Expression = function("split", field(fieldName), delimiter)

        @JvmStatic
        public fun sqrt(numericExpr: Expression): Expression = function("sqrt", numericExpr)

        @JvmStatic
        public fun sqrt(numericField: String): Expression = function("sqrt", field(numericField))

        @JvmStatic
        public fun startsWith(stringExpr: Expression, prefix: Expression): BooleanExpression = booleanFunction("starts_with", stringExpr, prefix)

        @JvmStatic
        public fun startsWith(stringExpr: Expression, prefix: String): BooleanExpression = booleanFunction("starts_with", stringExpr, prefix)

        @JvmStatic
        public fun startsWith(fieldName: String, prefix: Expression): BooleanExpression = booleanFunction("starts_with", field(fieldName), prefix)

        @JvmStatic
        public fun startsWith(fieldName: String, prefix: String): BooleanExpression = booleanFunction("starts_with", field(fieldName), prefix)

        @JvmStatic
        public fun stringConcat(firstString: Expression, vararg otherStrings: Expression): Expression = function("string_concat", firstString, *otherStrings)

        @JvmStatic
        public fun stringConcat(firstString: Expression, vararg otherStrings: Any): Expression = function("string_concat", firstString, *otherStrings)

        @JvmStatic
        public fun stringConcat(fieldName: String, vararg otherStrings: Expression): Expression = function("string_concat", field(fieldName), *otherStrings)

        @JvmStatic
        public fun stringConcat(fieldName: String, vararg otherStrings: Any): Expression = function("string_concat", field(fieldName), *otherStrings)

        @JvmStatic
        public fun stringContains(stringExpression: Expression, substring: Expression): BooleanExpression = booleanFunction("string_contains", stringExpression, substring)

        @JvmStatic
        public fun stringContains(stringExpression: Expression, substring: String): BooleanExpression = booleanFunction("string_contains", stringExpression, substring)

        @JvmStatic
        public fun stringContains(fieldName: String, substring: Expression): BooleanExpression = booleanFunction("string_contains", field(fieldName), substring)

        @JvmStatic
        public fun stringContains(fieldName: String, substring: String): BooleanExpression = booleanFunction("string_contains", field(fieldName), substring)

        @JvmStatic
        public fun stringIndexOf(stringExpression: Expression, substring: Expression): Expression = function("string_index_of", stringExpression, substring)

        @JvmStatic
        public fun stringIndexOf(stringExpression: Expression, substring: String): Expression = function("string_index_of", stringExpression, substring)

        @JvmStatic
        public fun stringIndexOf(fieldName: String, substring: Expression): Expression = function("string_index_of", field(fieldName), substring)

        @JvmStatic
        public fun stringIndexOf(fieldName: String, substring: String): Expression = function("string_index_of", field(fieldName), substring)

        @JvmStatic
        public fun stringRepeat(stringExpression: Expression, count: Expression): Expression = function("string_repeat", stringExpression, count)

        @JvmStatic
        public fun stringRepeat(stringExpression: Expression, count: Int): Expression = function("string_repeat", stringExpression, count)

        @JvmStatic
        public fun stringRepeat(fieldName: String, count: Expression): Expression = function("string_repeat", field(fieldName), count)

        @JvmStatic
        public fun stringRepeat(fieldName: String, count: Int): Expression = function("string_repeat", field(fieldName), count)

        @JvmStatic
        public fun stringReplaceAll(stringExpression: Expression, oldValue: Expression, newValue: Expression): Expression = function("string_replace_all", stringExpression, oldValue, newValue)

        @JvmStatic
        public fun stringReplaceAll(stringExpression: Expression, oldValue: String, newValue: String): Expression = function("string_replace_all", stringExpression, oldValue, newValue)

        @JvmStatic
        public fun stringReplaceAll(fieldName: String, oldValue: Expression, newValue: Expression): Expression = function("string_replace_all", field(fieldName), oldValue, newValue)

        @JvmStatic
        public fun stringReplaceAll(fieldName: String, oldValue: String, newValue: String): Expression = function("string_replace_all", field(fieldName), oldValue, newValue)

        @JvmStatic
        public fun stringReplaceOne(stringExpression: Expression, oldValue: Expression, newValue: Expression): Expression = function("string_replace_one", stringExpression, oldValue, newValue)

        @JvmStatic
        public fun stringReplaceOne(stringExpression: Expression, oldValue: String, newValue: String): Expression = function("string_replace_one", stringExpression, oldValue, newValue)

        @JvmStatic
        public fun stringReplaceOne(fieldName: String, oldValue: Expression, newValue: Expression): Expression = function("string_replace_one", field(fieldName), oldValue, newValue)

        @JvmStatic
        public fun stringReplaceOne(fieldName: String, oldValue: String, newValue: String): Expression = function("string_replace_one", field(fieldName), oldValue, newValue)

        @JvmStatic
        public fun stringReverse(str: Expression): Expression = function("string_reverse", str)

        @JvmStatic
        public fun stringReverse(fieldName: String): Expression = function("string_reverse", field(fieldName))

        @JvmStatic
        public fun substring(stringExpression: Expression, index: Expression, length: Expression): Expression = function("substring", stringExpression, index, length)

        @JvmStatic
        public fun substring(fieldName: String, index: Int, length: Int): Expression = function("substring", field(fieldName), index, length)

        @JvmStatic
        public fun subtract(minuend: Expression, subtrahend: Expression): Expression = function("subtract", minuend, subtrahend)

        @JvmStatic
        public fun subtract(minuend: Expression, subtrahend: Number): Expression = function("subtract", minuend, subtrahend)

        @JvmStatic
        public fun subtract(numericFieldName: String, subtrahend: Expression): Expression = function("subtract", field(numericFieldName), subtrahend)

        @JvmStatic
        public fun subtract(numericFieldName: String, subtrahend: Number): Expression = function("subtract", field(numericFieldName), subtrahend)

        @JvmStatic
        public fun switchOn(condition: BooleanExpression, result: Any, vararg others: Any): Expression = function("switch_on", condition, result, *others)

        @JvmStatic
        public fun timestampAdd(timestamp: Expression, unit: Expression, amount: Expression): Expression = function("timestamp_add", timestamp, unit, amount)

        @JvmStatic
        public fun timestampAdd(timestamp: Expression, unit: String, amount: Long): Expression = function("timestamp_add", timestamp, unit, amount)

        @JvmStatic
        public fun timestampAdd(fieldName: String, unit: Expression, amount: Expression): Expression = function("timestamp_add", field(fieldName), unit, amount)

        @JvmStatic
        public fun timestampAdd(fieldName: String, unit: String, amount: Long): Expression = function("timestamp_add", field(fieldName), unit, amount)

        @JvmStatic
        public fun timestampDiff(end: Expression, start: Expression, unit: Expression): Expression = function("timestamp_diff", end, start, unit)

        @JvmStatic
        public fun timestampDiff(end: Expression, start: Expression, unit: String): Expression = timestampDiff(end, start, constant(unit))

        @JvmStatic
        public fun timestampDiff(end: Expression, startFieldName: String, unit: String): Expression = timestampDiff(end, field(startFieldName), constant(unit))

        @JvmStatic
        public fun timestampDiff(endFieldName: String, start: Expression, unit: String): Expression = timestampDiff(field(endFieldName), start, constant(unit))

        @JvmStatic
        public fun timestampDiff(endFieldName: String, startFieldName: String, unit: String): Expression = timestampDiff(field(endFieldName), field(startFieldName), constant(unit))

        @JvmStatic
        public fun timestampExtract(timestamp: Expression, part: Expression): Expression = function("timestamp_extract", timestamp, part)

        @JvmStatic
        public fun timestampExtract(timestamp: Expression, part: String): Expression = timestampExtract(timestamp, constant(part))

        @JvmStatic
        public fun timestampExtract(fieldName: String, part: Expression): Expression = timestampExtract(field(fieldName), part)

        @JvmStatic
        public fun timestampExtract(fieldName: String, part: String): Expression = timestampExtract(field(fieldName), constant(part))

        @JvmStatic
        public fun timestampExtractWithTimezone(timestamp: Expression, part: Expression, timezone: Expression): Expression = function("timestamp_extract", timestamp, part, timezone)

        @JvmStatic
        public fun timestampExtractWithTimezone(timestamp: Expression, part: Expression, timezone: String): Expression = timestampExtractWithTimezone(timestamp, part, constant(timezone))

        @JvmStatic
        public fun timestampExtractWithTimezone(timestamp: Expression, part: String, timezone: Expression): Expression = timestampExtractWithTimezone(timestamp, constant(part), timezone)

        @JvmStatic
        public fun timestampExtractWithTimezone(timestamp: Expression, part: String, timezone: String): Expression = timestampExtractWithTimezone(timestamp, constant(part), constant(timezone))

        @JvmStatic
        public fun timestampExtractWithTimezone(fieldName: String, part: Expression, timezone: Expression): Expression = timestampExtractWithTimezone(field(fieldName), part, timezone)

        @JvmStatic
        public fun timestampExtractWithTimezone(fieldName: String, part: Expression, timezone: String): Expression = timestampExtractWithTimezone(field(fieldName), part, constant(timezone))

        @JvmStatic
        public fun timestampExtractWithTimezone(fieldName: String, part: String, timezone: Expression): Expression = timestampExtractWithTimezone(field(fieldName), constant(part), timezone)

        @JvmStatic
        public fun timestampExtractWithTimezone(fieldName: String, part: String, timezone: String): Expression = timestampExtractWithTimezone(field(fieldName), constant(part), constant(timezone))

        @JvmStatic
        public fun timestampSubtract(timestamp: Expression, unit: Expression, amount: Expression): Expression = function("timestamp_subtract", timestamp, unit, amount)

        @JvmStatic
        public fun timestampSubtract(timestamp: Expression, unit: String, amount: Long): Expression = function("timestamp_subtract", timestamp, unit, amount)

        @JvmStatic
        public fun timestampSubtract(fieldName: String, unit: Expression, amount: Expression): Expression = function("timestamp_subtract", field(fieldName), unit, amount)

        @JvmStatic
        public fun timestampSubtract(fieldName: String, unit: String, amount: Long): Expression = function("timestamp_subtract", field(fieldName), unit, amount)

        @JvmStatic
        public fun timestampToUnixMicros(expr: Expression): Expression = function("timestamp_to_unix_micros", expr)

        @JvmStatic
        public fun timestampToUnixMicros(fieldName: String): Expression = function("timestamp_to_unix_micros", field(fieldName))

        @JvmStatic
        public fun timestampToUnixMillis(expr: Expression): Expression = function("timestamp_to_unix_millis", expr)

        @JvmStatic
        public fun timestampToUnixMillis(fieldName: String): Expression = function("timestamp_to_unix_millis", field(fieldName))

        @JvmStatic
        public fun timestampToUnixSeconds(expr: Expression): Expression = function("timestamp_to_unix_seconds", expr)

        @JvmStatic
        public fun timestampToUnixSeconds(fieldName: String): Expression = function("timestamp_to_unix_seconds", field(fieldName))

        @JvmStatic
        public fun timestampTruncate(timestamp: Expression, granularity: Expression): Expression = function("timestamp_trunc", timestamp, granularity)

        @JvmStatic
        public fun timestampTruncate(timestamp: Expression, granularity: String): Expression = function("timestamp_trunc", timestamp, granularity)

        @JvmStatic
        public fun timestampTruncate(fieldName: String, granularity: Expression): Expression = function("timestamp_trunc", field(fieldName), granularity)

        @JvmStatic
        public fun timestampTruncate(fieldName: String, granularity: String): Expression = function("timestamp_trunc", field(fieldName), granularity)

        @JvmStatic
        public fun timestampTruncateWithTimezone(timestamp: Expression, granularity: Expression, timezone: Expression): Expression = function("timestamp_trunc", timestamp, granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(timestamp: Expression, granularity: Expression, timezone: String): Expression = function("timestamp_trunc", timestamp, granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(timestamp: Expression, granularity: String, timezone: Expression): Expression = function("timestamp_trunc", timestamp, granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(timestamp: Expression, granularity: String, timezone: String): Expression = function("timestamp_trunc", timestamp, granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(fieldName: String, granularity: Expression, timezone: Expression): Expression = function("timestamp_trunc", field(fieldName), granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(fieldName: String, granularity: Expression, timezone: String): Expression = function("timestamp_trunc", field(fieldName), granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(fieldName: String, granularity: String, timezone: Expression): Expression = function("timestamp_trunc", field(fieldName), granularity, timezone)

        @JvmStatic
        public fun timestampTruncateWithTimezone(fieldName: String, granularity: String, timezone: String): Expression = function("timestamp_trunc", field(fieldName), granularity, timezone)

        @JvmStatic
        public fun toLower(stringExpression: Expression): Expression = function("to_lower", stringExpression)

        @JvmStatic
        public fun toLower(fieldName: String): Expression = function("to_lower", field(fieldName))

        @JvmStatic
        public fun toUpper(stringExpression: Expression): Expression = function("to_upper", stringExpression)

        @JvmStatic
        public fun toUpper(fieldName: String): Expression = function("to_upper", field(fieldName))

        @JvmStatic
        public fun trim(stringExpression: Expression): Expression = function("trim", stringExpression)

        @JvmStatic
        public fun trim(fieldName: String): Expression = function("trim", field(fieldName))

        @JvmStatic
        public fun trimValue(stringExpression: Expression, valueToTrim: Expression): Expression = function("trim", stringExpression, valueToTrim)

        @JvmStatic
        public fun trimValue(fieldName: String, valueToTrim: String): Expression = function("trim", field(fieldName), valueToTrim)

        @JvmStatic
        public fun trunc(numericExpr: Expression): Expression = function("trunc", numericExpr)

        @JvmStatic
        public fun trunc(numericField: String): Expression = function("trunc", field(numericField))

        @JvmStatic
        public fun truncToPrecision(numericExpr: Expression, decimalPlace: Expression): Expression = function("trunc", numericExpr, decimalPlace)

        @JvmStatic
        public fun truncToPrecision(numericExpr: Expression, decimalPlace: Int): Expression = function("trunc", numericExpr, decimalPlace)

        @JvmStatic
        public fun truncToPrecision(numericField: String, decimalPlace: Expression): Expression = function("trunc", field(numericField), decimalPlace)

        @JvmStatic
        public fun truncToPrecision(numericField: String, decimalPlace: Int): Expression = function("trunc", field(numericField), decimalPlace)

        @JvmStatic
        public fun type(expr: Expression): Expression = function("type", expr)

        @JvmStatic
        public fun type(fieldName: String): Expression = function("type", field(fieldName))

        @JvmStatic
        public fun unixMicrosToTimestamp(expr: Expression): Expression = function("unix_micros_to_timestamp", expr)

        @JvmStatic
        public fun unixMicrosToTimestamp(fieldName: String): Expression = function("unix_micros_to_timestamp", field(fieldName))

        @JvmStatic
        public fun unixMillisToTimestamp(expr: Expression): Expression = function("unix_millis_to_timestamp", expr)

        @JvmStatic
        public fun unixMillisToTimestamp(fieldName: String): Expression = function("unix_millis_to_timestamp", field(fieldName))

        @JvmStatic
        public fun unixSecondsToTimestamp(expr: Expression): Expression = function("unix_seconds_to_timestamp", expr)

        @JvmStatic
        public fun unixSecondsToTimestamp(fieldName: String): Expression = function("unix_seconds_to_timestamp", field(fieldName))

        @JvmStatic
        public fun variable(name: String): Expression = Variable(name)

        @JvmStatic
        public fun vector(vector: VectorValue): Expression = Constant(vector)

        @JvmStatic
        public fun vector(vector: DoubleArray): Expression = Constant(VectorValue.of(vector))

        @JvmStatic
        public fun vectorLength(vectorExpression: Expression): Expression = function("vector_length", vectorExpression)

        @JvmStatic
        public fun vectorLength(fieldName: String): Expression = function("vector_length", field(fieldName))

        @JvmStatic
        public fun xor(condition: BooleanExpression, vararg conditions: BooleanExpression): BooleanExpression = booleanFunction("xor", condition, *conditions)
    }
}

/** An [Expression] evaluating to a boolean, usable as a filter in `where` stages and in conditions. */
public abstract class BooleanExpression : Expression() {
    public fun conditional(thenExpr: Expression, elseExpr: Expression): Expression = Expression.conditional(this, thenExpr, elseExpr)

    public fun conditional(thenValue: Any, elseValue: Any): Expression = Expression.conditional(this, thenValue, elseValue)

    public fun countIf(): AggregateFunction = AggregateFunction.countIf(this)

    public fun not(): BooleanExpression = Expression.not(this)

    public companion object {
        @JvmStatic
        public fun rawFunction(name: String, vararg expr: Expression): BooleanExpression = BooleanFunctionExpression(name, expr.toList())
    }
}

/** An accumulator for `aggregate` stages: a function name and its arguments, as the Android SDK's `AggregateFunction`. */
public class AggregateFunction
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val name: String,
    internal val params: List<Expression>,
) {
    public fun alias(alias: String): AliasedAggregate = AliasedAggregate(alias, this)

    public companion object {
        @JvmStatic
        public fun arrayAgg(expression: Expression): AggregateFunction = aggregate("array_agg", expression)

        @JvmStatic
        public fun arrayAgg(fieldName: String): AggregateFunction = aggregate("array_agg", field(fieldName))

        @JvmStatic
        public fun arrayAggDistinct(expression: Expression): AggregateFunction = aggregate("array_agg_distinct", expression)

        @JvmStatic
        public fun arrayAggDistinct(fieldName: String): AggregateFunction = aggregate("array_agg_distinct", field(fieldName))

        @JvmStatic
        public fun average(expression: Expression): AggregateFunction = aggregate("average", expression)

        @JvmStatic
        public fun average(fieldName: String): AggregateFunction = aggregate("average", field(fieldName))

        @JvmStatic
        public fun count(expression: Expression): AggregateFunction = aggregate("count", expression)

        @JvmStatic
        public fun count(fieldName: String): AggregateFunction = aggregate("count", field(fieldName))

        @JvmStatic
        public fun countAll(): AggregateFunction = aggregate("count")

        @JvmStatic
        public fun countDistinct(expression: Expression): AggregateFunction = aggregate("count_distinct", expression)

        @JvmStatic
        public fun countDistinct(fieldName: String): AggregateFunction = aggregate("count_distinct", field(fieldName))

        @JvmStatic
        public fun countIf(condition: BooleanExpression): AggregateFunction = aggregate("count_if", condition)

        @JvmStatic
        public fun first(expression: Expression): AggregateFunction = aggregate("first", expression)

        @JvmStatic
        public fun first(fieldName: String): AggregateFunction = aggregate("first", field(fieldName))

        @JvmStatic
        public fun last(expression: Expression): AggregateFunction = aggregate("last", expression)

        @JvmStatic
        public fun last(fieldName: String): AggregateFunction = aggregate("last", field(fieldName))

        @JvmStatic
        public fun maximum(expression: Expression): AggregateFunction = aggregate("maximum", expression)

        @JvmStatic
        public fun maximum(fieldName: String): AggregateFunction = aggregate("maximum", field(fieldName))

        @JvmStatic
        public fun minimum(expression: Expression): AggregateFunction = aggregate("minimum", expression)

        @JvmStatic
        public fun minimum(fieldName: String): AggregateFunction = aggregate("minimum", field(fieldName))

        @JvmStatic
        public fun rawAggregate(name: String, vararg expr: Expression): AggregateFunction = AggregateFunction(name, expr.toList())

        @JvmStatic
        public fun sum(expression: Expression): AggregateFunction = aggregate("sum", expression)

        @JvmStatic
        public fun sum(fieldName: String): AggregateFunction = aggregate("sum", field(fieldName))
    }
}
