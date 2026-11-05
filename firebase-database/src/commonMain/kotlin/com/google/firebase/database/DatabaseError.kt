/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/** A failed operation or cancelled listener, as the Android SDK's `DatabaseError`; [code] is one of the constants. */
public expect class DatabaseError {
    /** One of the error code constants. */
    public val code: Int

    /** A human readable description of the error. */
    public val message: String

    /** More details of the error, or an empty string. */
    public val details: String

    /** The error as an exception, to throw or to fail a coroutine with. */
    public fun toException(): DatabaseException

    public companion object {
        public val DATA_STALE: Int
        public val OPERATION_FAILED: Int
        public val PERMISSION_DENIED: Int
        public val DISCONNECTED: Int
        public val EXPIRED_TOKEN: Int
        public val INVALID_TOKEN: Int
        public val MAX_RETRIES: Int
        public val OVERRIDDEN_BY_SET: Int
        public val UNAVAILABLE: Int
        public val USER_CODE_EXCEPTION: Int
        public val NETWORK_ERROR: Int
        public val WRITE_CANCELED: Int
        public val UNKNOWN_ERROR: Int

        /** A [USER_CODE_EXCEPTION] error carrying [e]'s stack trace in [details]. */
        public fun fromException(e: Throwable): DatabaseError
    }
}

/** Thrown when an operation on the database fails, as the Android SDK's `DatabaseException`. */
public expect open class DatabaseException : RuntimeException {
    public constructor(message: String)
    public constructor(message: String, cause: Throwable)
}
