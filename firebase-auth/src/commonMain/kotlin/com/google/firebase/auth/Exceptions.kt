/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.firebase.FirebaseException

/**
 * An authentication failure, mirroring `com.google.firebase.auth.FirebaseAuthException`; the subclasses group the
 * causes, and [errorCode] names the exact one with the Android SDK's `ERROR_*` codes on every platform.
 */
public expect open class FirebaseAuthException(errorCode: String, message: String) : FirebaseException {
    /** The error code, e.g. `ERROR_INVALID_EMAIL`. */
    public val errorCode: String
}

/** The out-of-band code of an email action is invalid or expired. */
public expect class FirebaseAuthActionCodeException(errorCode: String, message: String) : FirebaseAuthException

/** An email could not be sent. */
public expect open class FirebaseAuthEmailException(errorCode: String, message: String) : FirebaseAuthException

/** The credential is malformed, expired or wrong (including a wrong password or invalid email). */
public expect open class FirebaseAuthInvalidCredentialsException(errorCode: String, message: String) : FirebaseAuthException

/** The user account is disabled, deleted or its token is no longer valid. */
public expect class FirebaseAuthInvalidUserException(errorCode: String, message: String) : FirebaseAuthException

/** Phone verification needs an `Activity` on Android. */
public expect open class FirebaseAuthMissingActivityForRecaptchaException() : FirebaseAuthException

/** The sign-in requires a second factor; complete it with [resolver]. */
public expect open class FirebaseAuthMultiFactorException(errorCode: String, message: String, resolver: MultiFactorResolver) : FirebaseAuthException {
    public val resolver: MultiFactorResolver
}

/** The operation requires a recent sign-in; [FirebaseUser.reauthenticate] first. */
public expect class FirebaseAuthRecentLoginRequiredException(errorCode: String, message: String) : FirebaseAuthException

/** The email or credential is already used by another account. */
public expect class FirebaseAuthUserCollisionException(errorCode: String, message: String) : FirebaseAuthException {
    /** The email of the existing account, when known. */
    public val email: String?

    /** The credential that can sign in to the existing account, when the provider returned one. */
    public val updatedCredential: AuthCredential?
}

/** The password does not meet the project's policy; [reason] says why. */
public expect class FirebaseAuthWeakPasswordException(errorCode: String, message: String, reason: String?) : FirebaseAuthInvalidCredentialsException {
    public val reason: String?
}

/** A web sign-in flow failed or was cancelled. */
public expect open class FirebaseAuthWebException(errorCode: String, message: String) : FirebaseAuthException
