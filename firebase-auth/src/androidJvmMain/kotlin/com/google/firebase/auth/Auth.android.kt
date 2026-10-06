/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import android.app.Activity
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.auth.stub
import java.util.concurrent.TimeUnit

/*
 * Header stubs for com.google.firebase:firebase-auth (see buildSrc utils/HeaderStubs.kt): compiled against, verified to
 * match the real classes, and deleted from the output so the real SDK binds at runtime. The constructor of AuthCredential
 * only exists for the stubs extending it (the SDK's is not public); it is deprecated with an error so that it need not
 * match the real class and cannot be called. The members beyond the common declarations (the Android `Uri`, `Activity`
 * and `TimeUnit` ones) keep Android-only code compiling against the layer.
 */

private const val STUB_CONSTRUCTOR = "Header stub constructor; the SDK's class is not instantiable"

public actual class FirebaseAuth private constructor() {
    public actual val app: FirebaseApp get() = stub()
    public actual val currentUser: FirebaseUser? get() = stub()
    public actual val languageCode: String? get() = stub()
    public actual val tenantId: String? get() = stub()
    public actual val customAuthDomain: String? get() = stub()
    public actual fun setLanguageCode(languageCode: String): Unit = stub()
    public actual fun setTenantId(tenantId: String): Unit = stub()
    public actual fun setCustomAuthDomain(customAuthDomain: String): Unit = stub()
    public actual val firebaseAuthSettings: FirebaseAuthSettings get() = stub()
    public actual fun useAppLanguage(): Unit = stub()
    public actual fun useEmulator(host: String, port: Int): Unit = stub()
    public actual fun signOut(): Unit = stub()
    public actual fun isSignInWithEmailLink(link: String): Boolean = stub()
    public actual fun addAuthStateListener(listener: AuthStateListener): Unit = stub()
    public actual fun removeAuthStateListener(listener: AuthStateListener): Unit = stub()
    public actual fun addIdTokenListener(listener: IdTokenListener): Unit = stub()
    public actual fun removeIdTokenListener(listener: IdTokenListener): Unit = stub()
    public actual fun applyActionCode(code: String): Task<Nothing?> = stub()
    public actual fun checkActionCode(code: String): Task<ActionCodeResult> = stub()
    public actual fun confirmPasswordReset(code: String, newPassword: String): Task<Nothing?> = stub()
    public actual fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> = stub()

    @Deprecated("Migrating off of this method is recommended as a security best-practice. Learn more in the Identity Platform documentation for [Email Enumeration Protection](https://cloud.google.com/identity-platform/docs/admin/email-enumeration-protection).")
    public actual fun fetchSignInMethodsForEmail(email: String): Task<SignInMethodQueryResult> = stub()
    public actual fun initializeRecaptchaConfig(): Task<Nothing?> = stub()
    public actual fun revokeAccessToken(token: String): Task<Nothing?> = stub()
    public actual fun sendPasswordResetEmail(email: String): Task<Nothing?> = stub()
    public actual fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = stub()
    public actual fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Task<Nothing?> = stub()
    public actual fun signInAnonymously(): Task<AuthResult> = stub()
    public actual fun signInWithCredential(credential: AuthCredential): Task<AuthResult> = stub()
    public actual fun signInWithCustomToken(token: String): Task<AuthResult> = stub()
    public actual fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> = stub()
    public actual fun signInWithEmailLink(email: String, link: String): Task<AuthResult> = stub()
    public actual fun updateCurrentUser(user: FirebaseUser): Task<Nothing?> = stub()
    public actual fun verifyPasswordResetCode(code: String): Task<String> = stub()

    /** Android only: signs in with [provider] through a web flow started from [activity]. */
    public fun startActivityForSignInWithProvider(activity: Activity, provider: FederatedAuthProvider): Task<AuthResult> = stub()

    /** Android only: the result of a provider sign-in flow interrupted by the app being recreated, or null. */
    public val pendingAuthResult: Task<AuthResult>? get() = stub()

    public actual fun interface AuthStateListener {
        public actual fun onAuthStateChanged(auth: FirebaseAuth)
    }

    public actual fun interface IdTokenListener {
        public actual fun onIdTokenChanged(auth: FirebaseAuth)
    }

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseAuth = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseAuth = stub()
    }
}

