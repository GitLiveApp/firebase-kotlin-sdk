/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow

expect val Firebase.auth: FirebaseAuth

expect fun Firebase.auth(app: FirebaseApp): FirebaseAuth

expect class FirebaseAuth {
    val currentUser: FirebaseUser?
    val authStateChanged: Flow<FirebaseUser?>
    val idTokenChanged: Flow<FirebaseUser?>
    var languageCode: String
    suspend fun applyActionCode(code: String)
    suspend fun checkActionCode(code: String): ActionCodeResult
    suspend fun confirmPasswordReset(code: String, newPassword: String)
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun fetchSignInMethodsForEmail(email: String): SignInMethodQueryResult
    suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings? = null)
    suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings)
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signInWithCustomToken(token: String): AuthResult
    suspend fun signInAnonymously(): AuthResult
    suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult
    suspend fun signOut()
    suspend fun updateCurrentUser(user: FirebaseUser)
    suspend fun verifyPasswordResetCode(code: String): String
}

expect class AuthResult {
    val user: FirebaseUser?
}

expect class ActionCodeResult {
    val operation: Operation
    fun <T, A: ActionCodeDataType<T>> getData(type: A): T?
}

expect class SignInMethodQueryResult {
    val signInMethods: List<String>
}

enum class Operation {
    PasswordReset,
    VerifyEmail,
    RecoverEmail,
    Error,
    SignInWithEmailLink,
    VerifyBeforeChangeEmail,
    RevertSecondFactorAddition
}

sealed class ActionCodeDataType<T> {
    object Email : ActionCodeDataType<String>()
    object PreviousEmail : ActionCodeDataType<String>()
    object MultiFactor : ActionCodeDataType<MultiFactorInfo>()
}

expect class ActionCodeSettings {
    class Builder {
        fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder
        fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder
        fun setHandleCodeInApp(canHandleCodeInApp: Boolean): Builder
        fun setIOSBundleId(iOSBundleId: String): Builder
        fun setUrl(url: String): Builder
        fun build(): ActionCodeSettings
    }

    val canHandleCodeInApp: Boolean
    val androidInstallApp: Boolean
    val androidMinimumVersion: String?
    val androidPackageName: String?
    val iOSBundle: String?
    val url: String
}

expect open class FirebaseAuthException : FirebaseException
expect class FirebaseAuthActionCodeException : FirebaseAuthException
expect class FirebaseAuthEmailException : FirebaseAuthException
expect class FirebaseAuthInvalidCredentialsException : FirebaseAuthException
expect class FirebaseAuthInvalidUserException : FirebaseAuthException
expect class FirebaseAuthMultiFactorException: FirebaseAuthException
expect class FirebaseAuthRecentLoginRequiredException : FirebaseAuthException
expect class FirebaseAuthUserCollisionException : FirebaseAuthException
expect class FirebaseAuthWebException : FirebaseAuthException
