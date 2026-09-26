/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import cocoapods.FirebaseAuth.FIRActionCodeInfo
import cocoapods.FirebaseAuth.FIRActionCodeOperation
import cocoapods.FirebaseAuth.FIRActionCodeSettings
import cocoapods.FirebaseAuth.FIRActionCodeURL
import cocoapods.FirebaseAuth.FIRAdditionalUserInfo
import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthCredential
import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRAuthErrorDomain
import cocoapods.FirebaseAuth.FIRAuthStateDidChangeListenerHandle
import cocoapods.FirebaseAuth.FIREmailAuthProvider
import cocoapods.FirebaseAuth.FIRFacebookAuthProvider
import cocoapods.FirebaseAuth.FIRGitHubAuthProvider
import cocoapods.FirebaseAuth.FIRGoogleAuthProvider
import cocoapods.FirebaseAuth.FIRIDTokenDidChangeListenerHandle
import cocoapods.FirebaseAuth.FIROAuthCredential
import cocoapods.FirebaseAuth.FIROAuthProvider
import cocoapods.FirebaseAuth.FIRTwitterAuthProvider
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseAuth.FIRUserInfoProtocol
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.getApps
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseAuth internal constructor(public val ios: FIRAuth) {
    private val authStateListeners = mutableMapOf<AuthStateListener, FIRAuthStateDidChangeListenerHandle>()
    private val idTokenListeners = mutableMapOf<IdTokenListener, FIRIDTokenDidChangeListenerHandle>()

    public actual val app: FirebaseApp
        get() = Firebase.getApps(null).firstOrNull { (it.ios as Any?) == ios.app } ?: FirebaseApp.getInstance()

    public actual val currentUser: FirebaseUser? get() = ios.currentUser()?.let { FirebaseUserImpl(it, this) }

    public actual val languageCode: String? get() = ios.languageCode()

    public actual val tenantId: String? get() = ios.tenantID()

    public actual val customAuthDomain: String? get() = ios.customAuthDomain()

    public actual fun setLanguageCode(languageCode: String) {
        ios.setLanguageCode(languageCode)
    }

    public actual fun setTenantId(tenantId: String) {
        ios.setTenantID(tenantId)
    }

    public actual fun setCustomAuthDomain(customAuthDomain: String) {
        ios.setCustomAuthDomain(customAuthDomain)
    }

    public actual val firebaseAuthSettings: FirebaseAuthSettings get() = ios.compatSettings()

    public actual fun useAppLanguage() {
        ios.useAppLanguage()
    }

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    public actual fun signOut() {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            ios.signOut(error.ptr)
            error.value?.let { throw it.toAuthException(this@FirebaseAuth) }
        }
    }

    public actual fun isSignInWithEmailLink(link: String): Boolean = ios.isSignInWithEmailLink(link)

    public actual fun addAuthStateListener(listener: AuthStateListener) {
        authStateListeners.getOrPut(listener) { ios.addAuthStateDidChangeListener { _, _ -> listener.onAuthStateChanged(this) } }
    }

    public actual fun removeAuthStateListener(listener: AuthStateListener) {
        authStateListeners.remove(listener)?.let { ios.removeAuthStateDidChangeListener(it) }
    }

    public actual fun addIdTokenListener(listener: IdTokenListener) {
        idTokenListeners.getOrPut(listener) { ios.addIDTokenDidChangeListener { _, _ -> listener.onIdTokenChanged(this) } }
    }

    public actual fun removeIdTokenListener(listener: IdTokenListener) {
        idTokenListeners.remove(listener)?.let { ios.removeIDTokenDidChangeListener(it) }
    }

    public actual fun applyActionCode(code: String): Task<Nothing?> = write(this) { ios.applyActionCode(code, it) }

    public actual fun checkActionCode(code: String): Task<ActionCodeResult> = task(this) { completion ->
        ios.checkActionCode(code) { info, error -> completion(info?.toCompat(), error) }
    }

    public actual fun confirmPasswordReset(code: String, newPassword: String): Task<Nothing?> = write(this) { ios.confirmPasswordResetWithCode(code, newPassword, it) }

    public actual fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> = task(this) { completion ->
        ios.createUserWithEmail(email = email, password = password) { result, error -> completion(result?.toCompat(this), error) }
    }

    @Deprecated("Migrating off of this method is recommended as a security best-practice. Learn more in the Identity Platform documentation for [Email Enumeration Protection](https://cloud.google.com/identity-platform/docs/admin/email-enumeration-protection).")
    public actual fun fetchSignInMethodsForEmail(email: String): Task<SignInMethodQueryResult> = task(this) { completion ->
        ios.fetchSignInMethodsForEmail(email) { methods, error -> completion(SignInMethodQueryResultImpl(methods?.map { it.toString() }), error) }
    }

    public actual fun initializeRecaptchaConfig(): Task<Nothing?> = write(this) { ios.initializeRecaptchaConfigWithCompletion(it) }

    public actual fun revokeAccessToken(token: String): Task<Nothing?> = write(this) { ios.revokeTokenWithAuthorizationCode(token, it) }

    public actual fun sendPasswordResetEmail(email: String): Task<Nothing?> = sendPasswordResetEmail(email, null)

    public actual fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = write(this) {
        if (actionCodeSettings == null) ios.sendPasswordResetWithEmail(email = email, completion = it) else ios.sendPasswordResetWithEmail(email, actionCodeSettings.toIos(), it)
    }

    public actual fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Task<Nothing?> = write(this) { ios.sendSignInLinkToEmail(email, actionCodeSettings.toIos(), it) }

    public actual fun signInAnonymously(): Task<AuthResult> = task(this) { completion ->
        ios.signInAnonymouslyWithCompletion { result, error -> completion(result?.toCompat(this), error) }
    }

    public actual fun signInWithCredential(credential: AuthCredential): Task<AuthResult> = task(this) { completion ->
        ios.signInWithCredential(credential.ios) { result, error -> completion(result?.toCompat(this), error) }
    }

    public actual fun signInWithCustomToken(token: String): Task<AuthResult> = task(this) { completion ->
        ios.signInWithCustomToken(token) { result, error -> completion(result?.toCompat(this), error) }
    }

    public actual fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> = task(this) { completion ->
        ios.signInWithEmail(email = email, password = password) { result, error -> completion(result?.toCompat(this), error) }
    }

    public actual fun signInWithEmailLink(email: String, link: String): Task<AuthResult> = task(this) { completion ->
        ios.signInWithEmail(email = email, link = link) { result, error -> completion(result?.toCompat(this), error) }
    }

    public actual fun updateCurrentUser(user: FirebaseUser): Task<Nothing?> = write(this) { ios.updateCurrentUser(user.ios, it) }

    public actual fun verifyPasswordResetCode(code: String): Task<String> = task(this) { completion ->
        ios.verifyPasswordResetCode(code) { email, error -> completion(email, error) }
    }

    public actual fun interface AuthStateListener {
        public actual fun onAuthStateChanged(auth: FirebaseAuth)
    }

    public actual fun interface IdTokenListener {
        public actual fun onIdTokenChanged(auth: FirebaseAuth)
    }

    override fun equals(other: Any?): Boolean = other is FirebaseAuth && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseAuth(app=${ios.app?.name})"

    public actual companion object {
        private val instances = mutableMapOf<FIRAuth, FirebaseAuth>()

        public actual fun getInstance(): FirebaseAuth = wrap(FIRAuth.auth())

        public actual fun getInstance(app: FirebaseApp): FirebaseAuth = wrap(FIRAuth.authWithApp(app.ios as objcnames.classes.FIRApp))

        /** The instance of the iOS SDK's [auth]. */
        public fun wrap(auth: FIRAuth): FirebaseAuth = instances.getOrPut(auth) { FirebaseAuth(auth) }
    }
}

