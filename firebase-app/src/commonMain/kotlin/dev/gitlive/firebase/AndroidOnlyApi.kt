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

internal const val ACTIVITY_ANDROID_ONLY =
    "android.app.Activity is Android-only and only stands in for the Firebase Android SDK members taking one; a call bound to an Activity belongs in androidMain"

internal const val EXECUTOR_JVM_ONLY =
    "java.util.concurrent.Executor is JVM-only and only stands in for the Firebase Android SDK members taking one; the multiplatform listeners are called on the " +
        "thread that completes the operation, so dispatch from the listener, or await() the Task in a coroutine"

internal const val PARCEL_ANDROID_ONLY =
    "android.os.Parcel and Parcelable are Android-only and only stand in for the Firebase Android SDK's Parcelable members (describeContents, writeToParcel, CREATOR); " +
        "parcelling belongs in androidMain"

internal const val JSON_OBJECT_ANDROID_ONLY =
    "org.json.JSONObject is Android/JVM-only and only stands in for the Firebase Android SDK members returning one; use kotlinx.serialization in common code"

internal const val STATUS_ANDROID_ONLY =
    "com.google.android.gms.common.api.Status is Android-only (Play Services) and only stands in for the Firebase Android SDK members taking one"

internal const val CHRONO_UNIT_JVM_ONLY =
    "java.time.temporal.ChronoUnit is JVM-only and only stands in for the Firebase Android SDK members returning one; use kotlin.time.DurationUnit in common code"

internal const val CONTENT_PROVIDER_ANDROID_ONLY =
    "FirebaseInitProvider is the android.content.ContentProvider that initializes the default FirebaseApp from the manifest and only exists on Android; " +
        "common code initializes Firebase with Firebase.initialize(context) or FirebaseApp.initializeApp(context)"

/*
 * Messages of the error-deprecated Android/JVM-only overloads of the Task API, inherited by every Task subclass
 * (LoadBundleTask, StorageTask, ...).
 */
internal const val TASK_LISTENER_EXECUTOR_JVM_ONLY =
    "The Executor overloads of the Task listeners are JVM-only; the multiplatform Task calls its listeners on the thread that completes it: " +
        "use the overload without an Executor and dispatch from the listener, or await() the Task in a coroutine"

internal const val TASK_LISTENER_ACTIVITY_ANDROID_ONLY =
    "The Activity overloads of the Task listeners are Android-only, where the listener is dropped when the Activity stops; " +
        "use the overload without an Activity, or await() the Task in a coroutine scope that is cancelled with the screen"

internal const val TASK_CONTINUATION_EXECUTOR_JVM_ONLY =
    "The Executor overloads of continueWith, continueWithTask and onSuccessTask are JVM-only; the multiplatform Task runs its continuations on the thread " +
        "that completes it: use the overload without an Executor, or await() the Task in a coroutine"

internal const val TIMESTAMP_PARCELABLE_ANDROID_ONLY =
    "describeContents, writeToParcel and CREATOR implement android.os.Parcelable, which is Android-only; a Timestamp maps as a Firestore timestamp through " +
        "kotlinx.serialization on every platform"

internal const val RECAPTCHA_SITE_KEY_DEPRECATED =
    "Deprecated in the Firebase Android SDK, which no longer uses the reCAPTCHA site key; the setter keeps the key and nothing reads it"

internal const val KCLASS_JAVA_JVM_ONLY =
    "KClass.java and javaClass are JVM-only: they produce a java.lang.Class, which only the Firebase Android SDK's reflective members take; " +
        "the reified reads in com.google.firebase.serialization (toObject<T>(), toObjects<T>(), getField<T>(), getValue<T>()) map a @Serializable class " +
        "with kotlinx.serialization on every platform"
