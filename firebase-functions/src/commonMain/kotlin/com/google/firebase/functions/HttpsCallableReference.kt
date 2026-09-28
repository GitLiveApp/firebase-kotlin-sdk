/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.functions.SET_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.WITH_TIMEOUT_JVM_ONLY

/**
 * A reference to a callable HTTPS trigger in Cloud Functions, mirroring
 * `com.google.firebase.functions.HttpsCallableReference` from the Firebase Android SDK. Obtained from
 * [FirebaseFunctions.getHttpsCallable] or [getHttpsCallableFromUrl].
 *
 * The timeout is set with the [setTimeout] and [withTimeout] extensions taking a `kotlin.time.Duration`; the Android
 * SDK's `TimeUnit` overloads are JVM-only and deprecated with an error in common code.
 *
 * Not mirrored: `stream()` (returns an `org.reactivestreams.Publisher`, which is JVM-only, and the iOS SDK's streaming
 * API is Swift-only).
 */
public expect class HttpsCallableReference {
    /**
     * Executes this callable HTTPS trigger without arguments. The request automatically carries the Firebase
     * installation id and, when a user is signed in, their auth token. A failed task carries a [FirebaseFunctionsException].
     */
    public fun call(): Task<HttpsCallableResult>

    /**
     * Executes this callable HTTPS trigger with [data], which is serialized as JSON: `null`, a `String`, a number, a
     * `Boolean`, a `List` or a `Map` with `String` keys of these (on JS also the JS SDK's own objects and arrays). See [call].
     */
    public fun call(data: Any?): Task<HttpsCallableResult>

    /** The timeout of the calls made through this reference, in milliseconds. */
    public val timeout: Long

    /** Sets the timeout of the calls made through this reference; Android/JVM only, common code uses [setTimeout] with a `Duration`. */
    @Deprecated(SET_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public fun setTimeout(timeout: Long, units: Any)

    /** A copy of this reference with the given timeout; Android/JVM only, common code uses [withTimeout] with a `Duration`. */
    @Deprecated(WITH_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public fun withTimeout(timeout: Long, units: Any): HttpsCallableReference
}
