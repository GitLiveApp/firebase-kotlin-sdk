/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import dev.gitlive.firebase.functions.STREAM_JVM_ONLY

/**
 * Android/JVM only: an element of the stream returned by the Android SDK's `HttpsCallableReference.stream()`, which is not
 * mirrored (see `api/android-sdk/exclusions.txt`). Declared so that common code referring to it fails with a message saying
 * so, rather than with an unresolved reference; on Android and the JVM the real class is used.
 */
@Deprecated(STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
public abstract class StreamResponse private constructor() {
    /** A message streamed before the result. */
    @Suppress("DEPRECATION_ERROR")
    public class Message
    @Deprecated(STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
    constructor(
        @Deprecated(STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
        public val message: HttpsCallableResult,
    ) : StreamResponse()

    /** The final result of the call. */
    @Suppress("DEPRECATION_ERROR")
    public class Result
    @Deprecated(STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
    constructor(
        @Deprecated(STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
        public val result: HttpsCallableResult,
    ) : StreamResponse()
}
