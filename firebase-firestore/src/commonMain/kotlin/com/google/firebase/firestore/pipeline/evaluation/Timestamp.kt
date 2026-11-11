/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("TimestampKt")
@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.firestore.pipeline.evaluation

import dev.gitlive.firebase.firestore.PIPELINE_EVALUATION_ANDROID_ONLY
import kotlin.jvm.JvmName

/*
 * Android/JVM only: the public helpers of the Android SDK's local pipeline evaluation (`TimestampKt`), declared so that common
 * code referring to them fails with a message saying so, rather than with an unresolved reference; on Android and the JVM
 * the SDK's are used (see buildSrc utils/HeaderStubs.kt).
 */

@Deprecated(PIPELINE_EVALUATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public fun convertUnit(unit: String): java.time.temporal.ChronoUnit = throw UnsupportedOperationException(PIPELINE_EVALUATION_ANDROID_ONLY)

@Deprecated(PIPELINE_EVALUATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public fun isMicrosecondsInTimestampBounds(microseconds: Long): Boolean = throw UnsupportedOperationException(PIPELINE_EVALUATION_ANDROID_ONLY)

@Deprecated(PIPELINE_EVALUATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public fun isMillisecondsInTimestampBounds(milliseconds: Long): Boolean = throw UnsupportedOperationException(PIPELINE_EVALUATION_ANDROID_ONLY)

@Deprecated(PIPELINE_EVALUATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public fun isSecondsInTimestampBounds(seconds: Long): Boolean = throw UnsupportedOperationException(PIPELINE_EVALUATION_ANDROID_ONLY)

@Deprecated(PIPELINE_EVALUATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public fun isTimestampInBounds(seconds: Long, nanos: Int): Boolean = throw UnsupportedOperationException(PIPELINE_EVALUATION_ANDROID_ONLY)
