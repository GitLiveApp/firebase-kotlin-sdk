/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp

/*
 * The static members of the layer that this module's own (shipped) code uses. Common code calling the companion members
 * would bind to a Companion object that the real Android SDK classes do not have, so these go through a Java helper on
 * Android and the JVM (FirestoreInternals.android.kt) and call the companions directly elsewhere.
 */

internal expect fun setFirestoreLoggingEnabled(loggingEnabled: Boolean)

internal expect fun timestampNow(): Timestamp

internal expect fun fieldPathOf(vararg fieldNames: String): FieldPath

internal expect fun fieldPathDocumentId(): FieldPath

internal expect fun fieldValueServerTimestamp(): FieldValue

internal expect fun fieldValueDelete(): FieldValue

internal expect fun fieldValueIncrement(value: Long): FieldValue

internal expect fun fieldValueIncrement(value: Double): FieldValue

internal expect fun fieldValueArrayUnion(vararg elements: Any): FieldValue

internal expect fun fieldValueArrayRemove(vararg elements: Any): FieldValue

internal expect fun setOptionsMerge(): SetOptions

internal expect fun setOptionsMergeFields(fields: List<String>): SetOptions

internal expect fun setOptionsMergeFieldPaths(fields: List<FieldPath>): SetOptions

internal expect fun aggregateCount(): AggregateField

internal expect fun aggregateSum(field: String): AggregateField

internal expect fun aggregateSum(fieldPath: FieldPath): AggregateField

internal expect fun aggregateAverage(field: String): AggregateField

internal expect fun aggregateAverage(fieldPath: FieldPath): AggregateField

internal expect fun filterAnd(filters: List<Filter>): Filter

internal expect fun filterOr(filters: List<Filter>): Filter

internal expect fun filterEqualTo(field: String, value: Any?): Filter

internal expect fun filterEqualTo(fieldPath: FieldPath, value: Any?): Filter

internal expect fun filterNotEqualTo(field: String, value: Any?): Filter

internal expect fun filterNotEqualTo(fieldPath: FieldPath, value: Any?): Filter

internal expect fun filterLessThan(field: String, value: Any): Filter

internal expect fun filterLessThan(fieldPath: FieldPath, value: Any): Filter

internal expect fun filterLessThanOrEqualTo(field: String, value: Any): Filter

internal expect fun filterLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter

internal expect fun filterGreaterThan(field: String, value: Any): Filter

internal expect fun filterGreaterThan(fieldPath: FieldPath, value: Any): Filter

internal expect fun filterGreaterThanOrEqualTo(field: String, value: Any): Filter

internal expect fun filterGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Filter

internal expect fun filterArrayContains(field: String, value: Any): Filter

internal expect fun filterArrayContains(fieldPath: FieldPath, value: Any): Filter

internal expect fun filterArrayContainsAny(field: String, values: List<Any>): Filter

internal expect fun filterArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter

internal expect fun filterInArray(field: String, values: List<Any>): Filter

internal expect fun filterInArray(fieldPath: FieldPath, values: List<Any>): Filter

internal expect fun filterNotInArray(field: String, values: List<Any>): Filter

internal expect fun filterNotInArray(fieldPath: FieldPath, values: List<Any>): Filter