public actual abstract class FirebaseAuthSettings actual constructor() {
    public actual abstract fun setAppVerificationDisabledForTesting(disabled: Boolean)

    /** Android only. */
    public abstract fun forceRecaptchaFlowForTesting(forceRecaptchaFlow: Boolean)

    /** Android only. */
    public abstract fun setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber: String, smsCode: String)
}

public actual interface UserInfo {
    public actual val uid: String
    public actual val providerId: String
    public actual val displayName: String?
    public actual val email: String?
    public actual val phoneNumber: String?
    public actual val isEmailVerified: Boolean

    /** Android only: the profile photo as a `Uri`. */
    public val photoUrl: Uri?
}

public actual abstract class FirebaseUser actual constructor() : UserInfo {
    public actual abstract val tenantId: String?
    public actual abstract val isAnonymous: Boolean
    public actual abstract val metadata: FirebaseUserMetadata?
    public actual abstract val multiFactor: MultiFactor
    public actual abstract val providerData: List<UserInfo>
    public actual fun delete(): Task<Nothing?> = stub()
    public actual fun getIdToken(forceRefresh: Boolean): Task<GetTokenResult> = stub()
    public actual fun linkWithCredential(credential: AuthCredential): Task<AuthResult> = stub()
    public actual fun reauthenticate(credential: AuthCredential): Task<Nothing?> = stub()
    public actual fun reauthenticateAndRetrieveData(credential: AuthCredential): Task<AuthResult> = stub()
    public actual fun reload(): Task<Nothing?> = stub()
    public actual fun sendEmailVerification(): Task<Nothing?> = stub()
    public actual fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = stub()
    public actual fun unlink(provider: String): Task<AuthResult> = stub()

    @Deprecated("Use verifyBeforeUpdateEmail instead", ReplaceWith("verifyBeforeUpdateEmail(email)"))
    public actual fun updateEmail(email: String): Task<Nothing?> = stub()
    public actual fun updatePassword(password: String): Task<Nothing?> = stub()
    public actual fun updatePhoneNumber(credential: PhoneAuthCredential): Task<Nothing?> = stub()
    public actual fun updateProfile(request: UserProfileChangeRequest): Task<Nothing?> = stub()
    public actual fun verifyBeforeUpdateEmail(newEmail: String): Task<Nothing?> = stub()
    public actual fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = stub()

    /** Android only: links [provider] through a web flow started from [activity]. */
    public fun startActivityForLinkWithProvider(activity: Activity, provider: FederatedAuthProvider): Task<AuthResult> = stub()

    /** Android only: re-authenticates with [provider] through a web flow started from [activity]. */
    public fun startActivityForReauthenticateWithProvider(activity: Activity, provider: FederatedAuthProvider): Task<AuthResult> = stub()
}

public actual class UserProfileChangeRequest private constructor() {
    public actual val displayName: String? get() = stub()

    /** Android only. */
    public val photoUri: Uri? get() = stub()

    public actual class Builder actual constructor() {
        public val displayName: String? get() = stub()

        /** Android only. */
        public val photoUri: Uri? get() = stub()
        public actual fun setDisplayName(displayName: String?): Builder = stub()

        /** Android only. */
        public fun setPhotoUri(photoUri: Uri?): Builder = stub()
        public actual fun build(): UserProfileChangeRequest = stub()
    }
}

public actual abstract class AuthCredential {
    @Deprecated(STUB_CONSTRUCTOR, level = DeprecationLevel.ERROR)
    protected constructor()

    public actual abstract val provider: String
    public actual abstract val signInMethod: String
}

@Suppress("DEPRECATION_ERROR")
public actual class EmailAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual class FacebookAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual class GithubAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual class GoogleAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual class TwitterAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual class PhoneAuthCredential private constructor() : AuthCredential() {
    override val provider: String get() = stub()
    override val signInMethod: String get() = stub()
    public actual val smsCode: String? get() = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual abstract class OAuthCredential actual constructor() : AuthCredential() {
    public actual abstract val accessToken: String?
    public actual abstract val idToken: String?
    public actual abstract val secret: String?
}

public actual class EmailAuthProvider private constructor() {
    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "password"

        @JvmField
        public actual val EMAIL_PASSWORD_SIGN_IN_METHOD: String = "password"

        @JvmField
        public actual val EMAIL_LINK_SIGN_IN_METHOD: String = "emailLink"

        @JvmStatic
        public actual fun getCredential(email: String, password: String): AuthCredential = stub()

        @JvmStatic
        public actual fun getCredentialWithLink(email: String, emailLink: String): AuthCredential = stub()
    }
}

