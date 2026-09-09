/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("TimestampInstantKt")

package com.google.firebase

import kotlin.jvm.JvmName
import kotlin.time.Instant

/*
 * Multiplatform counterparts of the JVM-only `Timestamp(Instant)` constructor and `Timestamp.toInstant()`, using
 * `kotlin.time.Instant`. Plain common code, shipped on Android too (see keepClasses in the build file): the Android SDK's
 * Timestamp is a Kotlin class with a real Companion, so the companion extension binds to it.
 */

/** Creates a [Timestamp] for the given [instant], the multiplatform form of the JVM-only `Timestamp(Instant)` constructor. */
public fun Timestamp.Companion.fromInstant(instant: Instant): Timestamp = Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)

/** This timestamp as a [kotlin.time.Instant], the multiplatform form of the JVM-only `toInstant()` and `toDate()`. */
public fun Timestamp.toKotlinInstant(): Instant = Instant.fromEpochSeconds(seconds, nanoseconds)
