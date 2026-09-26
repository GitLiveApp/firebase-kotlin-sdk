/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.actionCodeSettingsBuilder
import dev.gitlive.firebase.auth.internal.emailValue
import dev.gitlive.firebase.auth.internal.multiFactorInfoValue
import dev.gitlive.firebase.auth.internal.previousEmailValue
import dev.gitlive.firebase.auth.internal.signOutAwaiting
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.ActionCodeResult as CompatActionCodeResult
import com.google.firebase.auth.ActionCodeSettings as CompatActionCodeSettings
import com.google.firebase.auth.AdditionalUserInfo as CompatAdditionalUserInfo
import com.google.firebase.auth.AuthResult as CompatAuthResult
import com.google.firebase.auth.FirebaseAuth as CompatFirebaseAuth
import com.google.firebase.auth.GetTokenResult as CompatGetTokenResult

/**
 * The entry point of Firebase Authentication.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.auth.FirebaseAuth] this instance wraps.
 */
public class FirebaseAuth internal constructor(public val compat: CompatFirebaseAuth) {
    /** The signed-in user, or null. */
    public val currentUser: FirebaseUser?
        get() = compat.currentUser?.let { FirebaseUser(it) }

    /** The signed-in user now and whenever it changes. */
    public val authStateChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val listener = CompatFirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.let { FirebaseUser(it) }) }
        compat.addAuthStateListener(listener)
        awaitClose { compat.removeAuthStateListener(listener) }
    }

    /** The signed-in user now and whenever it or its ID token changes. */
    public val idTokenChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val listener = CompatFirebaseAuth.IdTokenListener { auth -> trySend(auth.currentUser?.let { FirebaseUser(it) }) }
        compat.addIdTokenListener(listener)
        awaitClose { compat.removeIdTokenListener(listener) }
    }

    /** The language of the emails and SMS the SDK sends; empty for the project default. */
    public var languageCode: String
        get() = compat.languageCode.orEmpty()
        set(value) {
            compat.setLanguageCode(value)
        }

    public suspend fun applyActionCode(code: String) {
        compat.applyActionCode(code).await()
    }

    public suspend fun confirmPasswordReset(code: String, newPassword: String) {
        compat.confirmPasswordReset(code, newPassword).await()
    }

    public suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult(compat.createUserWithEmailAndPassword(email, password).await())

    @Deprecated("Migrating off of this method is recommended as a security best-practice. Learn more in the Identity Platform documentation for [Email Enumeration Protection](https://cloud.google.com/identity-platform/docs/admin/email-enumeration-protection).")
    @Suppress("DEPRECATION")
    public suspend fun fetchSignInMethodsForEmail(email: String): List<String> = compat.fetchSignInMethodsForEmail(email).await().signInMethods.orEmpty()

    public suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings? = null) {
        compat.sendPasswordResetEmail(email, actionCodeSettings?.toCompat()).await()
    }

    public suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) {
        compat.sendSignInLinkToEmail(email, actionCodeSettings.toCompat()).await()
    }

    public fun isSignInWithEmailLink(link: String): Boolean = compat.isSignInWithEmailLink(link)

    public suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult(compat.signInWithEmailAndPassword(email, password).await())

    public suspend fun signInWithCustomToken(token: String): AuthResult = AuthResult(compat.signInWithCustomToken(token).await())

    public suspend fun signInAnonymously(): AuthResult = AuthResult(compat.signInAnonymously().await())

    public suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult = AuthResult(compat.signInWithCredential(authCredential.compat).await())

    public suspend fun signInWithEmailLink(email: String, link: String): AuthResult = AuthResult(compat.signInWithEmailLink(email, link).await())

    public suspend fun signOut() {
        compat.signOutAwaiting()
    }

    public suspend fun updateCurrentUser(user: FirebaseUser) {
        compat.updateCurrentUser(user.compat).await()
    }

    public suspend fun verifyPasswordResetCode(code: String): String = compat.verifyPasswordResetCode(code).await()

    public suspend fun <T : ActionCodeResult> checkActionCode(code: String): T {
        val result = compat.checkActionCode(code).await()
        @Suppress("UNCHECKED_CAST")
        return when (result.operation) {
            CompatActionCodeResult.SIGN_IN_WITH_EMAIL_LINK -> ActionCodeResult.SignInWithEmailLink
            CompatActionCodeResult.VERIFY_EMAIL -> ActionCodeResult.VerifyEmail(result.info!!.emailValue())
            CompatActionCodeResult.PASSWORD_RESET -> ActionCodeResult.PasswordReset(result.info!!.emailValue())
            CompatActionCodeResult.RECOVER_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.RecoverEmail(emailValue(), previousEmailValue())
            }
            CompatActionCodeResult.VERIFY_BEFORE_CHANGE_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.VerifyBeforeChangeEmail(emailValue(), previousEmailValue())
            }
            CompatActionCodeResult.REVERT_SECOND_FACTOR_ADDITION -> (result.info as ActionCodeMultiFactorInfo).run {
                ActionCodeResult.RevertSecondFactorAddition(emailValue(), MultiFactorInfo(multiFactorInfoValue()))
            }
            else -> throw UnsupportedOperationException(result.operation.toString())
        } as T
    }

    /** Connects to the Authentication emulator at [host]:[port]; must be called before the instance is used. */
    public fun useEmulator(host: String, port: Int) {
        compat.useEmulator(host, port)
    }

    override fun equals(other: Any?): Boolean = other is FirebaseAuth && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseAuth(app=${compat.app.name})"
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.AuthResult] this result wraps. */
public class AuthResult internal constructor(public val compat: CompatAuthResult) {
    public val user: FirebaseUser?
        get() = compat.user?.let { FirebaseUser(it) }
    public val credential: AuthCredential?
        get() = compat.credential?.let { AuthCredential(it) }
    public val additionalUserInfo: AdditionalUserInfo?
        get() = compat.additionalUserInfo?.let { AdditionalUserInfo(it) }
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.AdditionalUserInfo] this wraps. */
public class AdditionalUserInfo internal constructor(public val compat: CompatAdditionalUserInfo) {
    public val providerId: String?
        get() = compat.providerId
    public val username: String?
        get() = compat.username
    public val profile: Map<String, Any?>?
        get() = compat.profile
    public val isNewUser: Boolean
        get() = compat.isNewUser
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.GetTokenResult] this wraps. */
public class AuthTokenResult internal constructor(public val compat: CompatGetTokenResult) {
    public val claims: Map<String, Any>
        get() = compat.claims.filterValues { it != null }.mapValues { it.value as Any }
    public val signInProvider: String?
        get() = compat.signInProvider
    public val token: String?
        get() = compat.token
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
    val linkDomain: String? = null,
)