public actual class FacebookAuthProvider private constructor() {
    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "facebook.com"

        @JvmField
        public actual val FACEBOOK_SIGN_IN_METHOD: String = "facebook.com"

        @JvmStatic
        public actual fun getCredential(accessToken: String): AuthCredential = stub()
    }
}

public actual class GithubAuthProvider private constructor() {
    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "github.com"

        @JvmField
        public actual val GITHUB_SIGN_IN_METHOD: String = "github.com"

        @JvmStatic
        public actual fun getCredential(token: String): AuthCredential = stub()
    }
}

public actual class GoogleAuthProvider private constructor() {
    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "google.com"

        @JvmField
        public actual val GOOGLE_SIGN_IN_METHOD: String = "google.com"

        @JvmStatic
        public actual fun getCredential(idToken: String?, accessToken: String?): AuthCredential = stub()
    }
}

public actual class TwitterAuthProvider private constructor() {
    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "twitter.com"

        @JvmField
        public actual val TWITTER_SIGN_IN_METHOD: String = "twitter.com"

        @JvmStatic
        public actual fun getCredential(token: String, secret: String): AuthCredential = stub()
    }
}

public actual abstract class FederatedAuthProvider actual constructor()

public actual class OAuthProvider private constructor() : FederatedAuthProvider() {
    public actual val providerId: String get() = stub()

    public actual class Builder private constructor() {
        /** Android only. */
        public val scopes: List<String> get() = stub()
        public actual fun setScopes(scopes: List<String>): Builder = stub()
        public actual fun addCustomParameter(key: String, value: String): Builder = stub()
        public actual fun addCustomParameters(customParameters: Map<String, String>): Builder = stub()
        public actual fun build(): OAuthProvider = stub()
    }

    public actual class CredentialBuilder private constructor() {
        /** Android only. */
        public val accessToken: String? get() = stub()

        /** Android only. */
        public val idToken: String? get() = stub()
        public actual fun setAccessToken(accessToken: String): CredentialBuilder = stub()
        public actual fun setIdToken(idToken: String): CredentialBuilder = stub()
        public actual fun setIdTokenWithRawNonce(idToken: String, rawNonce: String): CredentialBuilder = stub()
        public actual fun build(): AuthCredential = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(providerId: String): Builder = stub()

        @JvmStatic
        public actual fun newBuilder(providerId: String, firebaseAuth: FirebaseAuth): Builder = stub()

        @JvmStatic
        public actual fun newCredentialBuilder(providerId: String): CredentialBuilder = stub()

        @JvmStatic
        @Deprecated("Use newCredentialBuilder(providerId)", ReplaceWith("OAuthProvider.newCredentialBuilder(providerId).setIdToken(idToken).setAccessToken(accessToken).build()"))
        public actual fun getCredential(providerId: String, idToken: String, accessToken: String): AuthCredential = stub()
    }
}

public actual class PhoneAuthProvider private constructor() {
    /** Android only: verifies [phoneNumber] with the deprecated instance API. */
    @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
    public fun verifyPhoneNumber(phoneNumber: String, timeout: Long, unit: TimeUnit, activity: Activity, callbacks: OnVerificationStateChangedCallbacks): Unit = stub()

    /** Android only: verifies [phoneNumber] with the deprecated instance API. */
    @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
    public fun verifyPhoneNumber(phoneNumber: String, timeout: Long, unit: TimeUnit, activity: Activity, callbacks: OnVerificationStateChangedCallbacks, forceResendingToken: ForceResendingToken): Unit = stub()

    public actual class ForceResendingToken private constructor()

    public actual abstract class OnVerificationStateChangedCallbacks actual constructor() {
        public actual abstract fun onVerificationCompleted(credential: PhoneAuthCredential)
        public actual abstract fun onVerificationFailed(exception: FirebaseException)
        public actual open fun onCodeSent(verificationId: String, token: ForceResendingToken): Unit = stub()
        public actual open fun onCodeAutoRetrievalTimeOut(verificationId: String): Unit = stub()
    }

    public actual companion object {
        @JvmField
        public actual val PROVIDER_ID: String = "phone"

        @JvmField
        public actual val PHONE_SIGN_IN_METHOD: String = "phone"

        @JvmStatic
        public actual fun getCredential(verificationId: String, smsCode: String): PhoneAuthCredential = stub()

        @JvmStatic
        public actual fun verifyPhoneNumber(options: PhoneAuthOptions): Unit = stub()

        @JvmStatic
        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public actual fun getInstance(): PhoneAuthProvider = stub()

        @JvmStatic
        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public actual fun getInstance(firebaseAuth: FirebaseAuth): PhoneAuthProvider = stub()
    }
}

