/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp

internal actual fun setFirestoreLoggingEnabled(loggingEnabled: Boolean) = FirebaseFirestore.setLoggingEnabled(loggingEnabled)

internal actual fun timestampNow(): Timestamp = Timestamp.now()

internal actual fun fieldPathOf(vararg fieldNames: String): FieldPath = FieldPath.of(*fieldNames)

internal actual fun fieldPathDocumentId(): FieldPath = FieldPath.documentId()

internal actual fun fieldValueServerTimestamp(): FieldValue = FieldValue.serverTimestamp()

internal actual fun fieldValueDelete(): FieldValue = FieldValue.delete()

internal actual fun fieldValueIncrement(value: Long): FieldValue = FieldValue.increment(value)

internal actual fun fieldValueIncrement(value: Double): FieldValue = FieldValue.increment(value)

internal actual fun fieldValueArrayUnion(vararg elements: Any): FieldValue = FieldValue.arrayUnion(*elements)

internal actual fun fieldValueArrayRemove(vararg elements: Any): FieldValue = FieldValue.arrayRemove(*elements)

internal actual fun setOptionsMerge(): SetOptions = SetOptions.merge()

internal actual fun setOptionsMergeFields(fields: List<String>): SetOptions = SetOptions.mergeFields(fields)

internal actual fun setOptionsMergeFieldPaths(fields: List<FieldPath>): SetOptions = SetOptions.mergeFieldPaths(fields)

internal actual fun aggregateCount(): AggregateField = AggregateField.count()

internal actual fun aggregateSum(field: String): AggregateField = AggregateField.sum(field)

internal actual fun aggregateSum(fieldPath: FieldPath): AggregateField = AggregateField.sum(fieldPath)

internal actual fun aggregateAverage(field: String): AggregateField = AggregateField.average(field)

internal actual fun aggregateAverage(fieldPath: FieldPath): AggregateField = AggregateField.average(fieldPath)

internal actual fun filterAnd(filters: List<Filter>): Filter = Filter.and(*filters.toTypedArray())

internal actual fun filterOr(filters: List<Filter>): Filter = Filter.or(*filters.toTypedArray())

internal actual fun filterEqualTo(field: String, value: Any?): Filter = Filter.equalTo(field, value)

internal actual fun filterEqualTo(fieldPath: FieldPath, value: Any?): Filter = Filter.equalTo(fieldPath, value)

internal actual fun filterNotEqualTo(field: String, value: Any?): Filter = Filter.notEqualTo(field, value)

internal actual fun filterNotEqualTo(fieldPath: FieldPath, value: Any?): Filter = Filter.notEqualTo(fieldPath, value)

internal actual fun filterLessThan(field: String, value: Any): Filter = Filter.lessThan(field, value)

internal actual fun filterLessThan(fieldPath: FieldPath, value: Any): Filter = Filter.lessThan(fieldPath, value)

internal actual fun filterLessThanOrEqualTo(field: String, value: Any): Filter = Filter.lessThanOrEqualTo(field, value)

internal actual fun filterLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter = Filter.lessThanOrEqualTo(fieldPath, value)

internal actual fun filterGreaterThan(field: String, value: Any): Filter = Filter.greaterThan(field, value)

internal actual fun filterGreaterThan(fieldPath: FieldPath, value: Any): Filter = Filter.greaterThan(fieldPath, value)

internal actual fun filterGreaterThanOrEqualTo(field: String, value: Any): Filter = Filter.greaterThanOrEqualTo(field, value)

internal actual fun filterGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter = Filter.greaterThanOrEqualTo(fieldPath, value)

internal actual fun filterArrayContains(field: String, value: Any): Filter = Filter.arrayContains(field, value)

internal actual fun filterArrayContains(fieldPath: FieldPath, value: Any): Filter = Filter.arrayContains(fieldPath, value)

internal actual fun filterArrayContainsAny(field: String, values: List<Any>): Filter = Filter.arrayContainsAny(field, values)

internal actual fun filterArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter = Filter.arrayContainsAny(fieldPath, values)

internal actual fun filterInArray(field: String, values: List<Any>): Filter = Filter.inArray(field, values)

internal actual fun filterInArray(fieldPath: FieldPath, values: List<Any>): Filter = Filter.inArray(fieldPath, values)

internal actual fun filterNotInArray(field: String, values: List<Any>): Filter = Filter.notInArray(field, values)

internal actual fun filterNotInArray(fieldPath: FieldPath, values: List<Any>): Filter = Filter.notInArray(fieldPath, values)
