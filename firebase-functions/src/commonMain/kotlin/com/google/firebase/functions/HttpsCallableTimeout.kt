/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("HttpsCallableTimeoutKt")

package com.google.firebase.functions

import kotlin.jvm.JvmName
import kotlin.time.Duration

/*
 * Multiplatform counterparts of `HttpsCallableReference.setTimeout(long, TimeUnit)` and `withTimeout(long, TimeUnit)`
 * taking a kotlin.time.Duration, since `java.util.concurrent.TimeUnit` is JVM-only. Shipped on Android and the JVM too
 * (see keepClasses in the build file).
 */

/** Sets the timeout of the calls made through this reference. */
public expect fun HttpsCallableReference.setTimeout(timeout: Duration)

/** A copy of this reference whose calls use the given [timeout]. */
public expect fun HttpsCallableReference.withTimeout(timeout: Duration): HttpsCallableReference
