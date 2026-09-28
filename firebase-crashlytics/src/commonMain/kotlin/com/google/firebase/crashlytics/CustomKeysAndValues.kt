/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

/**
 * A set of custom keys and values to attach to reports, built with [Builder]; mirrors
 * `com.google.firebase.crashlytics.CustomKeysAndValues` from the Firebase Android SDK.
 */
public expect class CustomKeysAndValues {
    /** Builds a [CustomKeysAndValues]; putting a key again replaces its value. */
    @Suppress("ktlint:standard:class-signature") // the parentheses declare the expect constructor
    public class Builder() {
        public fun putString(key: String, value: String): Builder
        public fun putBoolean(key: String, value: Boolean): Builder
        public fun putDouble(key: String, value: Double): Builder
        public fun putFloat(key: String, value: Float): Builder
        public fun putLong(key: String, value: Long): Builder
        public fun putInt(key: String, value: Int): Builder
        public fun build(): CustomKeysAndValues
    }
}
