/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.util.concurrent

import dev.gitlive.firebase.TIME_UNIT_JVM_ONLY

/**
 * Stands in for `java.util.concurrent.TimeUnit` so that the Firebase Android SDK members taking one
 * (`HttpsCallableReference.setTimeout(Long, TimeUnit)`) can be declared in common code and deprecated with an error
 * naming their `kotlin.time.Duration` counterparts. A value of this type cannot exist in common code: the class has no
 * members and cannot be constructed. On Android and the JVM the class is a header stub that is stripped from the output
 * (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's enum.
 */
@Deprecated(TIME_UNIT_JVM_ONLY, level = DeprecationLevel.ERROR)
public class TimeUnit private constructor()
