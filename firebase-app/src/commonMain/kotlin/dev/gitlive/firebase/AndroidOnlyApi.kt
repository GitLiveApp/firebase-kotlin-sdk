/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

/*
 * Messages of the `@Deprecated(level = ERROR)` members of the `com.google.firebase` layer: Android SDK members whose
 * signature involves a JVM-only type with no stand-in. They are declared so that common code using them fails to compile
 * with a message naming the multiplatform replacement, while Android code keeps binding to the real SDK member.
 */

internal const val TIMESTAMP_OF_DATE_ANDROID_ONLY =
    "the java.util.Date and java.time.Instant constructors are JVM-only; " +
        "use Timestamp(instant) with a kotlin.time.Instant, or Timestamp(seconds, nanoseconds)"

internal const val TO_DATE_ANDROID_ONLY =
    "toDate returns a java.util.Date, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds"

internal const val TO_INSTANT_ANDROID_ONLY =
    "toInstant returns a java.time.Instant, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds"