/** The test settings of [this]; phone number verification, which they configure, is only available on iOS. */
internal expect fun FIRAuth.compatSettings(): FirebaseAuthSettings

/** @property ios The underlying Firebase iOS SDK object. */
public actual abstract class FirebaseUser actual constructor() : UserInfo {
    public abstract val ios: FIRUser
    internal abstract val auth: FirebaseAuth
    public actual abstract val tenantId: String?
    public actual abstract val isAnonymous: Boolean
    public actual abstract val metadata: FirebaseUserMetadata?
    public actual abstract val multiFactor: MultiFactor
    public actual abstract val providerData: List<UserInfo>

    public actual fun delete(): Task<Nothing?> = write(auth) { ios.deleteWithCompletion(it) }

    public actual fun getIdToken(forceRefresh: Boolean): Task<GetTokenResult> = task(auth) { completion ->
        ios.getIDTokenResultForcingRefresh(forceRefresh) { result, error ->
            completion(result?.let { GetTokenResult(it.token(), it.claims().entries.associate { (key, value) -> key.toString() to value.toKotlinValue() }) }, error)
        }
    }

    public actual fun linkWithCredential(credential: AuthCredential): Task<AuthResult> = task(auth) { completion ->
        ios.linkWithCredential(credential.ios) { result, error -> completion(result?.toCompat(auth), error) }
    }

    public actual fun reauthenticate(credential: AuthCredential): Task<Nothing?> = write(auth) { completion ->
        ios.reauthenticateWithCredential(credential.ios) { _, error -> completion(error) }
    }

    public actual fun reauthenticateAndRetrieveData(credential: AuthCredential): Task<AuthResult> = task(auth) { completion ->
        ios.reauthenticateWithCredential(credential.ios) { result, error -> completion(result?.toCompat(auth), error) }
    }

    public actual fun reload(): Task<Nothing?> = write(auth) { ios.reloadWithCompletion(it) }

    public actual fun sendEmailVerification(): Task<Nothing?> = sendEmailVerification(null)

    public actual fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = write(auth) {
        if (actionCodeSettings == null) ios.sendEmailVerificationWithCompletion(it) else ios.sendEmailVerificationWithActionCodeSettings(actionCodeSettings.toIos(), it)
    }

    public actual fun unlink(provider: String): Task<AuthResult> = task(auth) { completion ->
        ios.unlinkFromProvider(provider) { user, error -> completion(user?.let { AuthResultImpl(FirebaseUserImpl(it, auth), null, null) }, error) }
    }

    @Deprecated("Use verifyBeforeUpdateEmail instead", ReplaceWith("verifyBeforeUpdateEmail(email)"))
    public actual fun updateEmail(email: String): Task<Nothing?> = write(auth) { ios.updateEmail(email, it) }

    public actual fun updatePassword(password: String): Task<Nothing?> = write(auth) { ios.updatePassword(password, it) }

    public actual fun updatePhoneNumber(credential: PhoneAuthCredential): Task<Nothing?> = write(auth) { ios.updatePhoneNumberCompat(credential, it) }

    public actual fun updateProfile(request: UserProfileChangeRequest): Task<Nothing?> = write(auth) {
        val change = ios.profileChangeRequest()
        if (request.displayNameSet) change.setDisplayName(request.displayName)
        if (request.photoUriSet) change.setPhotoURL(request.photoUri?.let { url -> NSURL.URLWithString(url) })
        change.commitChangesWithCompletion(it)
    }

    public actual fun verifyBeforeUpdateEmail(newEmail: String): Task<Nothing?> = verifyBeforeUpdateEmail(newEmail, null)

    public actual fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?> = write(auth) {
        if (actionCodeSettings == null) ios.sendEmailVerificationBeforeUpdatingEmail(newEmail, it) else ios.sendEmailVerificationBeforeUpdatingEmail(newEmail, actionCodeSettings.toIos(), it)
    }

    override fun equals(other: Any?): Boolean = other is FirebaseUser && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseUser(uid=$uid)"
}

