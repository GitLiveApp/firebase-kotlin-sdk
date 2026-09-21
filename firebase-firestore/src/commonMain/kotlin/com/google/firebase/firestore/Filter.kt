/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/**
 * A query condition, mirroring `com.google.firebase.firestore.Filter` from the Firebase Android SDK: field conditions
 * combined with [and] and [or], applied with `Query.where`.
 */
public expect class Filter() {
    public companion object {
        public fun equalTo(field: String, value: Any?): Filter
        public fun equalTo(fieldPath: FieldPath, value: Any?): Filter
        public fun notEqualTo(field: String, value: Any?): Filter
        public fun notEqualTo(fieldPath: FieldPath, value: Any?): Filter
        public fun greaterThan(field: String, value: Any?): Filter
        public fun greaterThan(fieldPath: FieldPath, value: Any?): Filter
        public fun greaterThanOrEqualTo(field: String, value: Any?): Filter
        public fun greaterThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter
        public fun lessThan(field: String, value: Any?): Filter
        public fun lessThan(fieldPath: FieldPath, value: Any?): Filter
        public fun lessThanOrEqualTo(field: String, value: Any?): Filter
        public fun lessThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter
        public fun arrayContains(field: String, value: Any?): Filter
        public fun arrayContains(fieldPath: FieldPath, value: Any?): Filter
        public fun arrayContainsAny(field: String, values: List<Any>): Filter
        public fun arrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter
        public fun inArray(field: String, values: List<Any>): Filter
        public fun inArray(fieldPath: FieldPath, values: List<Any>): Filter
        public fun notInArray(field: String, values: List<Any>): Filter
        public fun notInArray(fieldPath: FieldPath, values: List<Any>): Filter

        /** A condition matching when every one of [filters] matches. */
        public fun and(vararg filters: Filter): Filter

        /** A condition matching when any one of [filters] matches. */
        public fun or(vararg filters: Filter): Filter
    }
}
