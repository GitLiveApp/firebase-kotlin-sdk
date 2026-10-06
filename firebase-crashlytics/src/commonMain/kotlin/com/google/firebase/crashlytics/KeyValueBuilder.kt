/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

/**
 * Receiver of the `setCustomKeys { }` and `recordException(throwable) { }` builders, mirroring
 * `com.google.firebase.crashlytics.KeyValueBuilder` from the Firebase Android SDK's Kotlin extensions.
 */
public expect class KeyValueBuilder internal constructor() {
    /** Adds a custom key; see [FirebaseCrashlytics.setCustomKey] for the limits. */
    public fun key(key: String, value: Boolean)

    /** See [key]. */
    public fun key(key: String, value: Double)

    /** See [key]. */
    public fun key(key: String, value: Float)

    /** See [key]. */
    public fun key(key: String, value: Int)

    /** See [key]. */
    public fun key(key: String, value: Long)

    /** See [key]. */
    public fun key(key: String, value: String)

    internal fun build(): CustomKeysAndValues
}