internal class FirebaseUserImpl(override val ios: FIRUser, override val auth: FirebaseAuth) : FirebaseUser() {
    override val uid: String get() = ios.uid()
    override val providerId: String get() = ios.providerID()
    override val displayName: String? get() = ios.displayName()
    override val email: String? get() = ios.email()
    override val phoneNumber: String? get() = ios.phoneNumber()
    override val isEmailVerified: Boolean get() = ios.emailVerified()
    override val photoUrl: String? get() = ios.photoURL()?.absoluteString
    override val tenantId: String? get() = ios.tenantID()
    override val isAnonymous: Boolean get() = ios.anonymous()
    override val metadata: FirebaseUserMetadata? get() = ios.metadata().let { FirebaseUserMetadataImpl(it.creationDate().toTimestamp(), it.lastSignInDate().toTimestamp()) }
    override val multiFactor: MultiFactor get() = ios.compatMultiFactor(auth)
    override val providerData: List<UserInfo> get() = ios.providerData().mapNotNull { info -> (info as? FIRUserInfoProtocol)?.toCompat() }
}

/** Phone number verification and multi-factor authentication are only available on iOS. */
internal expect fun FIRUser.updatePhoneNumberCompat(credential: PhoneAuthCredential, completion: (NSError?) -> Unit)

