/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException

public actual open class FirebaseAuthException : FirebaseException {
    public actual val errorCode: String

    public actual constructor(errorCode: String, message: String) : super(message) {
        this.errorCode = errorCode
    }

    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(message, cause) {
        this.errorCode = errorCode
    }
}

public actual class FirebaseAuthActionCodeException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

public actual open class FirebaseAuthEmailException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

public actual open class FirebaseAuthInvalidCredentialsException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

public actual class FirebaseAuthInvalidUserException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

public actual open class FirebaseAuthMissingActivityForRecaptchaException actual constructor() : FirebaseAuthException("ERROR_MISSING_ACTIVITY", "An Activity is required for phone verification")

public actual open class FirebaseAuthMultiFactorException : FirebaseAuthException {
    public actual val resolver: MultiFactorResolver

    public actual constructor(errorCode: String, message: String, resolver: MultiFactorResolver) : super(errorCode, message) {
        this.resolver = resolver
    }

    internal constructor(errorCode: String, message: String, resolver: MultiFactorResolver, cause: Throwable?) : super(errorCode, message, cause) {
        this.resolver = resolver
    }
}

public actual class FirebaseAuthRecentLoginRequiredException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

public actual class FirebaseAuthUserCollisionException : FirebaseAuthException {
    public actual val email: String?
    public actual val updatedCredential: AuthCredential?

    public actual constructor(errorCode: String, message: String) : super(errorCode, message) {
        email = null
        updatedCredential = null
    }

    internal constructor(errorCode: String, message: String, email: String?, updatedCredential: AuthCredential?, cause: Throwable?) : super(errorCode, message, cause) {
        this.email = email
        this.updatedCredential = updatedCredential
    }
}

public actual class FirebaseAuthWeakPasswordException : FirebaseAuthInvalidCredentialsException {
    public actual val reason: String?

    public actual constructor(errorCode: String, message: String, reason: String?) : super(errorCode, message) {
        this.reason = reason
    }

    internal constructor(errorCode: String, message: String, reason: String?, cause: Throwable?) : super(errorCode, message, cause) {
        this.reason = reason
    }
}

public actual open class FirebaseAuthWebException : FirebaseAuthException {
    public actual constructor(errorCode: String, message: String) : super(errorCode, message)
    internal constructor(errorCode: String, message: String, cause: Throwable?) : super(errorCode, message, cause)
}

/** What the platform SDK reported about a failure, beyond its code and message. */
internal class AuthErrorDetails(
    val cause: Throwable? = null,
    val resolver: MultiFactorResolver? = null,
    val email: String? = null,
    val updatedCredential: AuthCredential? = null,
    val reason: String? = null,
)

/** The Android SDK's names of the codes the iOS and JS SDKs name differently. */
private val errorCodeAliases = mapOf(
    "ERROR_MULTI_FACTOR_AUTH_REQUIRED" to "ERROR_SECOND_FACTOR_REQUIRED",
    "ERROR_SECOND_FACTOR_ALREADY_IN_USE" to "ERROR_SECOND_FACTOR_ALREADY_ENROLLED",
)

/**
 * The exception the Android SDK throws for [errorCode] (an `ERROR_*` code, as the platform SDKs also name their errors):
 * the subclass the code belongs to, a [FirebaseNetworkException] or [FirebaseTooManyRequestsException] for the
 * failures that are not authentication errors, or a plain [FirebaseAuthException].
 */
internal fun authException(errorCode: String, message: String, details: AuthErrorDetails = AuthErrorDetails()): FirebaseException {
    val code = errorCodeAliases[errorCode] ?: errorCode
    return when (code) {
        "ERROR_NETWORK_REQUEST_FAILED", "ERROR_TIMEOUT" -> FirebaseNetworkException(message)
        "ERROR_TOO_MANY_REQUESTS", "ERROR_QUOTA_EXCEEDED" -> FirebaseTooManyRequestsException(message)
        "ERROR_WEAK_PASSWORD" -> FirebaseAuthWeakPasswordException(code, message, details.reason, details.cause)
        "ERROR_INVALID_ACTION_CODE", "ERROR_EXPIRED_ACTION_CODE" -> FirebaseAuthActionCodeException(code, message, details.cause)
        "ERROR_INVALID_RECIPIENT_EMAIL", "ERROR_INVALID_SENDER", "ERROR_INVALID_MESSAGE_PAYLOAD" -> FirebaseAuthEmailException(code, message, details.cause)
        "ERROR_USER_NOT_FOUND", "ERROR_USER_DISABLED", "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "ERROR_USER_MISMATCH", "ERROR_NULL_USER" ->
            FirebaseAuthInvalidUserException(code, message, details.cause)
        "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
            FirebaseAuthUserCollisionException(code, message, details.email, details.updatedCredential, details.cause)
        "ERROR_REQUIRES_RECENT_LOGIN" -> FirebaseAuthRecentLoginRequiredException(code, message, details.cause)
        "ERROR_SECOND_FACTOR_REQUIRED" -> details.resolver?.let { FirebaseAuthMultiFactorException(code, message, it, details.cause) } ?: FirebaseAuthException(code, message, details.cause)
        "ERROR_WEB_CONTEXT_ALREADY_PRESENTED", "ERROR_WEB_CONTEXT_CANCELED", "ERROR_WEB_CONTEXT_CANCELLED", "ERROR_WEB_INTERNAL_ERROR",
        "ERROR_WEB_STORAGE_UNSUPPORTED", "ERROR_WEB_NETWORK_REQUEST_FAILED", "ERROR_POPUP_BLOCKED", "ERROR_POPUP_CLOSED_BY_USER", "ERROR_CANCELLED_POPUP_REQUEST",
        -> FirebaseAuthWebException(code, message, details.cause)
        "ERROR_INVALID_CUSTOM_TOKEN", "ERROR_CUSTOM_TOKEN_MISMATCH", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD",
        "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_VERIFICATION_CODE", "ERROR_INVALID_VERIFICATION_ID", "ERROR_MISSING_VERIFICATION_CODE",
        "ERROR_MISSING_VERIFICATION_ID", "ERROR_INVALID_PHONE_NUMBER", "ERROR_MISSING_PHONE_NUMBER", "ERROR_SESSION_EXPIRED", "ERROR_CAPTCHA_CHECK_FAILED",
        "ERROR_INVALID_CERT_HASH", "ERROR_MISSING_CLIENT_IDENTIFIER", "ERROR_INVALID_TENANT_ID", "ERROR_INVALID_PROVIDER_ID", "ERROR_INVALID_APP_CREDENTIAL",
        "ERROR_MISSING_APP_CREDENTIAL", "ERROR_INVALID_MULTI_FACTOR_SESSION", "ERROR_MISSING_MULTI_FACTOR_SESSION", "ERROR_MISSING_MULTI_FACTOR_INFO",
        "ERROR_INVALID_MULTI_FACTOR_INFO",
        -> FirebaseAuthInvalidCredentialsException(code, message, details.cause)
        else -> FirebaseAuthException(code, message, details.cause)
    }
}
