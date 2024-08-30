/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow

public expect val Firebase.auth: FirebaseAuth

public expect fun Firebase.auth(app: FirebaseApp): FirebaseAuth

public expect class FirebaseAuth {
    public val currentUser: FirebaseUser?
    public val authStateChanged: Flow<FirebaseUser?>
    public val idTokenChanged: Flow<FirebaseUser?>
    public var languageCode: String
    public suspend fun applyActionCode(code: String)
    public suspend fun <T : ActionCodeResult> checkActionCode(code: String): T
    public suspend fun confirmPasswordReset(code: String, newPassword: String)
    public suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult

    @Deprecated("Migrating off of this method is recommended as a security best-practice. Learn more in the Identity Platform documentation for [Email Enumeration Protection](https://cloud.google.com/identity-platform/docs/admin/email-enumeration-protection).")
    public suspend fun fetchSignInMethodsForEmail(email: String): List<String>
    public suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings? = null)
    public suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings)
    public fun isSignInWithEmailLink(link: String): Boolean
    public suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    public suspend fun signInWithCustomToken(token: String): AuthResult
    public suspend fun signInAnonymously(): AuthResult
    public suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult
    public suspend fun signInWithEmailLink(email: String, link: String): AuthResult
    public suspend fun signOut()
    public suspend fun updateCurrentUser(user: FirebaseUser)
    public suspend fun verifyPasswordResetCode(code: String): String
    public fun useEmulator(host: String, port: Int)
}

public expect class AuthResult {
    public val user: FirebaseUser?
    public val credential: AuthCredential?
    public val additionalUserInfo: AdditionalUserInfo?
}

public expect class AdditionalUserInfo {
    public val providerId: String?
    public val username: String?
    public val profile: Map<String, Any?>?
    public val isNewUser: Boolean
}

public expect class AuthTokenResult {
//    val authTimestamp: Long
    public val claims: Map<String, Any>

//    val expirationTimestamp: Long
//    val issuedAtTimestamp: Long
    public val signInProvider: String?
    public val token: String?
}

public sealed class ActionCodeResult {
    public data object SignInWithEmailLink : ActionCodeResult()
    public class PasswordReset internal constructor(public val email: String) : ActionCodeResult()
    public class VerifyEmail internal constructor(public val email: String) : ActionCodeResult()
    public class RecoverEmail internal constructor(public val email: String, public val previousEmail: String) : ActionCodeResult()
    public class VerifyBeforeChangeEmail internal constructor(public val email: String, public val previousEmail: String) : ActionCodeResult()
    public class RevertSecondFactorAddition internal constructor(public val email: String, public val multiFactorInfo: MultiFactorInfo?) : ActionCodeResult()
}

public data class ActionCodeSettings(
    val url: String,
    val androidPackageName: AndroidPackageName? = null,
    val dynamicLinkDomain: String? = null,
    val canHandleCodeInApp: Boolean = false,
    val iOSBundleId: String? = null,
)

public data class AndroidPackageName(
    val packageName: String,
    val installIfNotAvailable: Boolean = true,
    val minimumVersion: String? = null,
)

public expect open class FirebaseAuthException : FirebaseException
public expect class FirebaseAuthActionCodeException : FirebaseAuthException
public expect class FirebaseAuthEmailException : FirebaseAuthException
public expect open class FirebaseAuthInvalidCredentialsException : FirebaseAuthException
public expect class FirebaseAuthWeakPasswordException : FirebaseAuthInvalidCredentialsException
public expect class FirebaseAuthInvalidUserException : FirebaseAuthException
public expect class FirebaseAuthMultiFactorException : FirebaseAuthException
public expect class FirebaseAuthRecentLoginRequiredException : FirebaseAuthException
public expect class FirebaseAuthUserCollisionException : FirebaseAuthException
public expect class FirebaseAuthWebException : FirebaseAuthException
