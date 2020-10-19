/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.auth

import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual val Firebase.auth
    get() = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance())

actual fun Firebase.auth(app: FirebaseApp) =
    FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance(app.android))

actual class FirebaseAuth internal constructor(val android: com.google.firebase.auth.FirebaseAuth) {
    actual val currentUser: FirebaseUser?
        get() = android.currentUser?.let { FirebaseUser(it) }

    actual val authStateChanged get() = callbackFlow {
        val listener = AuthStateListener { auth -> if (!isClosedForSend) offer(auth.currentUser?.let { FirebaseUser(it) }) }
        android.addAuthStateListener(listener)
        awaitClose { android.removeAuthStateListener(listener) }
    }

    actual val idTokenChanged: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = com.google.firebase.auth.FirebaseAuth.IdTokenListener { auth -> if (!isClosedForSend) offer(auth.currentUser?.let { FirebaseUser(it) })}
            android.addIdTokenListener(listener)
            awaitClose { android.removeIdTokenListener(listener) }
        }

    actual var languageCode: String
        get() = android.languageCode ?: ""
        set(value) { android.setLanguageCode(value) }

    actual suspend fun applyActionCode(code: String) = android.applyActionCode(code).await().run { Unit }
    actual suspend fun checkActionCode(code: String): ActionCodeResult = ActionCodeResult(android.checkActionCode(code).await())
    actual suspend fun confirmPasswordReset(code: String, newPassword: String) = android.confirmPasswordReset(code, newPassword).await().run { Unit }

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        AuthResult(android.createUserWithEmailAndPassword(email, password).await())

    actual suspend fun fetchSignInMethodsForEmail(email: String): SignInMethodQueryResult = SignInMethodQueryResult(android.fetchSignInMethodsForEmail(email).await())

    actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) {
        android.sendPasswordResetEmail(email, actionCodeSettings?.android).await()
    }

    actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) = android.sendSignInLinkToEmail(email, actionCodeSettings.android).await().run { Unit }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        AuthResult(android.signInWithEmailAndPassword(email, password).await())

    actual suspend fun signInWithCustomToken(token: String) =
        AuthResult(android.signInWithCustomToken(token).await())

    actual suspend fun signInAnonymously() = AuthResult(android.signInAnonymously().await())

    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        AuthResult(android.signInWithCredential(authCredential.android).await())

    actual suspend fun signOut() = android.signOut()

    actual suspend fun updateCurrentUser(user: FirebaseUser) = android.updateCurrentUser(user.android).await().run { Unit }
    actual suspend fun verifyPasswordResetCode(code: String): String = android.verifyPasswordResetCode(code).await()
}

actual class AuthResult internal constructor(val android: com.google.firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = android.user?.let { FirebaseUser(it) }
}

actual class ActionCodeResult(val android: com.google.firebase.auth.ActionCodeResult) {
    actual val operation: Operation
        get() = when (android.operation) {
            com.google.firebase.auth.ActionCodeResult.PASSWORD_RESET -> Operation.PasswordReset(this)
            com.google.firebase.auth.ActionCodeResult.VERIFY_EMAIL -> Operation.VerifyEmail(this)
            com.google.firebase.auth.ActionCodeResult.RECOVER_EMAIL -> Operation.RecoverEmail(this)
            com.google.firebase.auth.ActionCodeResult.ERROR -> Operation.Error
            com.google.firebase.auth.ActionCodeResult.SIGN_IN_WITH_EMAIL_LINK -> Operation.SignInWithEmailLink
            com.google.firebase.auth.ActionCodeResult.VERIFY_BEFORE_CHANGE_EMAIL -> Operation.VerifyBeforeChangeEmail(this)
            com.google.firebase.auth.ActionCodeResult.REVERT_SECOND_FACTOR_ADDITION -> Operation.RevertSecondFactorAddition(this)
            else -> Operation.Error
        }
}

internal actual sealed class ActionCodeDataType<out T> {

    actual abstract fun dataForResult(result: ActionCodeResult): T

    actual object Email : ActionCodeDataType<String>() {
        override fun dataForResult(result: ActionCodeResult): String = result.android.info!!.email
    }
    actual object PreviousEmail : ActionCodeDataType<String>() {
        override fun dataForResult(result: ActionCodeResult): String = (result.android.info as ActionCodeEmailInfo).previousEmail
    }
    actual object MultiFactor : ActionCodeDataType<MultiFactorInfo?>() {
        override fun dataForResult(result: ActionCodeResult): MultiFactorInfo? = (result.android.info as? ActionCodeMultiFactorInfo)?.multiFactorInfo?.let { MultiFactorInfo(it) }
    }
}

actual class SignInMethodQueryResult(val android: com.google.firebase.auth.SignInMethodQueryResult) {
    actual val signInMethods: List<String>
        get() = android.signInMethods ?: emptyList()
}

actual class ActionCodeSettings private constructor(val android: com.google.firebase.auth.ActionCodeSettings) {

    actual constructor(url: String,
                       androidPackageName: AndroidPackageName?,
                       dynamicLinkDomain: String?,
                       canHandleCodeInApp: Boolean,
                       iOSBundleId: String?
    ) : this(com.google.firebase.auth.ActionCodeSettings.newBuilder().apply {
        this.url = url
        androidPackageName?.let {
            this.setAndroidPackageName(it.androidPackageName, it.installIfNotAvailable, it.minimumVersion)
        }
        this.dynamicLinkDomain = dynamicLinkDomain
        this.handleCodeInApp = canHandleCodeInApp
        this.iosBundleId = iosBundleId
    }.build())

    actual val canHandleCodeInApp: Boolean
        get() = android.canHandleCodeInApp()
    actual val androidPackageName: AndroidPackageName?
        get() = android.androidPackageName?.let {
            AndroidPackageName(it, android.androidInstallApp, android.androidMinimumVersion)
        }
    actual val iOSBundle: String?
        get() = android.iosBundle
    actual val url: String
        get() = android.url
}

actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
actual typealias FirebaseAuthMultiFactorException = com.google.firebase.auth.FirebaseAuthMultiFactorException
actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException
