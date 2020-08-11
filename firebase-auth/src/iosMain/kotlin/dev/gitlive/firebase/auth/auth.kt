/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.*


actual val Firebase.auth
    get() = FirebaseAuth(FIRAuth.auth())

actual fun Firebase.auth(app: FirebaseApp) =
    FirebaseAuth(FIRAuth.authWithApp(app.ios))

actual class FirebaseAuth internal constructor(val ios: FIRAuth) {

    actual val currentUser: FirebaseUser?
        get() = ios.currentUser?.let { FirebaseUser(it) }

    actual val authStateChanged get() = callbackFlow {
        val handle = ios.addAuthStateDidChangeListener { _, user -> offer(user?.let { FirebaseUser(it) }) }
        awaitClose { ios.removeAuthStateDidChangeListener(handle) }
    }

    actual val idTokenChanged get() = callbackFlow {
        val handle = ios.addIDTokenDidChangeListener { _, user -> offer(user?.let { FirebaseUser(it) }) }
        awaitClose { ios.removeIDTokenDidChangeListener(handle) }
    }

    actual var languageCode: String
        get() = ios.languageCode ?: ""
        set(value) { ios.setLanguageCode(value) }

    actual suspend fun applyActionCode(code: String) = ios.await { applyActionCode(code, it) }.run { Unit }
    actual suspend fun checkActionCode(code: String): ActionCodeResult = ActionCodeResult(ios.awaitResult { checkActionCode(code, it) })
    actual suspend fun confirmPasswordReset(code: String, newPassword: String) = ios.await { confirmPasswordResetWithCode(code, newPassword, it) }.run { Unit }

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        AuthResult(ios.awaitResult { createUserWithEmail(email = email, password = password, completion = it) })

    actual suspend fun fetchSignInMethodsForEmail(email: String): SignInMethodQueryResult {
        val signInMethods: List<*> = ios.awaitResult { fetchSignInMethodsForEmail(email, it) }
        return SignInMethodQueryResult(signInMethods.mapNotNull { it as String })
    }

    actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) {
        ios.await { actionCodeSettings?.let { actionSettings -> sendPasswordResetWithEmail(email, actionSettings.ios, it) } ?: sendPasswordResetWithEmail(email = email, completion = it) }
    }

    actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) = ios.await { sendSignInLinkToEmail(email, actionCodeSettings.ios, it) }.run { Unit }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        AuthResult(ios.awaitResult { signInWithEmail(email = email, password = password, completion = it) })

    actual suspend fun signInWithCustomToken(token: String) =
        AuthResult(ios.awaitResult { signInWithCustomToken(token, it) })

    actual suspend fun signInAnonymously() =
        AuthResult(ios.awaitResult { signInAnonymouslyWithCompletion(it) })

    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        AuthResult(ios.awaitResult { signInWithCredential(authCredential.ios, it) })

    actual suspend fun signOut() = ios.throwError { signOut(it) }.run { Unit }

    actual suspend fun updateCurrentUser(user: FirebaseUser) = ios.await { updateCurrentUser(user.ios, it) }.run { Unit }
    actual suspend fun verifyPasswordResetCode(code: String): String = ios.awaitResult { verifyPasswordResetCode(code, it) }
}

actual class AuthResult internal constructor(val ios: FIRAuthDataResult) {
    actual val user: FirebaseUser?
        get() = FirebaseUser(ios.user)
}

actual class ActionCodeResult(val ios: FIRActionCodeInfo) {
    actual val operation: Operation
        get() = when (ios.operation) {
            FIRActionCodeOperationPasswordReset -> Operation.PasswordReset
            FIRActionCodeOperationVerifyEmail -> Operation.VerifyEmail
            FIRActionCodeOperationRecoverEmail -> Operation.RecoverEmail
            FIRActionCodeOperationUnknown-> Operation.Error
            FIRActionCodeOperationEmailLink -> Operation.SignInWithEmailLink
            FIRActionCodeOperationVerifyAndChangeEmail -> Operation.VerifyBeforeChangeEmail
            FIRActionCodeOperationRevertSecondFactorAddition -> Operation.RevertSecondFactorAddition
            else -> Operation.Error
        }
    actual fun <T, A: ActionCodeDataType<T>> getData(type: A): T? = when (type) {
        is ActionCodeDataType.Email -> ios.email
        is ActionCodeDataType.PreviousEmail -> ios.previousEmail
        is ActionCodeDataType.MultiFactor -> null
        else -> null
    } as? T
}

actual class SignInMethodQueryResult(actual val signInMethods: List<String>)

