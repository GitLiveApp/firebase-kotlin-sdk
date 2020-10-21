/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.*
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

actual val Firebase.auth
    get() = rethrow { dev.gitlive.firebase.auth; FirebaseAuth(firebase.auth()) }

actual fun Firebase.auth(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.auth; FirebaseAuth(firebase.auth(app.js)) }

actual class FirebaseAuth internal constructor(val js: firebase.auth.Auth) {

    actual val currentUser: FirebaseUser?
        get() = rethrow { js.currentUser?.let { FirebaseUser(it) } }

    actual val authStateChanged get() = callbackFlow {
        val unsubscribe = js.onAuthStateChanged {
            offer(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    actual val idTokenChanged get() = callbackFlow {
        val unsubscribe = js.onIdTokenChanged {
            offer(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    actual var languageCode: String
        get() = js.languageCode ?: ""
        set(value) { js.languageCode = value }

    actual suspend fun applyActionCode(code: String) = rethrow { js.applyActionCode(code).await() }
    actual suspend fun checkActionCode(code: String): ActionCodeResult = rethrow { ActionCodeResult(js.checkActionCode(code).await()) }
    actual suspend fun confirmPasswordReset(code: String, newPassword: String) = rethrow { js.confirmPasswordReset(code, newPassword).await() }

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        rethrow { AuthResult(js.createUserWithEmailAndPassword(email, password).await()) }

    actual suspend fun fetchSignInMethodsForEmail(email: String): SignInMethodQueryResult = rethrow { SignInMethodQueryResult(js.fetchSignInMethodsForEmail(email).await().asList()) }

    actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) =
        rethrow { js.sendPasswordResetEmail(email, actionCodeSettings?.js).await() }

    actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) =
        rethrow { js.sendSignInLinkToEmail(email, actionCodeSettings.js).await() }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        rethrow { AuthResult(js.signInWithEmailAndPassword(email, password).await()) }

    actual suspend fun signInWithCustomToken(token: String)
            = rethrow { AuthResult(js.signInWithCustomToken(token).await()) }

    actual suspend fun signInAnonymously()
            = rethrow { AuthResult(js.signInAnonymously().await()) }

    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        rethrow { AuthResult(js.signInWithCredential(authCredential.js).await()) }

    actual suspend fun signOut() = rethrow { js.signOut().await() }

    actual suspend fun updateCurrentUser(user: FirebaseUser) =
        rethrow {
            js.updateCurrentUser(user.js).await()
        }
    actual suspend fun verifyPasswordResetCode(code: String): String =
        rethrow {
            js.verifyPasswordResetCode(code).await()
        }

}

actual class AuthResult internal constructor(val js: firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = rethrow { js.user?.let { FirebaseUser(it) } }
}

actual class ActionCodeResult(val js: firebase.auth.ActionCodeInfo) {
    actual val operation: Operation
        get() = when (js.operation) {
            "PASSWORD_RESET" -> Operation.PasswordReset(this)
            "VERIFY_EMAIL" -> Operation.VerifyEmail(this)
            "RECOVER_EMAIL" -> Operation.RecoverEmail(this)
            "EMAIL_SIGNIN" -> Operation.SignInWithEmailLink
            "VERIFY_AND_CHANGE_EMAIL" -> Operation.VerifyBeforeChangeEmail(this)
            "REVERT_SECOND_FACTOR_ADDITION" -> Operation.RevertSecondFactorAddition(this)
            else -> Operation.Error
        }
}

internal actual sealed class ActionCodeDataType<out T> {

    actual abstract fun dataForResult(result: ActionCodeResult): T

    actual object Email : ActionCodeDataType<String>() {
        override fun dataForResult(result: ActionCodeResult): String = result.js.data.email!!
    }
    actual object PreviousEmail : ActionCodeDataType<String>() {
        override fun dataForResult(result: ActionCodeResult): String = result.js.data.previousEmail!!
    }
    actual object MultiFactor : ActionCodeDataType<MultiFactorInfo?>() {
        override fun dataForResult(result: ActionCodeResult): MultiFactorInfo? = result.js.data.multiFactorInfo?.let { MultiFactorInfo(it) }
    }
}

actual class SignInMethodQueryResult(actual val signInMethods: List<String>)

actual class ActionCodeSettings private constructor(val js: firebase.auth.ActionCodeSettings) {

    actual constructor(url: String,
                       androidPackageName: AndroidPackageName?,
                       dynamicLinkDomain: String?,
                       canHandleCodeInApp: Boolean,
                       iOSBundleId: String?
    ) : this(object : firebase.auth.ActionCodeSettings {
        override val android: firebase.auth.AndroidActionCodeSettings? = androidPackageName?.let { androidPackageName -> object : firebase.auth.AndroidActionCodeSettings {
            override val installApp: Boolean get() = androidPackageName.installIfNotAvailable
            override val minimumVersion: String? get() = androidPackageName.minimumVersion
            override val packageName: String get() = androidPackageName.androidPackageName

        } }
        override val dynamicLinkDomain: String? = dynamicLinkDomain
        override val handleCodeInApp: Boolean? = canHandleCodeInApp
        override val iOS: firebase.auth.iOSActionCodeSettings? = iOSBundleId?.let { iOSBundleId ->
            object : firebase.auth.iOSActionCodeSettings {
                override val bundleId: String?
                    get() = iOSBundleId
            }
        }
        override val url: String = url
    })

    actual val canHandleCodeInApp: Boolean
        get() = js.handleCodeInApp ?: false
    actual val androidPackageName: AndroidPackageName?
        get() = js.android?.let { AndroidPackageName(it.packageName, it.installApp ?: false, it.minimumVersion) }
    actual val iOSBundle: String?
        get() = js.iOS?.bundleId
    actual val url: String
        get() = js.url
}

actual open class FirebaseAuthException(code: String?, cause: Throwable): FirebaseException(code, cause)
actual open class FirebaseAuthActionCodeException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthEmailException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthInvalidCredentialsException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthInvalidUserException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthMultiFactorException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthRecentLoginRequiredException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthUserCollisionException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthWebException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)


internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.auth.rethrow { function() }

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw errorToException(e)
    }
}

private fun errorToException(cause: dynamic) = when(val code = cause.code?.toString()?.toLowerCase()) {
    "auth/invalid-user-token" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/requires-recent-login" -> FirebaseAuthRecentLoginRequiredException(code, cause)
    "auth/user-disabled" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/user-token-expired" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/web-storage-unsupported" -> FirebaseAuthWebException(code, cause)
    "auth/network-request-failed" -> FirebaseNetworkException(code, cause)
    "auth/invalid-credential",
    "auth/invalid-verification-code",
    "auth/missing-verification-code",
    "auth/invalid-verification-id",
    "auth/missing-verification-id" -> FirebaseAuthInvalidCredentialsException(code, cause)
    "auth/maximum-second-factor-count-exceeded",
    "auth/second-factor-already-in-use" -> FirebaseAuthMultiFactorException(code, cause)
    "auth/credential-already-in-use" -> FirebaseAuthUserCollisionException(code, cause)
    "auth/invalid-email" -> FirebaseAuthEmailException(code, cause)

//                "auth/app-deleted" ->
//                "auth/app-not-authorized" ->
//                "auth/argument-error" ->
//                "auth/invalid-api-key" ->
//                "auth/operation-not-allowed" ->
//                "auth/too-many-arguments" ->
//                "auth/unauthorized-domain" ->
    else -> {
        println("Unknown error code in ${JSON.stringify(cause)}")
        FirebaseAuthException(code, cause)
    }
}
