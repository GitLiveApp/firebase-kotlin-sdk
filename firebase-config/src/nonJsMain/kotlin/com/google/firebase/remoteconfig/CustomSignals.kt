/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

/**
 * Custom signals: key-value pairs the backend can target conditions on, as the Android SDK's `CustomSignals`. The JS
 * SDK has no custom signals, so this class exists on Android, the JVM and Apple platforms.
 */
public expect class CustomSignals {
    public class Builder {
        public constructor()

        /** Sets the signal [key] to a string [value], or removes it when [value] is null. */
        public fun put(key: String, value: String?): Builder

        /** Sets the signal [key] to a long [value]. */
        public fun put(key: String, value: Long): Builder

        /** Sets the signal [key] to a double [value]. */
        public fun put(key: String, value: Double): Builder

        public fun build(): CustomSignals
    }
}