public actual class PhoneAuthOptions private constructor() {
    public actual class Builder actual constructor(firebaseAuth: FirebaseAuth) {
        public actual fun setPhoneNumber(phoneNumber: String): Builder = stub()
        public actual fun setCallbacks(callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks): Builder = stub()
        public actual fun setForceResendingToken(token: PhoneAuthProvider.ForceResendingToken): Builder = stub()
        public actual fun setMultiFactorHint(hint: PhoneMultiFactorInfo): Builder = stub()
        public actual fun setMultiFactorSession(session: MultiFactorSession): Builder = stub()
        public actual fun requireSmsValidation(requireSmsValidation: Boolean): Builder = stub()
        public actual fun build(): PhoneAuthOptions = stub()

        /** Android only: the activity the verification (and its reCAPTCHA fallback) is attached to; required. */
        public fun setActivity(activity: Activity): Builder = stub()

        /** Android only: how long to wait for SMS auto-retrieval before [PhoneAuthProvider.OnVerificationStateChangedCallbacks.onCodeAutoRetrievalTimeOut]. */
        public fun setTimeout(timeout: Long?, unit: TimeUnit): Builder = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()

        @JvmStatic
        public actual fun newBuilder(firebaseAuth: FirebaseAuth): Builder = stub()
    }
}

public actual abstract class MultiFactor actual constructor() {
    public actual abstract val enrolledFactors: List<MultiFactorInfo>
    public actual abstract fun enroll(assertion: MultiFactorAssertion, displayName: String?): Task<Nothing?>
    public actual abstract fun getSession(): Task<MultiFactorSession>
    public actual abstract fun unenroll(info: MultiFactorInfo): Task<Nothing?>
    public actual abstract fun unenroll(factorUid: String): Task<Nothing?>
}

public actual abstract class MultiFactorAssertion actual constructor() {
    public actual abstract val factorId: String
}

public actual abstract class MultiFactorInfo actual constructor() {
    public actual abstract val displayName: String?
    public actual abstract val enrollmentTimestamp: Long
    public actual abstract val factorId: String
    public actual abstract val uid: String

    public actual companion object {
        @JvmField
        public actual val FACTOR_ID_KEY: String = "factorIdKey"
    }
}

public actual class PhoneMultiFactorInfo actual constructor(uid: String, displayName: String?, enrollmentTimestamp: Long, phoneNumber: String) : MultiFactorInfo() {
    override val displayName: String? get() = stub()
    override val enrollmentTimestamp: Long get() = stub()
    override val factorId: String get() = stub()
    override val uid: String get() = stub()
    public actual val phoneNumber: String get() = stub()
}

public actual class TotpMultiFactorInfo private constructor() : MultiFactorInfo() {
    override val displayName: String? get() = stub()
    override val enrollmentTimestamp: Long get() = stub()
    override val factorId: String get() = stub()
    override val uid: String get() = stub()
}

public actual abstract class MultiFactorSession actual constructor()

public actual abstract class MultiFactorResolver actual constructor() {
    public actual abstract val firebaseAuth: FirebaseAuth
    public actual abstract val hints: List<MultiFactorInfo>
    public actual abstract val session: MultiFactorSession
    public actual abstract fun resolveSignIn(assertion: MultiFactorAssertion): Task<AuthResult>
}

public actual class PhoneMultiFactorAssertion actual constructor(credential: PhoneAuthCredential) : MultiFactorAssertion() {
    override val factorId: String get() = stub()
}

public actual class PhoneMultiFactorGenerator actual constructor() {
    public actual companion object {
        @JvmField
        public actual val FACTOR_ID: String = "phone"

        @JvmStatic
        public actual fun getAssertion(credential: PhoneAuthCredential): PhoneMultiFactorAssertion = stub()
    }
}

public actual class TotpMultiFactorAssertion private constructor() : MultiFactorAssertion() {
    override val factorId: String get() = stub()
}

