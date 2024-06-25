/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.ActionCodeResult.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import platform.Foundation.NSError
import platform.Foundation.NSURL

public actual val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth(FIRAuth.auth())

public actual fun Firebase.auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth(
    FIRAuth.authWithApp(app.ios as objcnames.classes.FIRApp),
)

public actual class FirebaseAuth internal constructor(public val ios: FIRAuth) {

    public actual val currentUser: FirebaseUser?
        get() = ios.currentUser?.let { FirebaseUser(it) }

    public actual val authStateChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val handle = ios.addAuthStateDidChangeListener { _, user -> trySend(user?.let { FirebaseUser(it) }) }
        awaitClose { ios.removeAuthStateDidChangeListener(handle) }
    }

    public actual val idTokenChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val handle = ios.addIDTokenDidChangeListener { _, user -> trySend(user?.let { FirebaseUser(it) }) }
        awaitClose { ios.removeIDTokenDidChangeListener(handle) }
    }

    public actual var languageCode: String
        get() = ios.languageCode ?: ""
        set(value) {
            ios.setLanguageCode(value)
        }

    public actual suspend fun applyActionCode(code: String): Unit = ios.await { applyActionCode(code, it) }
    public actual suspend fun confirmPasswordReset(code: String, newPassword: String): Unit = ios.await { confirmPasswordResetWithCode(code, newPassword, it) }

    public actual suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult =
        AuthResult(ios.awaitResult { createUserWithEmail(email = email, password = password, completion = it) })

    @Suppress("UNCHECKED_CAST")
    public actual suspend fun fetchSignInMethodsForEmail(email: String): List<String> =
        ios.awaitResult<FIRAuth, List<*>?> { fetchSignInMethodsForEmail(email, it) }.orEmpty() as List<String>

    public actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) {
        ios.await { actionCodeSettings?.let { actionSettings -> sendPasswordResetWithEmail(email, actionSettings.toIos(), it) } ?: sendPasswordResetWithEmail(email = email, completion = it) }
    }

    public actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Unit = ios.await { sendSignInLinkToEmail(email, actionCodeSettings.toIos(), it) }

    public actual fun isSignInWithEmailLink(link: String): Boolean = ios.isSignInWithEmailLink(link)

    public actual suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult =
        AuthResult(ios.awaitResult { signInWithEmail(email = email, password = password, completion = it) })

    public actual suspend fun signInWithCustomToken(token: String): AuthResult =
        AuthResult(ios.awaitResult { signInWithCustomToken(token, it) })

    public actual suspend fun signInAnonymously(): AuthResult =
        AuthResult(ios.awaitResult { signInAnonymouslyWithCompletion(it) })

    public actual suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult =
        AuthResult(ios.awaitResult { signInWithCredential(authCredential.ios, it) })

    public actual suspend fun signInWithEmailLink(email: String, link: String): AuthResult =
        AuthResult(ios.awaitResult { signInWithEmail(email = email, link = link, completion = it) })

    public actual suspend fun signOut(): Unit = ios.throwError { signOut(it) }

    public actual suspend fun updateCurrentUser(user: FirebaseUser): Unit = ios.await { updateCurrentUser(user.ios, it) }
    public actual suspend fun verifyPasswordResetCode(code: String): String = ios.awaitResult { verifyPasswordResetCode(code, it) }

    public actual suspend fun <T : ActionCodeResult> checkActionCode(code: String): T {
        val result: FIRActionCodeInfo = ios.awaitResult { checkActionCode(code, it) }
        @Suppress("UNCHECKED_CAST")
        return when (result.operation) {
            FIRActionCodeOperationEmailLink -> SignInWithEmailLink
            FIRActionCodeOperationVerifyEmail -> VerifyEmail(result.email!!)
            FIRActionCodeOperationPasswordReset -> PasswordReset(result.email!!)
            FIRActionCodeOperationRecoverEmail -> RecoverEmail(result.email!!, result.previousEmail!!)
            FIRActionCodeOperationVerifyAndChangeEmail -> VerifyBeforeChangeEmail(result.email!!, result.previousEmail!!)
            FIRActionCodeOperationRevertSecondFactorAddition -> RevertSecondFactorAddition(result.email!!, null)
            FIRActionCodeOperationUnknown -> throw UnsupportedOperationException(result.operation.toString())
            else -> throw UnsupportedOperationException(result.operation.toString())
        } as T
    }

    public actual fun useEmulator(host: String, port: Int): Unit = ios.useEmulatorWithHost(host, port.toLong())
}

