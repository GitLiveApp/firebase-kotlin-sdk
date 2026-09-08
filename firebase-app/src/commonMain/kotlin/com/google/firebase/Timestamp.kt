/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * A point in time with nanosecond precision, mirroring `com.google.firebase.Timestamp` from the Firebase Android SDK.
 *
 * Not mirrored, because they use Android/JVM-only types: the `Date` and `Instant` constructors, `toDate`, `toInstant`
 * and the `Parcelable` members.
 *
 * @param seconds seconds since the Unix epoch, between `-62135596800` and `253402300799` inclusive
 * @param nanoseconds fraction of a second in nanoseconds, between `0` and `999999999` inclusive
 */
public expect class Timestamp(seconds: Long, nanoseconds: Int) : Comparable<Timestamp> {
    /** Seconds since the Unix epoch. */
    public val seconds: Long

    /** Fraction of a second in nanoseconds. */
    public val nanoseconds: Int

    override fun compareTo(other: Timestamp): Int

    public companion object {
        /** The current time. */
        public fun now(): Timestamp
    }
}
