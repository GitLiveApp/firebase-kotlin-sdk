/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.firebase.FirebaseException
import kotlin.jvm.JvmStatic

/**
 * The exception a call through an [HttpsCallableReference] fails with, mirroring
 * `com.google.firebase.functions.FirebaseFunctionsException` from the Firebase Android SDK. Plain common code: on Android
 * and the JVM it is a header stub for the SDK's own class.
 *
 * @property code The canonical error code of the failure.
 * @property details The `details` the function sent along with the error, deserialized like [HttpsCallableResult.data]; null otherwise.
 */
public class FirebaseFunctionsException : FirebaseException {
    public val code: Code
    public val details: Any?

    internal constructor(message: String, code: Code, details: Any?) : super(message) {
        this.code = code
        this.details = details
    }

    internal constructor(message: String, code: Code, details: Any?, cause: Throwable) : super(message, cause) {
        this.code = code
        this.details = details
    }

    /**
     * The canonical error codes of Google APIs (https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto)
     * a callable HTTPS trigger can fail with. Same order as the Android SDK, whose values are the ordinals.
     */
    public enum class Code {
        OK,
        CANCELLED,
        UNKNOWN,
        INVALID_ARGUMENT,
        DEADLINE_EXCEEDED,
        NOT_FOUND,
        ALREADY_EXISTS,
        PERMISSION_DENIED,
        RESOURCE_EXHAUSTED,
        FAILED_PRECONDITION,
        ABORTED,
        OUT_OF_RANGE,
        UNIMPLEMENTED,
        INTERNAL,
        UNAVAILABLE,
        DATA_LOSS,
        UNAUTHENTICATED,
        ;

        public companion object {
            /** The code with the given canonical [value] (its ordinal); [UNKNOWN] for an unknown value. */
            @JvmStatic
            public fun fromValue(value: Int): Code = entries.getOrNull(value) ?: UNKNOWN

            /** The code an HTTP [status] maps to, as the Cloud Functions protocol defines it; [UNKNOWN] for other statuses. */
            @JvmStatic
            public fun fromHttpStatus(status: Int): Code = when (status) {
                200 -> OK
                400 -> INVALID_ARGUMENT
                401 -> UNAUTHENTICATED
                403 -> PERMISSION_DENIED
                404 -> NOT_FOUND
                409 -> ABORTED
                429 -> RESOURCE_EXHAUSTED
                499 -> CANCELLED
                500 -> INTERNAL
                501 -> UNIMPLEMENTED
                503 -> UNAVAILABLE
                504 -> DEADLINE_EXCEEDED
                else -> UNKNOWN
            }
        }
    }
}