actual class FirebaseUser internal constructor(val ios: FIRUser) {
    actual val uid: String
        get() = ios.uid
    actual val displayName: String?
        get() = ios.displayName
    actual val email: String?
        get() = ios.email
    actual val phoneNumber: String?
        get() = ios.phoneNumber
    actual val photoURL: String?
        get() = ios.photoURL?.absoluteString
    actual val isAnonymous: Boolean
        get() = ios.anonymous
    actual val isEmailVerified: Boolean
        get() = ios.emailVerified
    actual val metaData: MetaData?
        get() = MetaData(ios.metadata)
    actual val multiFactor: MultiFactor
        get() = MultiFactor(ios.multiFactor)
    actual val providerData: List<UserInfo>
        get() = ios.providerData.mapNotNull { provider -> (provider as? FIRUserInfoProtocol)?.let { UserInfo(it) } }
    actual val providerId: String
        get() = ios.providerID
    actual suspend fun delete() = ios.await { deleteWithCompletion(it) }.run { Unit }
    actual suspend fun reload() = ios.await { reloadWithCompletion(it) }.run { Unit }
    actual suspend fun getIdToken(forceRefresh: Boolean) {
        val token: String = ios.awaitResult { getIDTokenForcingRefresh(forceRefresh, it) }
    }
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(ios.awaitResult { linkWithCredential(credential.ios, it) })
    actual suspend fun reauthenticate(credential: AuthCredential) {
        val result: FIRAuthDataResult = ios.awaitResult { reauthenticateWithCredential(credential.ios, it) }
    }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(ios.awaitResult { reauthenticateAndRetrieveDataWithCredential(credential.ios, it) })

    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) = ios.await {
        actionCodeSettings?.let { actionSettings -> sendEmailVerificationWithActionCodeSettings(actionSettings.ios, it) } ?: sendEmailVerificationWithCompletion(it)
    }.run { Unit }
    actual suspend fun unlink(provider: String): FirebaseUser? {
        val user: FIRUser? = ios.awaitResult { unlinkFromProvider(provider, it) }
        return user?.let {
            FirebaseUser(it)
        }
    }
    actual suspend fun updateEmail(email: String) = ios.await { updateEmail(email, it) }.run { Unit }
    actual suspend fun updatePassword(password: String) = ios.await { updatePassword(password, it) }.run { Unit }
    actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) = ios.await { updatePhoneNumberCredential(credential.ios, it) }.run { Unit }
    actual suspend fun updateProfile(buildRequest: (UserProfileChangeRequest.Builder) -> Unit) {
        val request = UserProfileChangeRequest.Builder(this.ios).apply { buildRequest(this) }.build()
        ios.await { request.ios.commitChangesWithCompletion(it) }
    }
    actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) = ios.await {
        actionCodeSettings?.let { actionSettings -> sendEmailVerificationBeforeUpdatingEmail(newEmail, actionSettings.ios, it) } ?: sendEmailVerificationBeforeUpdatingEmail(newEmail, it)
    }.run { Unit }
}

actual class UserInfo(val ios: FIRUserInfoProtocol) {
    actual val displayName: String?
        get() = ios.displayName
    actual val email: String?
        get() = ios.email
    actual val phoneNumber: String?
        get() = ios.phoneNumber
    actual val photoURL: String?
        get() = ios.photoURL?.absoluteString
    actual val providerId: String
        get() = ios.providerID
    actual val uid: String
        get() = ios.uid
}

actual class MetaData(val ios: FIRUserMetadata) {
    actual val creationTime: Long?
        get() = ios.creationDate?.timeIntervalSinceReferenceDate?.toLong()
    actual val lastSignInTime: Long?
        get() = ios.lastSignInDate?.timeIntervalSinceReferenceDate?.toLong()
}

actual class UserProfileChangeRequest(val ios: FIRUserProfileChangeRequest) {
    actual class Builder(private val user: FIRUser) {

        private val request = user.profileChangeRequest()

        actual fun setDisplayName(displayName: String?): Builder = apply {
            request.setDisplayName(displayName)
        }
        actual fun setPhotoURL(photoURL: String?): Builder = apply {
            request.setPhotoURL(photoURL?.let { NSURL.URLWithString(it) })
        }
        actual fun build(): UserProfileChangeRequest = UserProfileChangeRequest(request)
    }
    actual val displayName: String?
        get() = ios.displayName
    actual val photoURL: String?
        get() = ios.photoURL?.absoluteString
}

actual open class FirebaseAuthException(message: String): FirebaseException(message)
actual open class FirebaseAuthActionCodeException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthEmailException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthInvalidCredentialsException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthInvalidUserException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthMultiFactorException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthRecentLoginRequiredException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthUserCollisionException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthWeakPasswordException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthWebException(message: String): FirebaseAuthException(message)