public data class AndroidPackageName(
    val packageName: String,
    val installIfNotAvailable: Boolean = true,
    val minimumVersion: String? = null,
)

/** The settings as the Android-SDK-shaped [com.google.firebase.auth.ActionCodeSettings]. */
@Suppress("DEPRECATION")
public fun ActionCodeSettings.toCompat(): CompatActionCodeSettings = actionCodeSettingsBuilder()
    .setUrl(url)
    .also { androidPackageName?.run { it.setAndroidPackageName(packageName, installIfNotAvailable, minimumVersion) } }
    .also { dynamicLinkDomain?.run { it.setDynamicLinkDomain(this) } }
    .also { linkDomain?.run { it.setLinkDomain(this) } }
    .setHandleCodeInApp(canHandleCodeInApp)
    .also { iOSBundleId?.run { it.setIOSBundleId(this) } }
    .build()

public typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
public typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
public typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
public typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
public typealias FirebaseAuthWeakPasswordException = com.google.firebase.auth.FirebaseAuthWeakPasswordException
public typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
public typealias FirebaseAuthMultiFactorException = com.google.firebase.auth.FirebaseAuthMultiFactorException
public typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
public typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
public typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException

/** The [MultiFactorResolver] of a sign-in that requires a second factor. */
public val FirebaseAuthMultiFactorException.multiFactorResolver: MultiFactorResolver get() = MultiFactorResolver(resolver)
