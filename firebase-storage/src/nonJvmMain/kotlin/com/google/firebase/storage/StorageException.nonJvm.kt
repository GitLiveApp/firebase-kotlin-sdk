/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.firebase.FirebaseException

private const val NO_HTTP_RESULT = -1
private const val HTTP_UNAUTHENTICATED = 401
private const val HTTP_UNAUTHORIZED = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private val HTTP_SUCCESS = 200..299

public actual open class StorageException internal constructor(
    message: String,
    cause: Throwable?,
    public actual val errorCode: Int,
    public actual val httpResultCode: Int,
) : FirebaseException(message, cause) {

    public actual val isRecoverableException: Boolean get() = errorCode == ERROR_RETRY_LIMIT_EXCEEDED

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class ErrorCode

    public actual companion object {
        public actual val ERROR_BUCKET_NOT_FOUND: Int = -13011
        public actual val ERROR_CANCELED: Int = -13040
        public actual val ERROR_INVALID_CHECKSUM: Int = -13031
        public actual val ERROR_NOT_AUTHENTICATED: Int = -13020
        public actual val ERROR_NOT_AUTHORIZED: Int = -13021
        public actual val ERROR_OBJECT_NOT_FOUND: Int = -13010
        public actual val ERROR_PROJECT_NOT_FOUND: Int = -13012
        public actual val ERROR_QUOTA_EXCEEDED: Int = -13013
        public actual val ERROR_RETRY_LIMIT_EXCEEDED: Int = -13030
        public actual val ERROR_UNKNOWN: Int = -13000

        public actual fun fromException(exception: Throwable): StorageException = exception as? StorageException
            ?: StorageException(exception.message ?: messageOf(ERROR_UNKNOWN), exception, ERROR_UNKNOWN, NO_HTTP_RESULT)

        public actual fun fromExceptionAndHttpCode(exception: Throwable?, httpResultCode: Int): StorageException? = when {
            exception is StorageException -> exception
            exception == null && httpResultCode in HTTP_SUCCESS -> null
            else -> {
                val code = when (httpResultCode) {
                    HTTP_UNAUTHENTICATED -> ERROR_NOT_AUTHENTICATED
                    HTTP_UNAUTHORIZED -> ERROR_NOT_AUTHORIZED
                    HTTP_NOT_FOUND -> ERROR_OBJECT_NOT_FOUND
                    HTTP_TOO_MANY_REQUESTS -> ERROR_QUOTA_EXCEEDED
                    else -> ERROR_UNKNOWN
                }
                StorageException(exception?.message ?: messageOf(code), exception, code, httpResultCode)
            }
        }

        /** A [StorageException] for [errorCode], with the Android SDK's message for it. */
        internal fun of(errorCode: Int, cause: Throwable? = null, httpResultCode: Int = NO_HTTP_RESULT, message: String? = null): StorageException = StorageException(message ?: messageOf(errorCode), cause, errorCode, httpResultCode)

        internal fun canceled(): StorageException = of(ERROR_CANCELED)

        private fun messageOf(errorCode: Int): String = when (errorCode) {
            ERROR_OBJECT_NOT_FOUND -> "Object does not exist at location."
            ERROR_BUCKET_NOT_FOUND -> "Bucket does not exist."
            ERROR_PROJECT_NOT_FOUND -> "Project does not exist."
            ERROR_QUOTA_EXCEEDED -> "Quota for bucket exceeded, please view quota on www.firebase.google.com/storage."
            ERROR_NOT_AUTHENTICATED -> "User is not authenticated, please authenticate using Firebase Authentication and try again."
            ERROR_NOT_AUTHORIZED -> "User does not have permission to access this object."
            ERROR_RETRY_LIMIT_EXCEEDED -> "The operation retry limit has been exceeded."
            ERROR_INVALID_CHECKSUM -> "Object has a checksum which does not match. Please retry the operation."
            ERROR_CANCELED -> "The operation was cancelled."
            else -> "An unknown error occurred, please check the HTTP result code and inner exception for server response."
        }
    }
}