public actual class TotpMultiFactorGenerator private constructor() {
    public actual companion object {
        @JvmField
        public actual val FACTOR_ID: String = "totp"

        @JvmStatic
        public actual fun generateSecret(session: MultiFactorSession): Task<TotpSecret> = stub()

        @JvmStatic
        public actual fun getAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): TotpMultiFactorAssertion = stub()

        @JvmStatic
        public actual fun getAssertionForSignIn(enrollmentId: String, oneTimePassword: String): TotpMultiFactorAssertion = stub()
    }
}

public actual interface TotpSecret {
    public actual val sharedSecretKey: String
    public actual fun generateQrCodeUrl(accountName: String, issuer: String): String
    public actual fun openInOtpApp(qrCodeUrl: String)

    /** Android only. */
    public val codeIntervalSeconds: Int

    /** Android only. */
    public val codeLength: Int

    /** Android only. */
    public val enrollmentCompletionDeadline: Long

    /** Android only. */
    public val hashAlgorithm: String

    /** Android only. */
    public val sessionInfo: String

    /** Android only. */
    public fun generateQrCodeUrl(): String

    /** Android only. */
    public fun openInOtpApp(qrCodeUrl: String, fallbackUrl: String, activity: Activity)
}

public actual open class FirebaseAuthException actual constructor(errorCode: String, message: String) : FirebaseException(message) {
    public actual val errorCode: String = errorCode
}

public actual class FirebaseAuthActionCodeException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual open class FirebaseAuthEmailException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual open class FirebaseAuthInvalidCredentialsException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual class FirebaseAuthInvalidUserException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual open class FirebaseAuthMissingActivityForRecaptchaException actual constructor() : FirebaseAuthException("ERROR_MISSING_ACTIVITY", "An Activity is required for phone verification")

public actual open class FirebaseAuthMultiFactorException actual constructor(errorCode: String, message: String, resolver: MultiFactorResolver) : FirebaseAuthException(errorCode, message) {
    public actual val resolver: MultiFactorResolver = resolver
}

public actual class FirebaseAuthRecentLoginRequiredException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual class FirebaseAuthUserCollisionException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message) {
    public actual val email: String? get() = stub()
    public actual val updatedCredential: AuthCredential? get() = stub()
}

public actual class FirebaseAuthWeakPasswordException actual constructor(errorCode: String, message: String, reason: String?) : FirebaseAuthInvalidCredentialsException(errorCode, message) {
    public actual val reason: String? = reason
}

public actual open class FirebaseAuthWebException actual constructor(errorCode: String, message: String) : FirebaseAuthException(errorCode, message)

public actual open class ActionCodeInfo actual constructor() {
    public actual open val email: String get() = stub()
}

public actual class ActionCodeUrl private constructor() {
    public actual val apiKey: String? get() = stub()
    public actual val code: String? get() = stub()
    public actual val continueUrl: String? get() = stub()
    public actual val languageCode: String? get() = stub()
    public actual val operation: Int get() = stub()

    public actual companion object {
        @JvmStatic
        public actual fun parseLink(link: String): ActionCodeUrl? = stub()
    }
}

public actual class ActionCodeSettings private constructor() {
    public actual val url: String? get() = stub()
    public actual fun canHandleCodeInApp(): Boolean = stub()

    @get:JvmName("getIOSBundle")
    public actual val iosBundle: String? get() = stub()
    public actual val androidPackageName: String? get() = stub()
    public actual val androidInstallApp: Boolean get() = stub()
    public actual val androidMinimumVersion: String? get() = stub()
    public actual val linkDomain: String? get() = stub()

    public actual class Builder private constructor() {
        public actual val url: String? get() = stub()
        public actual val handleCodeInApp: Boolean get() = stub()

        @get:JvmName("getIOSBundleId")
        public actual val iosBundleId: String? get() = stub()
        public actual val linkDomain: String? get() = stub()

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("linkDomain"))
        public actual val dynamicLinkDomain: String? get() = stub()
        public actual fun setUrl(url: String): Builder = stub()
        public actual fun setHandleCodeInApp(handleCodeInApp: Boolean): Builder = stub()
        public actual fun setIOSBundleId(iOSBundleId: String): Builder = stub()
        public actual fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder = stub()
        public actual fun setLinkDomain(linkDomain: String): Builder = stub()

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("setLinkDomain(dynamicLinkDomain)"))
        public actual fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder = stub()
        public actual fun build(): ActionCodeSettings = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()
    }
}
