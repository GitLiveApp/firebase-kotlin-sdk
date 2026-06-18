/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.externals.Auth
import dev.gitlive.firebase.auth.externals.getAuth
import dev.gitlive.firebase.auth.externals.applyActionCode
import dev.gitlive.firebase.auth.externals.confirmPasswordReset
import dev.gitlive.firebase.auth.externals.createUserWithEmailAndPassword
import dev.gitlive.firebase.auth.externals.sendPasswordResetEmail
import dev.gitlive.firebase.auth.externals.fetchSignInMethodsForEmail
import dev.gitlive.firebase.auth.externals.sendSignInLinkToEmail
import dev.gitlive.firebase.auth.externals.isSignInWithEmailLink
import dev.gitlive.firebase.auth.externals.signInWithEmailAndPassword
import dev.gitlive.firebase.auth.externals.signInWithCustomToken
import dev.gitlive.firebase.auth.externals.signInAnonymously
import dev.gitlive.firebase.auth.externals.signInWithCredential
import dev.gitlive.firebase.auth.externals.signInWithEmailLink
import dev.gitlive.firebase.auth.externals.signOut
import dev.gitlive.firebase.auth.externals.updateCurrentUser
import dev.gitlive.firebase.auth.externals.verifyPasswordResetCode
import dev.gitlive.firebase.auth.externals.checkActionCode
import dev.gitlive.firebase.auth.externals.connectAuthEmulator
import dev.gitlive.firebase.auth.externals.IdTokenResult
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsObjectEntries
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.stringifyThrownValue
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.js
import dev.gitlive.firebase.externals.awaitUnit
import dev.gitlive.firebase.externals.awaitValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.JsException
import dev.gitlive.firebase.auth.externals.AuthResult as JsAuthResult
import dev.gitlive.firebase.auth.externals.AdditionalUserInfo as JsAdditionalUserInfo

public actual val Firebase.auth: FirebaseAuth
    get() = rethrow { FirebaseAuth(getAuth()) }

public actual fun Firebase.auth(app: FirebaseApp): FirebaseAuth = rethrow { FirebaseAuth(getAuth(app.js)) }

public val FirebaseAuth.js: Auth get() = js

