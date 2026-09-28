/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

/*
 * Messages of the `@Deprecated(level = ERROR)` members of the `com.google.firebase.functions` layer: Android SDK members
 * whose signature involves a JVM-only type. They are declared so that common code using them fails to compile with a
 * message naming the multiplatform replacement, while Android code keeps binding to the real SDK member.
 */

internal const val SET_TIMEOUT_JVM_ONLY =
    "setTimeout(Long, TimeUnit) takes a java.util.concurrent.TimeUnit and can only be called from Android/JVM code; " +
        "use setTimeout(timeout) with a kotlin.time.Duration from common code"

internal const val WITH_TIMEOUT_JVM_ONLY =
    "withTimeout(Long, TimeUnit) takes a java.util.concurrent.TimeUnit and can only be called from Android/JVM code; " +
        "use withTimeout(timeout) with a kotlin.time.Duration from common code"
