/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * The static members of the Firebase Android SDK that this module's own code uses. Compiled by javac after the Kotlin
 * header stubs are stripped, so it binds to the real classes (Kotlin code compiled against the stubs would call their
 * Companion objects instead, which the real classes do not have).
 */
final class FirestoreStatics {
    private FirestoreStatics() {}

    static void setLoggingEnabled(boolean loggingEnabled) { FirebaseFirestore.setLoggingEnabled(loggingEnabled); }
    static Timestamp timestampNow() { return Timestamp.now(); }

    static FieldPath fieldPathOf(String... fieldNames) { return FieldPath.of(fieldNames); }
    static FieldPath documentId() { return FieldPath.documentId(); }

    static FieldValue serverTimestamp() { return FieldValue.serverTimestamp(); }
    static FieldValue delete() { return FieldValue.delete(); }
    static FieldValue increment(long value) { return FieldValue.increment(value); }
    static FieldValue increment(double value) { return FieldValue.increment(value); }
    static FieldValue arrayUnion(Object... elements) { return FieldValue.arrayUnion(elements); }
    static FieldValue arrayRemove(Object... elements) { return FieldValue.arrayRemove(elements); }

    static SetOptions merge() { return SetOptions.merge(); }
    static SetOptions mergeFields(List<String> fields) { return SetOptions.mergeFields(fields); }
    static SetOptions mergeFieldPaths(List<FieldPath> fields) { return SetOptions.mergeFieldPaths(fields); }

    static AggregateField count() { return AggregateField.count(); }
    static AggregateField sum(String field) { return AggregateField.sum(field); }
    static AggregateField sum(FieldPath fieldPath) { return AggregateField.sum(fieldPath); }
    static AggregateField average(String field) { return AggregateField.average(field); }
    static AggregateField average(FieldPath fieldPath) { return AggregateField.average(fieldPath); }

    static Filter and(Filter[] filters) { return Filter.and(filters); }
    static Filter or(Filter[] filters) { return Filter.or(filters); }
    static Filter equalTo(String field, Object value) { return Filter.equalTo(field, value); }
    static Filter equalTo(FieldPath fieldPath, Object value) { return Filter.equalTo(fieldPath, value); }
    static Filter notEqualTo(String field, Object value) { return Filter.notEqualTo(field, value); }
    static Filter notEqualTo(FieldPath fieldPath, Object value) { return Filter.notEqualTo(fieldPath, value); }
    static Filter lessThan(String field, Object value) { return Filter.lessThan(field, value); }
    static Filter lessThan(FieldPath fieldPath, Object value) { return Filter.lessThan(fieldPath, value); }
    static Filter lessThanOrEqualTo(String field, Object value) { return Filter.lessThanOrEqualTo(field, value); }
    static Filter lessThanOrEqualTo(FieldPath fieldPath, Object value) { return Filter.lessThanOrEqualTo(fieldPath, value); }
    static Filter greaterThan(String field, Object value) { return Filter.greaterThan(field, value); }
    static Filter greaterThan(FieldPath fieldPath, Object value) { return Filter.greaterThan(fieldPath, value); }
    static Filter greaterThanOrEqualTo(String field, Object value) { return Filter.greaterThanOrEqualTo(field, value); }
    static Filter greaterThanOrEqualTo(FieldPath fieldPath, Object value) { return Filter.greaterThanOrEqualTo(fieldPath, value); }
    static Filter arrayContains(String field, Object value) { return Filter.arrayContains(field, value); }
    static Filter arrayContains(FieldPath fieldPath, Object value) { return Filter.arrayContains(fieldPath, value); }
    static Filter arrayContainsAny(String field, List<?> values) { return Filter.arrayContainsAny(field, values); }
    static Filter arrayContainsAny(FieldPath fieldPath, List<?> values) { return Filter.arrayContainsAny(fieldPath, values); }
    static Filter inArray(String field, List<?> values) { return Filter.inArray(field, values); }
    static Filter inArray(FieldPath fieldPath, List<?> values) { return Filter.inArray(fieldPath, values); }
    static Filter notInArray(String field, List<?> values) { return Filter.notInArray(field, values); }
    static Filter notInArray(FieldPath fieldPath, List<?> values) { return Filter.notInArray(fieldPath, values); }
}