actual open class AuthCredential(open val ios: FIRAuthCredential) {
    actual val providerId: String
        get() = ios.provider
}

actual class PhoneAuthCredential(override val ios: FIRPhoneAuthCredential) : AuthCredential(ios)

actual object EmailAuthProvider {
    actual fun credentialWithEmail(
        email: String,
        password: String
    ): AuthCredential =
        AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, password = password))
}

actual class MultiFactor(val ios: FIRMultiFactor) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = ios.enrolledFactors.mapNotNull { info -> (info as? FIRMultiFactorInfo)?.let{  MultiFactorInfo(it) } }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = ios.await { enrollWithAssertion(multiFactorAssertion.ios, displayName, it) }.run { Unit }
    actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(ios.awaitResult { getSessionWithCompletion(completion = it) })
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = ios.await { unenrollWithInfo(multiFactorInfo.ios, it) }.run { Unit }
    actual suspend fun unenroll(factorUid: String) = ios.await { unenrollWithFactorUID(factorUid, it) }.run { Unit }
}

actual class MultiFactorInfo(val ios: FIRMultiFactorInfo) {
    actual val displayName: String?
        get() = ios.displayName
    actual val enrollmentTime: Long
        get() = ios.enrollmentDate.timeIntervalSinceReferenceDate.toLong()
    actual val factorId: String
        get() = ios.factorID
    actual val uid: String
        get() = ios.UID
}

actual class MultiFactorAssertion(val ios: FIRMultiFactorAssertion) {
    actual val factorId: String
        get() = ios.factorID
}

actual class MultiFactorSession(val ios: FIRMultiFactorSession)

actual class ActionCodeSettings private constructor(val ios: FIRActionCodeSettings) {
    actual class Builder(val ios: FIRActionCodeSettings = FIRActionCodeSettings()) {
        actual fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder = apply {
            ios.setAndroidPackageName(androidPackageName, installIfNotAvailable, minimumVersion)
        }
        actual fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder = apply {
            ios.setDynamicLinkDomain(dynamicLinkDomain)
        }
        actual fun setHandleCodeInApp(status: Boolean): Builder = apply {
            ios.setHandleCodeInApp(status)
        }
        actual fun setIOSBundleId(iOSBundleId: String): Builder = apply {
            ios.setIOSBundleID(iOSBundleId)
        }
        actual fun setUrl(url: String): Builder = apply {
            ios.setURL(NSURL.URLWithString(url))
        }
        actual fun build(): ActionCodeSettings = ActionCodeSettings(ios)
    }

    actual val canHandleCodeInApp: Boolean
        get() = ios.handleCodeInApp()
    actual val androidInstallApp: Boolean
        get() = ios.androidInstallIfNotAvailable
    actual val androidMinimumVersion: String?
        get() = ios.androidMinimumVersion
    actual val androidPackageName: String?
        get() = ios.androidPackageName
    actual val iOSBundle: String?
        get() = ios.iOSBundleID
    actual val url: String
        get() = ios.URL?.absoluteString ?: ""
}


private fun <T, R> T.throwError(block: T.(errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
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

private suspend fun <T, R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R>()
    function { result, error ->
        if(result != null) {
            job.complete(result)
        } else if(error != null) {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await()
}

private suspend fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
}

private fun NSError.toException() = when(domain) {
    FIRAuthErrorDomain -> when(code) {
        FIRAuthErrorCodeInvalidActionCode,
        FIRAuthErrorCodeExpiredActionCode -> FirebaseAuthActionCodeException(toString())

        FIRAuthErrorCodeInvalidEmail,
        FIRAuthErrorCodeEmailAlreadyInUse -> FirebaseAuthEmailException(toString())

        FIRAuthErrorCodeInvalidCredential -> FirebaseAuthInvalidCredentialsException(toString())

        FIRAuthErrorCodeInvalidUserToken -> FirebaseAuthInvalidUserException(toString())

        FIRAuthErrorCodeRequiresRecentLogin -> FirebaseAuthRecentLoginRequiredException(toString())

        FIRAuthErrorCodeMultiFactorInfoNotFound -> FirebaseAuthMultiFactorException(toString())

        FIRAuthErrorCodeEmailAlreadyInUse,
        FIRAuthErrorCodeAccountExistsWithDifferentCredential,
        FIRAuthErrorCodeCredentialAlreadyInUse -> FirebaseAuthUserCollisionException(toString())

        FIRAuthErrorCodeWebContextAlreadyPresented,
        FIRAuthErrorCodeWebContextCancelled,
        FIRAuthErrorCodeWebInternalError -> FirebaseAuthWebException(toString())

        else -> FirebaseAuthException(toString())
    }
    else -> FirebaseAuthException(toString())
}
