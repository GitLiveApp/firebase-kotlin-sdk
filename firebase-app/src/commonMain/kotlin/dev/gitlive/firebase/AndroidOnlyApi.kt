/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

/*
 * Messages of the `@Deprecated(level = ERROR)` members of the `com.google.firebase` layer: Android SDK members whose
 * signature involves a JVM-only type that common code cannot hold. They are declared so that common code using them
 * fails to compile with a message naming the multiplatform replacement, while Android code keeps binding to the real SDK
 * member.
 */

internal const val TIMESTAMP_OF_DATE_ANDROID_ONLY =
    "the java.util.Date constructor is JVM-only; use Timestamp(instant) with a kotlin.time.Instant, or Timestamp(seconds, nanoseconds)"

internal const val TIMESTAMP_OF_INSTANT_ANDROID_ONLY =
    "the java.time.Instant constructor is JVM-only; use Timestamp(instant) with a kotlin.time.Instant, or Timestamp(seconds, nanoseconds)"

internal const val TO_DATE_ANDROID_ONLY =
    "toDate returns a java.util.Date, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds"

internal const val TO_INSTANT_ANDROID_ONLY =
    "toInstant returns a java.time.Instant, which is JVM-only; use toKotlinInstant(), or seconds and nanoseconds"

/*
 * Messages of the error-deprecated stand-in classes for JVM-only types (firebase-app/src/commonMain/kotlin/java/...): they
 * exist so that the SDK members taking them can be declared with their exact signatures, and cannot be used.
 */

internal const val CLASS_JVM_ONLY =
    "java.lang.Class is JVM-only: the members taking one map a class by reflection, which only exists on Android and the JVM; " +
        "the reified reads in com.google.firebase.serialization (toObject<T>(), toObjects<T>(), getField<T>(), getValue<T>()) map a " +
        "@Serializable class with kotlinx.serialization on every platform"
internal const val GET_RESULT_CLASS_JVM_ONLY =
    "getResult(Class) is JVM-only; use result, which throws the task's failure as a RuntimeExecutionException, or exception"
internal const val DATE_JVM_ONLY =
    "java.util.Date is JVM-only and only stands in for the Firebase Android SDK members taking one; use kotlin.time.Instant in common code"

internal const val JAVA_INSTANT_JVM_ONLY =
    "java.time.Instant is JVM-only and only stands in for the Firebase Android SDK members taking one; use kotlin.time.Instant in common code"

internal const val TIME_UNIT_JVM_ONLY =
    "java.util.concurrent.TimeUnit is JVM-only and only stands in for the Firebase Android SDK members taking one; use kotlin.time.Duration in common code"

internal const val FILE_JVM_ONLY =
    "java.io.File is JVM-only and only stands in for the Firebase Android SDK members taking one; common code passes a kotlinx-io Path to the counterpart"

internal const val INPUT_STREAM_JVM_ONLY =
    "java.io.InputStream is JVM-only and only stands in for the Firebase Android SDK members taking one; common code passes a kotlinx-io RawSource to the counterpart"

internal const val BYTE_BUFFER_JVM_ONLY =
    "java.nio.ByteBuffer is JVM-only and only stands in for the Firebase Android SDK members taking one; pass a ByteArray in common code"