internal expect fun FIRUser.compatMultiFactor(auth: FirebaseAuth): MultiFactor

private fun FIRUserInfoProtocol.toCompat(): UserInfo = UserInfoImpl(uid(), providerID(), displayName(), email(), phoneNumber(), false, photoURL()?.absoluteString)

internal fun NSDate?.toTimestamp(): Long = this?.let { (it.timeIntervalSince1970 * 1000).toLong() } ?: 0L

/** A Foundation value as Kotlin collections (dictionaries as maps, arrays as lists). */
internal fun Any?.toKotlinValue(): Any? = when (this) {
    is NSString -> toString()
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toKotlinValue() }
    is List<*> -> map { it.toKotlinValue() }
    else -> this
}

/** The iOS SDK's credential behind a credential of the layer. */
public val AuthCredential.ios: FIRAuthCredential get() = platform as FIRAuthCredential

internal fun FIRAuthCredential.toCompat(): AuthCredential = when (this) {
    is FIROAuthCredential -> OAuthCredentialImpl(this, provider(), IDToken(), accessToken(), secret())
    else -> when (provider()) {
        EmailAuthProvider.PROVIDER_ID -> EmailAuthCredential(this, EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)
        FacebookAuthProvider.PROVIDER_ID -> FacebookAuthCredential(this)
        GithubAuthProvider.PROVIDER_ID -> GithubAuthCredential(this)
        GoogleAuthProvider.PROVIDER_ID -> GoogleAuthCredential(this)
        TwitterAuthProvider.PROVIDER_ID -> TwitterAuthCredential(this)
        PhoneAuthProvider.PROVIDER_ID -> PhoneAuthCredential(this, null)
        else -> NativeCredential(this, provider(), provider())
    }
}

internal fun FIRAuthDataResult.toCompat(auth: FirebaseAuth): AuthResult = AuthResultImpl(
    FirebaseUserImpl(user(), auth),
    credential()?.toCompat(),
    additionalUserInfo()?.toCompat(),
)

private fun FIRAdditionalUserInfo.toCompat(): AdditionalUserInfo = AdditionalUserInfoImpl(
    providerID(),
    username(),
    profile()?.entries?.associate { (key, value) -> key.toString() to value.toKotlinValue() },
    newUser(),
)

private fun FIRActionCodeInfo.toCompat(): ActionCodeResult {
    val operation = operation().toOperation()
    val email = email()
    val details = when (operation) {
        ActionCodeResult.RECOVER_EMAIL, ActionCodeResult.VERIFY_BEFORE_CHANGE_EMAIL -> ActionCodeEmailInfoImpl(email, previousEmail().orEmpty())
        else -> ActionCodeInfoImpl(email)
    }
    return ActionCodeResultImpl(operation, details)
}

