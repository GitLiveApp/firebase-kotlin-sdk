/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("TimestampInstantKt")

package com.google.firebase

import kotlin.jvm.JvmName
import kotlin.time.Instant

/*
 * Multiplatform counterparts of the JVM-only `Timestamp(Instant)` constructor and `Timestamp.toInstant()`, using
 * `kotlin.time.Instant`. Plain common code, shipped on Android and the JVM too (see keepClasses in the build file). The
 * factory is a top-level function rather than a companion extension because the firebase-java-sdk's Timestamp is a Java
 * class without a companion object.
 */

/** Creates a [Timestamp] for the given [instant], the multiplatform form of the JVM-only `Timestamp(Instant)` constructor. */
public fun Timestamp(instant: Instant): Timestamp = Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)

/** This timestamp as a [kotlin.time.Instant], the multiplatform form of the JVM-only `toInstant()` and `toDate()`. */
public fun Timestamp.toKotlinInstant(): Instant = Instant.fromEpochSeconds(seconds, nanoseconds)