public actual class AuthResult internal constructor(public val ios: FIRAuthDataResult) {
    public actual val user: FirebaseUser?
        get() = FirebaseUser(ios.user)
}

public actual class AuthTokenResult(public val ios: FIRAuthTokenResult) {
//    actual val authTimestamp: Long
//        get() = ios.authDate
    public actual val claims: Map<String, Any>
        get() = ios.claims.map { it.key.toString() to it.value as Any }.toMap()

//    actual val expirationTimestamp: Long
//        get() = ios.expirationDate
//    actual val issuedAtTimestamp: Long
//        get() = ios.issuedAtDate
    public actual val signInProvider: String?
        get() = ios.signInProvider
    public actual val token: String?
        get() = ios.token
}

internal fun ActionCodeSettings.toIos() = FIRActionCodeSettings().also {
    it.URL = NSURL.URLWithString(url)
    androidPackageName?.run { it.setAndroidPackageName(packageName, installIfNotAvailable, minimumVersion) }
    it.dynamicLinkDomain = dynamicLinkDomain
    it.handleCodeInApp = canHandleCodeInApp
    iOSBundleId?.run { it.setIOSBundleID(this) }
}

public actual open class FirebaseAuthException(message: String) : FirebaseException(message)
public actual open class FirebaseAuthActionCodeException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthEmailException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthInvalidCredentialsException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthWeakPasswordException(message: String) : FirebaseAuthInvalidCredentialsException(message)
public actual open class FirebaseAuthInvalidUserException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthMultiFactorException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthRecentLoginRequiredException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthUserCollisionException(message: String) : FirebaseAuthException(message)
public actual open class FirebaseAuthWebException(message: String) : FirebaseAuthException(message)

internal fun <T, R> T.throwError(block: T.(errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
    memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val result = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value
        if (error != null) {
            throw error.toException()
        }
        return result
    }
}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as R
}

internal suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
}

private fun NSError.toException() = when (domain) {
    FIRAuthErrorDomain -> when (code) {
        FIRAuthErrorCodeInvalidActionCode,
        FIRAuthErrorCodeExpiredActionCode,
        -> FirebaseAuthActionCodeException(toString())

        FIRAuthErrorCodeInvalidEmail -> FirebaseAuthEmailException(toString())

        FIRAuthErrorCodeCaptchaCheckFailed,
        FIRAuthErrorCodeInvalidPhoneNumber,
        FIRAuthErrorCodeMissingPhoneNumber,
        FIRAuthErrorCodeInvalidVerificationID,
        FIRAuthErrorCodeInvalidVerificationCode,
        FIRAuthErrorCodeMissingVerificationID,
        FIRAuthErrorCodeMissingVerificationCode,
        FIRAuthErrorCodeUserTokenExpired,
        FIRAuthErrorCodeInvalidCredential,
        -> FirebaseAuthInvalidCredentialsException(toString())

        FIRAuthErrorCodeWeakPassword -> FirebaseAuthWeakPasswordException(toString())

        FIRAuthErrorCodeInvalidUserToken -> FirebaseAuthInvalidUserException(toString())

        FIRAuthErrorCodeRequiresRecentLogin -> FirebaseAuthRecentLoginRequiredException(toString())

        FIRAuthErrorCodeSecondFactorAlreadyEnrolled,
        FIRAuthErrorCodeSecondFactorRequired,
        FIRAuthErrorCodeMaximumSecondFactorCountExceeded,
        FIRAuthErrorCodeMultiFactorInfoNotFound,
        -> FirebaseAuthMultiFactorException(toString())

        FIRAuthErrorCodeEmailAlreadyInUse,
        FIRAuthErrorCodeAccountExistsWithDifferentCredential,
        FIRAuthErrorCodeCredentialAlreadyInUse,
        -> FirebaseAuthUserCollisionException(toString())

        FIRAuthErrorCodeWebContextAlreadyPresented,
        FIRAuthErrorCodeWebContextCancelled,
        FIRAuthErrorCodeWebInternalError,
        -> FirebaseAuthWebException(toString())

        FIRAuthErrorCodeNetworkError -> FirebaseNetworkException(toString())

        else -> FirebaseAuthException(toString())
    }
    else -> FirebaseAuthException(toString())
}