private fun FIRActionCodeOperation.toOperation(): Int = when (this) {
    FIRActionCodeOperation.FIRActionCodeOperationPasswordReset -> ActionCodeResult.PASSWORD_RESET
    FIRActionCodeOperation.FIRActionCodeOperationVerifyEmail -> ActionCodeResult.VERIFY_EMAIL
    FIRActionCodeOperation.FIRActionCodeOperationRecoverEmail -> ActionCodeResult.RECOVER_EMAIL
    FIRActionCodeOperation.FIRActionCodeOperationEmailLink -> ActionCodeResult.SIGN_IN_WITH_EMAIL_LINK
    FIRActionCodeOperation.FIRActionCodeOperationVerifyAndChangeEmail -> ActionCodeResult.VERIFY_BEFORE_CHANGE_EMAIL
    FIRActionCodeOperation.FIRActionCodeOperationRevertSecondFactorAddition -> ActionCodeResult.REVERT_SECOND_FACTOR_ADDITION
    else -> ActionCodeResult.ERROR
}

internal fun ActionCodeSettings.toIos(): FIRActionCodeSettings = FIRActionCodeSettings().also {
    url?.let { url -> it.setURL(NSURL.URLWithString(url)) }
    androidPackageName?.let { name -> it.setAndroidPackageName(name, androidInstallApp, androidMinimumVersion) }
    @Suppress("DEPRECATION")
    dynamicLinkDomain?.let { domain -> it.setDynamicLinkDomain(domain) }
    linkDomain?.let { domain -> it.setLinkDomain(domain) }
    it.setHandleCodeInApp(canHandleCodeInApp())
    iosBundle?.let { bundle -> it.setIOSBundleID(bundle) }
}

/** The Android SDK's names of the iOS SDK's error codes (FIRAuthErrors.h), for errors that do not carry their name. */
private val errorCodeNames = mapOf(
    17004L to "ERROR_INVALID_CREDENTIAL",
    17005L to "ERROR_USER_DISABLED",
    17006L to "ERROR_OPERATION_NOT_ALLOWED",
    17007L to "ERROR_EMAIL_ALREADY_IN_USE",
    17008L to "ERROR_INVALID_EMAIL",
    17009L to "ERROR_WRONG_PASSWORD",
    17010L to "ERROR_TOO_MANY_REQUESTS",
    17011L to "ERROR_USER_NOT_FOUND",
    17012L to "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
    17014L to "ERROR_REQUIRES_RECENT_LOGIN",
    17015L to "ERROR_PROVIDER_ALREADY_LINKED",
    17016L to "ERROR_NO_SUCH_PROVIDER",
    17017L to "ERROR_INVALID_USER_TOKEN",
    17020L to "ERROR_NETWORK_REQUEST_FAILED",
    17021L to "ERROR_USER_TOKEN_EXPIRED",
    17023L to "ERROR_INVALID_API_KEY",
    17024L to "ERROR_USER_MISMATCH",
    17025L to "ERROR_CREDENTIAL_ALREADY_IN_USE",
    17026L to "ERROR_WEAK_PASSWORD",
    17028L to "ERROR_APP_NOT_AUTHORIZED",
    17029L to "ERROR_EXPIRED_ACTION_CODE",
    17030L to "ERROR_INVALID_ACTION_CODE",
    17031L to "ERROR_INVALID_MESSAGE_PAYLOAD",
    17032L to "ERROR_INVALID_SENDER",
    17033L to "ERROR_INVALID_RECIPIENT_EMAIL",
    17034L to "ERROR_MISSING_EMAIL",
    17041L to "ERROR_MISSING_PHONE_NUMBER",
    17042L to "ERROR_INVALID_PHONE_NUMBER",
    17043L to "ERROR_MISSING_VERIFICATION_CODE",
    17044L to "ERROR_INVALID_VERIFICATION_CODE",
    17045L to "ERROR_MISSING_VERIFICATION_ID",
    17046L to "ERROR_INVALID_VERIFICATION_ID",
    17051L to "ERROR_SESSION_EXPIRED",
    17052L to "ERROR_QUOTA_EXCEEDED",
    17056L to "ERROR_CAPTCHA_CHECK_FAILED",
    17057L to "ERROR_WEB_CONTEXT_ALREADY_PRESENTED",
    17058L to "ERROR_WEB_CONTEXT_CANCELLED",
    17062L to "ERROR_WEB_INTERNAL_ERROR",
    17078L to "ERROR_SECOND_FACTOR_REQUIRED",
    17084L to "ERROR_MULTI_FACTOR_INFO_NOT_FOUND",
    17087L to "ERROR_SECOND_FACTOR_ALREADY_ENROLLED",
    17088L to "ERROR_MAXIMUM_SECOND_FACTOR_COUNT_EXCEEDED",
    17999L to "ERROR_INTERNAL_ERROR",
)

