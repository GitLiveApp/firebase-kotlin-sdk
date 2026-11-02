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
        /** Sets the custom key [key] to the string [value]; returns this builder. */
        public fun putString(key: String, value: String): Builder

        /** Sets the custom key [key] to the boolean [value]; returns this builder. */
        public fun putBoolean(key: String, value: Boolean): Builder

        /** Sets the custom key [key] to the double [value]; returns this builder. */
        public fun putDouble(key: String, value: Double): Builder

        /** Sets the custom key [key] to the float [value]; returns this builder. */
        public fun putFloat(key: String, value: Float): Builder

        /** Sets the custom key [key] to the long [value]; returns this builder. */
        public fun putLong(key: String, value: Long): Builder

        /** Sets the custom key [key] to the int [value]; returns this builder. */
        public fun putInt(key: String, value: Int): Builder

        /** Returns the keys and values put so far, as a [CustomKeysAndValues]. */
        public fun build(): CustomKeysAndValues
    }
}
