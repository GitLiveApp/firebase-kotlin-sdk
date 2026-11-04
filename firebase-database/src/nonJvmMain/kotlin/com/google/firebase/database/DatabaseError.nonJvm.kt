/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

public actual class DatabaseError internal constructor(
    public actual val code: Int,
    public actual val message: String,
    public actual val details: String = "",
) {
    public actual fun toException(): DatabaseException = DatabaseException("Firebase Database error: $message")

    override fun toString(): String = "DatabaseError: $message"

    public actual companion object {
        public actual val DATA_STALE: Int = -1
        public actual val OPERATION_FAILED: Int = -2
        public actual val PERMISSION_DENIED: Int = -3
        public actual val DISCONNECTED: Int = -4
        public actual val EXPIRED_TOKEN: Int = -6
        public actual val INVALID_TOKEN: Int = -7
        public actual val MAX_RETRIES: Int = -8
        public actual val OVERRIDDEN_BY_SET: Int = -9
        public actual val UNAVAILABLE: Int = -10
        public actual val USER_CODE_EXCEPTION: Int = -11
        public actual val NETWORK_ERROR: Int = -24
        public actual val WRITE_CANCELED: Int = -25
        public actual val UNKNOWN_ERROR: Int = -999

        public actual fun fromException(e: Throwable): DatabaseError = DatabaseError(
            USER_CODE_EXCEPTION,
            "User code called from the Firebase Database runloop threw an exception.",
            "User code called from the Firebase Database runloop threw an exception:\n${e.stackTraceToString()}",
        )

        /** The error of the platform SDK's [code] name (`PERMISSION_DENIED`, ...) and [message]. */
        internal fun of(codeName: String?, message: String, details: String = ""): DatabaseError = DatabaseError(
            when (codeName?.uppercase()) {
                "DATA_STALE", "DATASTALE" -> DATA_STALE
                "OPERATION_FAILED" -> OPERATION_FAILED
                "PERMISSION_DENIED" -> PERMISSION_DENIED
                "DISCONNECTED" -> DISCONNECTED
                "EXPIRED_TOKEN" -> EXPIRED_TOKEN
                "INVALID_TOKEN" -> INVALID_TOKEN
                "MAX_RETRIES", "MAXRETRIES" -> MAX_RETRIES
                "OVERRIDDEN_BY_SET" -> OVERRIDDEN_BY_SET
                "UNAVAILABLE" -> UNAVAILABLE
                "USER_CODE_EXCEPTION" -> USER_CODE_EXCEPTION
                "NETWORK_ERROR" -> NETWORK_ERROR
                "WRITE_CANCELED" -> WRITE_CANCELED
                else -> UNKNOWN_ERROR
            },
            message,
            details,
        )
    }
}

public actual open class DatabaseException : RuntimeException {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, cause: Throwable) : super(message, cause)
}