public actual class FirebaseAuth internal constructor(internal val js: Auth) {

    public actual val currentUser: FirebaseUser?
        get() = rethrow { js.currentUser?.let { FirebaseUser(it) } }

    public actual val authStateChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val unsubscribe = js.onAuthStateChanged {
            trySend(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    public actual val idTokenChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val unsubscribe = js.onIdTokenChanged {
            trySend(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    public actual var languageCode: String
        get() = js.languageCode ?: ""
        set(value) {
            js.languageCode = value
        }

    public actual suspend fun applyActionCode(code: String): Unit = rethrow { applyActionCode(js, code).awaitUnit() }
    public actual suspend fun confirmPasswordReset(code: String, newPassword: String): Unit = rethrow { confirmPasswordReset(js, code, newPassword).awaitUnit() }

    public actual suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult = rethrow { AuthResult(createUserWithEmailAndPassword(js, email, password).awaitValue()) }

    public actual suspend fun fetchSignInMethodsForEmail(email: String): List<String> = rethrow { fetchSignInMethodsForEmail(js, email).awaitValue().toList().map { it.toKotlinString() } }

    public actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?): Unit = rethrow { sendPasswordResetEmail(js, email, actionCodeSettings?.toJson()).awaitUnit() }

    public actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Unit = rethrow { sendSignInLinkToEmail(js, email, actionCodeSettings.toJson()).awaitUnit() }

    public actual fun isSignInWithEmailLink(link: String): Boolean = rethrow { isSignInWithEmailLink(js, link) }

    public actual suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult = rethrow { AuthResult(signInWithEmailAndPassword(js, email, password).awaitValue()) }

    public actual suspend fun signInWithCustomToken(token: String): AuthResult = rethrow { AuthResult(signInWithCustomToken(js, token).awaitValue()) }

    public actual suspend fun signInAnonymously(): AuthResult = rethrow { AuthResult(signInAnonymously(js).awaitValue()) }

    public actual suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult = rethrow { AuthResult(signInWithCredential(js, authCredential.js).awaitValue()) }

    public actual suspend fun signInWithEmailLink(email: String, link: String): AuthResult = rethrow { AuthResult(signInWithEmailLink(js, email, link).awaitValue()) }

    public actual suspend fun signOut(): Unit = rethrow { signOut(js).awaitUnit() }

    public actual suspend fun updateCurrentUser(user: FirebaseUser): Unit = rethrow { updateCurrentUser(js, user.js).awaitUnit() }

    public actual suspend fun verifyPasswordResetCode(code: String): String = rethrow { verifyPasswordResetCode(js, code).awaitValue().toKotlinString() }

    public actual suspend fun <T : ActionCodeResult> checkActionCode(code: String): T = rethrow {
        val result = checkActionCode(js, code).awaitValue()
        @Suppress("UNCHECKED_CAST")
        return when (result.operation) {
            "EMAIL_SIGNIN" -> ActionCodeResult.SignInWithEmailLink
            "VERIFY_EMAIL" -> ActionCodeResult.VerifyEmail(result.data.email!!)
            "PASSWORD_RESET" -> ActionCodeResult.PasswordReset(result.data.email!!)
            "RECOVER_EMAIL" -> ActionCodeResult.RecoverEmail(result.data.email!!, result.data.previousEmail!!)
            "VERIFY_AND_CHANGE_EMAIL" -> ActionCodeResult.VerifyBeforeChangeEmail(
                result.data.email!!,
                result.data.previousEmail!!,
            )
            "REVERT_SECOND_FACTOR_ADDITION" -> ActionCodeResult.RevertSecondFactorAddition(
                result.data.email!!,
                result.data.multiFactorInfo?.let { MultiFactorInfo(it) },
            )
            else -> throw UnsupportedOperationException(result.operation)
        } as T
    }

    public actual fun useEmulator(host: String, port: Int): Unit = rethrow { connectAuthEmulator(js, "http://$host:$port") }
}

public val AuthResult.js: JsAuthResult get() = js

public actual class AuthResult(internal val js: JsAuthResult) {
    public actual val user: FirebaseUser?
        get() = rethrow { js.user?.let { FirebaseUser(it) } }
    public actual val credential: AuthCredential?
        get() = rethrow { js.credential?.let { AuthCredential(it) } }
    public actual val additionalUserInfo: AdditionalUserInfo?
        get() = rethrow { js.additionalUserInfo?.let { AdditionalUserInfo(it) } }
}

public val AdditionalUserInfo.js: JsAdditionalUserInfo get() = js

public actual class AdditionalUserInfo(
    internal val js: JsAdditionalUserInfo,
) {
    public actual val providerId: String?
        get() = js.providerId
    public actual val username: String?
        get() = js.username
    public actual val profile: Map<String, Any?>?
        get() = rethrow {
            val profile = js.profile ?: return@rethrow null
            val entries = jsObjectEntries(profile)
            buildMap {
                for (index in 0 until entries.length) {
                    val entry = entries[index]!!
                    put(entry[0].toKotlin() as String, entry[1].toKotlin())
                }
            }
        }
    public actual val isNewUser: Boolean
        get() = js.newUser
}

public val AuthTokenResult.js: IdTokenResult get() = js

public actual class AuthTokenResult(internal val js: IdTokenResult) {
//    actual val authTimestamp: Long
//        get() = js.authTime
    public actual val claims: Map<String, Any>
        get() {
            val claims = js.claims
            val keys = jsObjectKeys(claims)
            return buildMap {
                for (index in 0 until keys.length) {
                    val key = keys[index]!!.toKotlinString()
                    jsGet(claims, key)?.toKotlin()?.let { put(key, it) }
                }
            }
        }

//    actual val expirationTimestamp: Long
//        get() = android.expirationTime
//    actual val issuedAtTimestamp: Long
//        get() = js.issuedAtTime
    public actual val signInProvider: String?
        get() = js.signInProvider
    public actual val token: String?
        get() = js.token
}

internal fun ActionCodeSettings.toJson(): JsAny {
    val json = jsObject()
    jsSet(json, "url", url.toJs())
    androidPackageName?.let { android ->
        val androidJson = jsObject()
        jsSet(androidJson, "installApp", android.installIfNotAvailable.toJs())
        android.minimumVersion?.let { jsSet(androidJson, "minimumVersion", it.toJs()) }
        jsSet(androidJson, "packageName", android.packageName.toJs())
        jsSet(json, "android", androidJson)
    }
    linkDomain?.let { jsSet(json, "linkDomain", it.toJs()) }
    dynamicLinkDomain?.let { jsSet(json, "dynamicLinkDomain", it.toJs()) }
    jsSet(json, "handleCodeInApp", canHandleCodeInApp.toJs())
    iOSBundleId?.let { jsSet(json, "ios", jsObject().also { ios -> jsSet(ios, "bundleId", it.toJs()) }) }
    return json
}

public actual open class FirebaseAuthException(code: String?, cause: Throwable) : FirebaseException(code, cause)
public actual open class FirebaseAuthActionCodeException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthEmailException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthInvalidCredentialsException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthWeakPasswordException(code: String?, cause: Throwable) : FirebaseAuthInvalidCredentialsException(code, cause)
public actual open class FirebaseAuthInvalidUserException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthMultiFactorException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthRecentLoginRequiredException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthUserCollisionException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)
public actual open class FirebaseAuthWebException(code: String?, cause: Throwable) : FirebaseAuthException(code, cause)

internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.auth.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: JsException) {
        throw errorToException(e)
    }
}

private fun errorToException(cause: JsException) = when (val code = cause.errorCode()?.lowercase()) {
    "auth/invalid-user-token" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/requires-recent-login" -> FirebaseAuthRecentLoginRequiredException(code, cause)
    "auth/user-disabled" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/user-token-expired" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/web-storage-unsupported" -> FirebaseAuthWebException(code, cause)
    "auth/network-request-failed" -> FirebaseNetworkException(code, cause)
    "auth/timeout" -> FirebaseNetworkException(code, cause)
    "auth/weak-password" -> FirebaseAuthWeakPasswordException(code, cause)
    "auth/invalid-credential",
    "auth/invalid-verification-code",
    "auth/missing-verification-code",
    "auth/invalid-verification-id",
    "auth/missing-verification-id",
    -> FirebaseAuthInvalidCredentialsException(code, cause)
    "auth/maximum-second-factor-count-exceeded",
    "auth/second-factor-already-in-use",
    -> FirebaseAuthMultiFactorException(code, cause)
    "auth/credential-already-in-use" -> FirebaseAuthUserCollisionException(code, cause)
    "auth/email-already-in-use" -> FirebaseAuthUserCollisionException(code, cause)
    "auth/invalid-email" -> FirebaseAuthEmailException(code, cause)
//                "auth/app-deleted" ->
//                "auth/app-not-authorized" ->
//                "auth/argument-error" ->
//                "auth/invalid-api-key" ->
//                "auth/operation-not-allowed" ->
//                "auth/too-many-arguments" ->
//                "auth/unauthorized-domain" ->
    else -> {
        println("Unknown error code in ${cause.stringifyThrownValue()}")
        FirebaseAuthException(code, cause)
    }
}
