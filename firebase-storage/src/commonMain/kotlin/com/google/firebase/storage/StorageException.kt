/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import dev.gitlive.firebase.storage.FROM_ERROR_STATUS_ANDROID_ONLY
import com.google.firebase.FirebaseException

/** An error of Cloud Storage, as the Android SDK's `StorageException`; [errorCode] is one of the `ERROR_*` constants. */
public expect open class StorageException : FirebaseException {
    /** One of the `ERROR_*` constants. */
    public val errorCode: Int

    /** The HTTP status of the failed request, or -1 if there was none. */
    public val httpResultCode: Int

    /** Whether retrying the operation could succeed. */
    public val isRecoverableException: Boolean

    /** The error codes of [errorCode]; the Android SDK's `@IntDef`. */
    @Retention(AnnotationRetention.SOURCE)
    public annotation class ErrorCode

    public companion object {
        public val ERROR_BUCKET_NOT_FOUND: Int
        public val ERROR_CANCELED: Int
        public val ERROR_INVALID_CHECKSUM: Int
        public val ERROR_NOT_AUTHENTICATED: Int
        public val ERROR_NOT_AUTHORIZED: Int
        public val ERROR_OBJECT_NOT_FOUND: Int
        public val ERROR_PROJECT_NOT_FOUND: Int
        public val ERROR_QUOTA_EXCEEDED: Int
        public val ERROR_RETRY_LIMIT_EXCEEDED: Int
        public val ERROR_UNKNOWN: Int

        /** [exception] as a [StorageException]: itself if it is one, otherwise an [ERROR_UNKNOWN] wrapping it. */
        public fun fromException(exception: Throwable): StorageException

        /** [exception] as a [StorageException] with the error code of [httpResultCode], or null if [exception] is null and the status is a success. */
        public fun fromExceptionAndHttpCode(exception: Throwable?, httpResultCode: Int): StorageException?

        /** Android only: the [StorageException] of a Play Services `Status`. */
        @Suppress("DEPRECATION_ERROR")
        @Deprecated(FROM_ERROR_STATUS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
        public fun fromErrorStatus(status: com.google.android.gms.common.api.Status): StorageException
    }
}
