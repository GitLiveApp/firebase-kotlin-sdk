/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.TIMESTAMP_OF_DATE_ANDROID_ONLY
import dev.gitlive.firebase.TO_DATE_ANDROID_ONLY
import dev.gitlive.firebase.TO_INSTANT_ANDROID_ONLY

private const val MIN_SECONDS = -62_135_596_800L // 0001-01-01T00:00:00Z
private const val MAX_SECONDS = 253_402_300_799L // 9999-12-31T23:59:59Z
private const val NANOS_PER_SECOND = 1_000_000_000
private const val NANOS_PER_MILLISECOND = 1_000_000

public actual class Timestamp actual constructor(
    public actual val seconds: Long,
    public actual val nanoseconds: Int,
) : Comparable<Timestamp> {

    init {
        require(nanoseconds in 0 until NANOS_PER_SECOND) { "Timestamp nanoseconds out of range: $nanoseconds" }
        require(seconds in MIN_SECONDS..MAX_SECONDS) { "Timestamp seconds out of range: $seconds" }
    }

    @Deprecated(TIMESTAMP_OF_DATE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual constructor(time: Any) : this(0, 0) {
        throw UnsupportedOperationException(TIMESTAMP_OF_DATE_ANDROID_ONLY)
    }

    actual override fun compareTo(other: Timestamp): Int = compareValuesBy(this, other, { it.seconds }, { it.nanoseconds })

    @Deprecated(TO_DATE_ANDROID_ONLY, ReplaceWith("toKotlinInstant()", "com.google.firebase.toKotlinInstant"), DeprecationLevel.ERROR)
    public actual fun toDate(): Any = throw UnsupportedOperationException(TO_DATE_ANDROID_ONLY)

    @Deprecated(TO_INSTANT_ANDROID_ONLY, ReplaceWith("toKotlinInstant()", "com.google.firebase.toKotlinInstant"), DeprecationLevel.ERROR)
    public actual fun toInstant(): Any = throw UnsupportedOperationException(TO_INSTANT_ANDROID_ONLY)

    override fun equals(other: Any?): Boolean = other is Timestamp && other.seconds == seconds && other.nanoseconds == nanoseconds

    override fun hashCode(): Int = 31 * seconds.hashCode() + nanoseconds

    override fun toString(): String = "Timestamp(seconds=$seconds, nanoseconds=$nanoseconds)"

    public actual companion object {
        public actual fun now(): Timestamp {
            val millis = currentTimeMillis()
            return Timestamp(millis.floorDiv(1000), millis.mod(1000L).toInt() * NANOS_PER_MILLISECOND)
        }
    }
}

/** Milliseconds since the Unix epoch. */
internal expect fun currentTimeMillis(): Long
