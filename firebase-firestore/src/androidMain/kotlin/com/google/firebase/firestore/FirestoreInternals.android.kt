/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirestoreInternalsKt")

package com.google.firebase.firestore

import com.google.firebase.Timestamp

// Shipped (see keepClasses in the build file): the SDK's static members this module's own code needs are reached through
// FirestoreStatics.java, which javac compiles after the header stubs are stripped, so it binds to the real classes.

internal actual fun setFirestoreLoggingEnabled(loggingEnabled: Boolean) = FirestoreStatics.setLoggingEnabled(loggingEnabled)

internal actual fun timestampNow(): Timestamp = FirestoreStatics.timestampNow()

internal actual fun fieldPathOf(vararg fieldNames: String): FieldPath = FirestoreStatics.fieldPathOf(*fieldNames)

internal actual fun fieldPathDocumentId(): FieldPath = FirestoreStatics.documentId()

internal actual fun fieldValueServerTimestamp(): FieldValue = FirestoreStatics.serverTimestamp()

internal actual fun fieldValueDelete(): FieldValue = FirestoreStatics.delete()

internal actual fun fieldValueIncrement(value: Long): FieldValue = FirestoreStatics.increment(value)

internal actual fun fieldValueIncrement(value: Double): FieldValue = FirestoreStatics.increment(value)

internal actual fun fieldValueArrayUnion(vararg elements: Any): FieldValue = FirestoreStatics.arrayUnion(*elements)

internal actual fun fieldValueArrayRemove(vararg elements: Any): FieldValue = FirestoreStatics.arrayRemove(*elements)

internal actual fun setOptionsMerge(): SetOptions = FirestoreStatics.merge()

internal actual fun setOptionsMergeFields(fields: List<String>): SetOptions = FirestoreStatics.mergeFields(fields)

internal actual fun setOptionsMergeFieldPaths(fields: List<FieldPath>): SetOptions = FirestoreStatics.mergeFieldPaths(fields)

internal actual fun aggregateCount(): AggregateField = FirestoreStatics.count()

internal actual fun aggregateSum(field: String): AggregateField = FirestoreStatics.sum(field)

internal actual fun aggregateSum(fieldPath: FieldPath): AggregateField = FirestoreStatics.sum(fieldPath)

internal actual fun aggregateAverage(field: String): AggregateField = FirestoreStatics.average(field)

internal actual fun aggregateAverage(fieldPath: FieldPath): AggregateField = FirestoreStatics.average(fieldPath)

internal actual fun filterAnd(filters: List<Filter>): Filter = FirestoreStatics.and(filters.toTypedArray())

internal actual fun filterOr(filters: List<Filter>): Filter = FirestoreStatics.or(filters.toTypedArray())

internal actual fun filterEqualTo(field: String, value: Any?): Filter = FirestoreStatics.equalTo(field, value)

internal actual fun filterEqualTo(fieldPath: FieldPath, value: Any?): Filter = FirestoreStatics.equalTo(fieldPath, value)

internal actual fun filterNotEqualTo(field: String, value: Any?): Filter = FirestoreStatics.notEqualTo(field, value)

internal actual fun filterNotEqualTo(fieldPath: FieldPath, value: Any?): Filter = FirestoreStatics.notEqualTo(fieldPath, value)

internal actual fun filterLessThan(field: String, value: Any): Filter = FirestoreStatics.lessThan(field, value)

internal actual fun filterLessThan(fieldPath: FieldPath, value: Any): Filter = FirestoreStatics.lessThan(fieldPath, value)

internal actual fun filterLessThanOrEqualTo(field: String, value: Any): Filter = FirestoreStatics.lessThanOrEqualTo(field, value)

internal actual fun filterLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter = FirestoreStatics.lessThanOrEqualTo(fieldPath, value)

internal actual fun filterGreaterThan(field: String, value: Any): Filter = FirestoreStatics.greaterThan(field, value)

internal actual fun filterGreaterThan(fieldPath: FieldPath, value: Any): Filter = FirestoreStatics.greaterThan(fieldPath, value)

internal actual fun filterGreaterThanOrEqualTo(field: String, value: Any): Filter = FirestoreStatics.greaterThanOrEqualTo(field, value)

internal actual fun filterGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter = FirestoreStatics.greaterThanOrEqualTo(fieldPath, value)

internal actual fun filterArrayContains(field: String, value: Any): Filter = FirestoreStatics.arrayContains(field, value)

internal actual fun filterArrayContains(fieldPath: FieldPath, value: Any): Filter = FirestoreStatics.arrayContains(fieldPath, value)

internal actual fun filterArrayContainsAny(field: String, values: List<Any>): Filter = FirestoreStatics.arrayContainsAny(field, values)

internal actual fun filterArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter = FirestoreStatics.arrayContainsAny(fieldPath, values)

internal actual fun filterInArray(field: String, values: List<Any>): Filter = FirestoreStatics.inArray(field, values)

internal actual fun filterInArray(fieldPath: FieldPath, values: List<Any>): Filter = FirestoreStatics.inArray(fieldPath, values)

internal actual fun filterNotInArray(field: String, values: List<Any>): Filter = FirestoreStatics.notInArray(field, values)

internal actual fun filterNotInArray(fieldPath: FieldPath, values: List<Any>): Filter = FirestoreStatics.notInArray(fieldPath, values)
