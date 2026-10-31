/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

/** A config value, as the Android SDK's `FirebaseRemoteConfigValue`; the `as*` conversions follow the Android SDK. */
public expect interface FirebaseRemoteConfigValue {
    /** The value as a boolean (`true`, `1`, `yes`, `y` and `on` are true); throws [IllegalArgumentException] otherwise. */
    public fun asBoolean(): Boolean

    /** The value as UTF-8 bytes. */
    public fun asByteArray(): ByteArray

    /** The value as a double; throws [IllegalArgumentException] if it is not a number. */
    public fun asDouble(): Double

    /** The value as a long; throws [IllegalArgumentException] if it is not a number. */
    public fun asLong(): Long

    /** The value as a string. */
    public fun asString(): String

    /** Where the value came from: one of the `FirebaseRemoteConfig.VALUE_SOURCE_*` constants. */
    public val source: Int
}
