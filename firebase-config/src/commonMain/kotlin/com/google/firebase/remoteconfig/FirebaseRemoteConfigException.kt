/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.firebase.FirebaseException

/** An error of Firebase Remote Config, as the Android SDK's `FirebaseRemoteConfigException`; [code] classifies real-time update errors. */
public expect open class FirebaseRemoteConfigException : FirebaseException {
    public constructor(message: String)
    public constructor(message: String, code: Code)
    public constructor(message: String, cause: Throwable?)
    public constructor(message: String, cause: Throwable?, code: Code)

    /** The kind of error, [Code.UNKNOWN] unless the error came from real-time config updates. */
    public val code: Code

    /** The kinds of error, as the Android SDK's `FirebaseRemoteConfigException.Code`. */
    public enum class Code {
        /** An unknown error. */
        UNKNOWN,

        /** The real-time stream to the backend could not be established or was lost. */
        CONFIG_UPDATE_STREAM_ERROR,

        /** A real-time update message could not be parsed. */
        CONFIG_UPDATE_MESSAGE_INVALID,

        /** The latest config could not be fetched after a real-time update. */
        CONFIG_UPDATE_NOT_FETCHED,

        /** The real-time update service is unavailable. */
        CONFIG_UPDATE_UNAVAILABLE,
        ;

        /** The numeric value of this code. */
        public fun value(): Int
    }
}

/** A Remote Config error caused by the client (a network problem, an invalid response, a parse error). */
public expect open class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException {
    public constructor(message: String)
    public constructor(message: String, code: Code)
    public constructor(message: String, cause: Throwable?)
    public constructor(message: String, cause: Throwable?, code: Code)
}

/** A Remote Config error reported by the backend; [httpStatusCode] is the HTTP status, or -1 if there was none. */
public expect open class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException {
    public constructor(httpStatusCode: Int, message: String)
    public constructor(httpStatusCode: Int, message: String, code: Code)
    public constructor(httpStatusCode: Int, message: String, cause: Throwable?)
    public constructor(httpStatusCode: Int, message: String, cause: Throwable?, code: Code)
    public constructor(message: String, code: Code)
    public constructor(message: String, cause: Throwable?, code: Code)

    /** The HTTP status code of the failed request, or -1 if the error did not come from an HTTP response. */
    public val httpStatusCode: Int
}

/** A fetch was throttled by the backend; [throttleEndTimeMillis] is when fetching becomes possible again. */
public expect open class FirebaseRemoteConfigFetchThrottledException(throttleEndTimeMillis: Long) : FirebaseRemoteConfigException {
    /** When the throttling ends, in milliseconds since the epoch. */
    public val throttleEndTimeMillis: Long
}
