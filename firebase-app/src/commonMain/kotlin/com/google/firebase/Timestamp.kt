/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.TIMESTAMP_OF_DATE_ANDROID_ONLY
import dev.gitlive.firebase.TO_DATE_ANDROID_ONLY
import dev.gitlive.firebase.TO_INSTANT_ANDROID_ONLY

/**
 * A point in time with nanosecond precision, mirroring `com.google.firebase.Timestamp` from the Firebase Android SDK.
 *
 * The `Date` and `Instant` constructors, `toDate` and `toInstant` use JVM-only types and can only be called from
 * Android/JVM code: from common code they fail to compile with a message naming the `kotlin.time.Instant` counterparts,
 * the top-level `Timestamp(Instant)` function and [toKotlinInstant]. The `Parcelable` members are not mirrored.
 *
 * @param seconds seconds since the Unix epoch, between `-62135596800` and `253402300799` inclusive
 * @param nanoseconds fraction of a second in nanoseconds, between `0` and `999999999` inclusive
 */
public expect class Timestamp(seconds: Long, nanoseconds: Int) : Comparable<Timestamp> {
    /** Android/JVM only: the `Timestamp(Date)` and `Timestamp(Instant)` constructors; use `Timestamp(kotlin.time.Instant)` from common code. */
    @Deprecated(TIMESTAMP_OF_DATE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public constructor(time: Any)

    /** Seconds since the Unix epoch. */
    public val seconds: Long

    /** Fraction of a second in nanoseconds. */
    public val nanoseconds: Int

    override fun compareTo(other: Timestamp): Int

    /** Android/JVM only: this timestamp as a `java.util.Date`; use [toKotlinInstant] from common code. */
    @Deprecated(TO_DATE_ANDROID_ONLY, ReplaceWith("toKotlinInstant()", "com.google.firebase.toKotlinInstant"), DeprecationLevel.ERROR)
    public fun toDate(): Any

    /** Android/JVM only: this timestamp as a `java.time.Instant`; use [toKotlinInstant] from common code. */
    @Deprecated(TO_INSTANT_ANDROID_ONLY, ReplaceWith("toKotlinInstant()", "com.google.firebase.toKotlinInstant"), DeprecationLevel.ERROR)
    public fun toInstant(): Any

    public companion object {
        /** The current time. */
        public fun now(): Timestamp
    }
}