/** The iOS SDK's error (named with the Android SDK's `ERROR_*` codes in its user info) as the Android SDK's exception. */
internal fun NSError.toAuthException(auth: FirebaseAuth?): FirebaseException {
    val errorCode = if (domain == FIRAuthErrorDomain) {
        userInfo["FIRAuthErrorUserInfoNameKey"] as? String ?: errorCodeNames[code] ?: "ERROR_$code"
    } else {
        "ERROR_$code"
    }
    return authException(
        errorCode,
        localizedDescription,
        AuthErrorDetails(
            resolver = auth?.let { multiFactorResolver(it) },
            email = userInfo["FIRAuthErrorUserInfoEmailKey"] as? String,
            updatedCredential = (userInfo["FIRAuthErrorUserInfoUpdatedCredentialKey"] as? FIRAuthCredential)?.toCompat(),
            reason = userInfo["NSLocalizedFailureReason"] as? String,
        ),
    )
}

/** The resolver of a sign-in that requires a second factor; multi-factor authentication is only available on iOS. */
internal expect fun NSError.multiFactorResolver(auth: FirebaseAuth): MultiFactorResolver?

internal inline fun <T> task(auth: FirebaseAuth?, crossinline start: ((T?, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(error.toAuthException(auth)) }
    return source.task
}

internal inline fun write(auth: FirebaseAuth?, crossinline start: ((NSError?) -> Unit) -> Unit): Task<Nothing?> = task(auth) { completion -> start { error -> completion(null, error) } }

internal actual fun nativeEmailCredential(email: String, password: String): Any = FIREmailAuthProvider.credentialWithEmail(email = email, password = password)

internal actual fun nativeEmailLinkCredential(email: String, emailLink: String): Any = FIREmailAuthProvider.credentialWithEmail(email = email, link = emailLink)

internal actual fun nativeFacebookCredential(accessToken: String): Any = FIRFacebookAuthProvider.credentialWithAccessToken(accessToken)

internal actual fun nativeGithubCredential(token: String): Any = FIRGitHubAuthProvider.credentialWithToken(token)

internal actual fun nativeGoogleCredential(idToken: String?, accessToken: String?): Any = FIRGoogleAuthProvider.credentialWithIDToken(requireNotNull(idToken) { "The iOS SDK requires the Google ID token" }, requireNotNull(accessToken) { "The iOS SDK requires the Google access token" })

internal actual fun nativeTwitterCredential(token: String, secret: String): Any = FIRTwitterAuthProvider.credentialWithToken(token, secret)

internal actual fun nativeOAuthCredential(providerId: String, idToken: String?, accessToken: String?, rawNonce: String?): Any = when {
    idToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, accessToken = requireNotNull(accessToken) { "An ID token or an access token is required" })
    accessToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce)
    rawNonce == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, accessToken = accessToken)
    else -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce, accessToken = accessToken)
}

internal actual fun parseActionCodeUrl(link: String): ActionCodeUrl? = FIRActionCodeURL.actionCodeURLWithLink(link)?.let {
    ActionCodeUrl(it.APIKey(), it.code(), it.continueURL()?.absoluteString, it.languageCode(), it.operation().toOperation())
}
