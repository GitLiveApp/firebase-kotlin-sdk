/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

/*
 * Messages of the `@Deprecated(level = ERROR)` members of the `com.google.firebase` layer: Android SDK members whose
 * signature involves an Android/JVM-only type. They are declared so that common code using them fails to compile with
 * a message naming the multiplatform replacement, while Android code keeps binding to the real SDK member.
 */

internal const val INITIALIZE_APP_ANDROID_ONLY =
    "initializeApp takes an android.content.Context and can only be called from Android code; " +
        "use Firebase.initialize(context) from common code (the context is ignored on the other platforms)"

internal const val GET_APPS_ANDROID_ONLY =
    "getApps takes an android.content.Context and can only be called from Android code; " +
        "use Firebase.getApps(context) from common code (the context is ignored on the other platforms)"

internal const val APPLICATION_CONTEXT_ANDROID_ONLY =
    "getApplicationContext returns an android.content.Context and can only be called from Android code"

internal const val FROM_RESOURCE_ANDROID_ONLY =
    "fromResource takes an android.content.Context and can only be called from Android code; " +
        "use Firebase.fromResource(context) from common code, which reads the platform's default configuration (the context is ignored on the other platforms)"

internal const val TIMESTAMP_OF_DATE_ANDROID_ONLY =
    "the java.util.Date and java.time.Instant constructors are JVM-only; use Timestamp(seconds, nanoseconds)"

internal const val TO_DATE_ANDROID_ONLY =
    "toDate returns a java.util.Date, which is JVM-only; use seconds and nanoseconds"

internal const val TO_INSTANT_ANDROID_ONLY =
    "toInstant returns a java.time.Instant, which is JVM-only; use seconds and nanoseconds"
