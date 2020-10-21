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
}

fun <T, A: ActionCodeDataType<T>> ActionCodeResult.getData(type: A): T? {
    return type.dataForResult(this)
}

expect class SignInMethodQueryResult {
    val signInMethods: List<String>
}

sealed class Operation {
    class PasswordReset(result: ActionCodeResult) : Operation() {
        val email: String = ActionCodeDataType.Email.dataForResult(result)
    }
    class VerifyEmail(result: ActionCodeResult) : Operation() {
        val email: String = ActionCodeDataType.Email.dataForResult(result)
    }
    class RecoverEmail(result: ActionCodeResult) : Operation() {
        val email: String = ActionCodeDataType.Email.dataForResult(result)
        val previousEmail: String = ActionCodeDataType.PreviousEmail.dataForResult(result)
    }
    object Error : Operation()
    object SignInWithEmailLink : Operation()
    class VerifyBeforeChangeEmail(result: ActionCodeResult) : Operation() {
        val email: String = ActionCodeDataType.Email.dataForResult(result)
        val previousEmail: String = ActionCodeDataType.PreviousEmail.dataForResult(result)
    }
    class RevertSecondFactorAddition(result: ActionCodeResult) : Operation() {
        val email: String = ActionCodeDataType.Email.dataForResult(result)
        val multiFactorInfo: MultiFactorInfo? = ActionCodeDataType.MultiFactor.dataForResult(result)
    }
}

internal expect sealed class ActionCodeDataType<out T> {

    abstract fun dataForResult(result: ActionCodeResult): T

    object Email : ActionCodeDataType<String>
    object PreviousEmail : ActionCodeDataType<String>
    object MultiFactor : ActionCodeDataType<MultiFactorInfo?>
}

expect class ActionCodeSettings {
    constructor(
        url: String,
        androidPackageName: AndroidPackageName? = null,
        dynamicLinkDomain: String? = null,
        canHandleCodeInApp: Boolean = false,
        iOSBundleId: String? = null
    )

    val canHandleCodeInApp: Boolean
    val androidPackageName: AndroidPackageName?
    val iOSBundle: String?
    val url: String
}

data class AndroidPackageName(val androidPackageName: String, val installIfNotAvailable: Boolean, val minimumVersion: String?)

expect open class FirebaseAuthException : FirebaseException
expect class FirebaseAuthActionCodeException : FirebaseAuthException
expect class FirebaseAuthEmailException : FirebaseAuthException
expect class FirebaseAuthInvalidCredentialsException : FirebaseAuthException
expect class FirebaseAuthInvalidUserException : FirebaseAuthException
expect class FirebaseAuthMultiFactorException: FirebaseAuthException
expect class FirebaseAuthRecentLoginRequiredException : FirebaseAuthException
expect class FirebaseAuthUserCollisionException : FirebaseAuthException
expect class FirebaseAuthWebException : FirebaseAuthException
