/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.net.Uri
import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.EmailAuthProvider
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
        val listener = AuthStateListener { auth -> offer(auth.currentUser?.let { FirebaseUser(it) }) }
        android.addAuthStateListener(listener)
        awaitClose { android.removeAuthStateListener(listener) }
    }

    actual val idTokenChanged: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = com.google.firebase.auth.FirebaseAuth.IdTokenListener { auth -> offer(auth.currentUser?.let { FirebaseUser(it) })}
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

actual open class AuthCredential(open val android: com.google.firebase.auth.AuthCredential) {
    actual val providerId: String
        get() = android.provider
    actual val signInMethod: String
        get() = android.signInMethod
}

actual class PhoneAuthCredential(override val android: com.google.firebase.auth.PhoneAuthCredential) : AuthCredential(android) {
    actual val smsCode: String?
        get() = android.smsCode
}

actual class AuthResult internal constructor(val android: com.google.firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = android.user?.let { FirebaseUser(it) }
}

actual class ActionCodeResult(val android: com.google.firebase.auth.ActionCodeResult) {
    actual val operation: Operation
        get() = when (android.operation) {
            com.google.firebase.auth.ActionCodeResult.PASSWORD_RESET -> Operation.PasswordReset
            com.google.firebase.auth.ActionCodeResult.VERIFY_EMAIL -> Operation.VerifyEmail
            com.google.firebase.auth.ActionCodeResult.RECOVER_EMAIL -> Operation.RecoverEmail
            com.google.firebase.auth.ActionCodeResult.ERROR -> Operation.Error
            com.google.firebase.auth.ActionCodeResult.SIGN_IN_WITH_EMAIL_LINK -> Operation.SignInWithEmailLink
            com.google.firebase.auth.ActionCodeResult.VERIFY_BEFORE_CHANGE_EMAIL -> Operation.VerifyBeforeChangeEmail
            com.google.firebase.auth.ActionCodeResult.REVERT_SECOND_FACTOR_ADDITION -> Operation.RevertSecondFactorAddition
            else -> Operation.Error
        }
    actual fun <T, A: ActionCodeDataType<T>> getData(type: A): T? = when (type) {
        is ActionCodeDataType.Email -> android.info?.email
        is ActionCodeDataType.PreviousEmail -> (android.info as? ActionCodeEmailInfo)?.previousEmail
        is ActionCodeDataType.MultiFactor -> (android.info as? ActionCodeMultiFactorInfo)?.multiFactorInfo?.let { MultiFactorInfo(it) }
        else -> null
    } as? T
}

actual class SignInMethodQueryResult(val android: com.google.firebase.auth.SignInMethodQueryResult) {
    actual val signInMethods: List<String>
        get() = android.signInMethods ?: emptyList()
}

actual class FirebaseUser internal constructor(val android: com.google.firebase.auth.FirebaseUser) {
    actual val uid: String
        get() = android.uid
    actual val displayName: String?
        get() = android.displayName
    actual val email: String?
        get() = android.email
    actual val phoneNumber: String?
        get() = android.phoneNumber
    actual val photoURL: String?
        get() = android.photoUrl?.toString()
    actual val isAnonymous: Boolean
        get() = android.isAnonymous
    actual val isEmailVerified: Boolean
        get() = android.isEmailVerified
    actual val metaData: MetaData?
        get() = android.metadata?.let{ MetaData(it) }
    actual val multiFactor: MultiFactor
        get() = MultiFactor(android.multiFactor)
    actual val providerData: List<UserInfo>
        get() = android.providerData.map { UserInfo(it) }
    actual val providerId: String
        get() = android.providerId
    actual suspend fun delete() = android.delete().await().run { Unit }
    actual suspend fun reload() = android.reload().await().run { Unit }
    actual suspend fun getIdToken(forceRefresh: Boolean) = android.getIdToken(forceRefresh).await().run { Unit }
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(android.linkWithCredential(credential.android).await())
    actual suspend fun reauthenticate(credential: AuthCredential) = android.reauthenticate(credential.android).await().run { Unit }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(android.reauthenticateAndRetrieveData(credential.android).await())
    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) {
        val request = actionCodeSettings?.android?.let { android.sendEmailVerification(it) } ?: android.sendEmailVerification()
        request.await().run { Unit }
    }
    actual suspend fun unlink(provider: String): AuthResult = AuthResult(android.unlink(provider).await())
    actual suspend fun updateEmail(email: String) = android.updateEmail(email).await().run { Unit }
    actual suspend fun updatePassword(password: String) = android.updatePassword(password).await().run { Unit }
    actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) = android.updatePhoneNumber(credential.android).await().run { Unit }
    actual suspend fun updateProfile(request: UserProfileChangeRequest) = android.updateProfile(request.android).await().run { Unit }
    actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) = android.verifyBeforeUpdateEmail(newEmail, actionCodeSettings?.android).await().run { Unit }
}

actual class UserInfo(val android: com.google.firebase.auth.UserInfo) {
    actual val displayName: String?
        get() = android.displayName
    actual val email: String?
        get() = android.email
    actual val phoneNumber: String?
        get() = android.phoneNumber
    actual val photoURL: String?
        get() = android.photoUrl?.toString()
    actual val providerId: String
        get() = android.providerId
    actual val uid: String
        get() = android.uid
}

actual class MetaData(val android: com.google.firebase.auth.FirebaseUserMetadata) {
    actual val creationTime: Long?
        get() = android.creationTimestamp
    actual val lastSignInTime: Long?
        get() = android.lastSignInTimestamp
}

actual class UserProfileChangeRequest(val android: com.google.firebase.auth.UserProfileChangeRequest) {
    actual class Builder(val android: com.google.firebase.auth.UserProfileChangeRequest.Builder = com.google.firebase.auth.UserProfileChangeRequest.Builder()) {
        actual fun setDisplayName(displayName: String?): Builder = apply {
            android.setDisplayName(displayName)
        }
        actual fun setPhotoURL(photoURL: String?): Builder = apply {
            android.setPhotoUri(photoURL?.let { Uri.parse(it) })
        }
        actual fun build(): UserProfileChangeRequest = UserProfileChangeRequest(android.build())
    }
    actual val displayName: String?
        get() = android.displayName
    actual val photoURL: String?
        get() = android.photoUri?.toString()
}

actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
actual typealias FirebaseAuthMultiFactorException = com.google.firebase.auth.FirebaseAuthMultiFactorException
actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
actual typealias FirebaseAuthWeakPasswordException = com.google.firebase.auth.FirebaseAuthWeakPasswordException
actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException

actual object EmailAuthProvider {
    actual fun credentialWithEmail(
        email: String,
        password: String
    ): AuthCredential = AuthCredential(EmailAuthProvider.getCredential(email, password))
}

actual class MultiFactor(val android: com.google.firebase.auth.MultiFactor) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = android.enrolledFactors.map { MultiFactorInfo(it) }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = android.enroll(multiFactorAssertion.android, displayName).await().run { Unit }
    actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(android.session.await())
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = android.unenroll(multiFactorInfo.android).await().run { Unit }
    actual suspend fun unenroll(factorUid: String) = android.unenroll(factorUid).await().run { Unit }
}

actual class MultiFactorInfo(val android: com.google.firebase.auth.MultiFactorInfo) {
    actual val displayName: String?
        get() = android.displayName
    actual val enrollmentTime: Long
        get() = android.enrollmentTimestamp
    actual val factorId: String
        get() = android.factorId
    actual val uid: String
        get() = android.uid
}

actual class MultiFactorAssertion(val android: com.google.firebase.auth.MultiFactorAssertion) {
    actual val factorId: String
        get() = android.factorId
}

actual class MultiFactorSession(val android: com.google.firebase.auth.MultiFactorSession)

actual class ActionCodeSettings private constructor(val android: com.google.firebase.auth.ActionCodeSettings) {
    actual class Builder(val android: com.google.firebase.auth.ActionCodeSettings.Builder = com.google.firebase.auth.ActionCodeSettings.newBuilder()) {
        actual fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder = apply {
            android.setAndroidPackageName(androidPackageName, installIfNotAvailable, minimumVersion)
        }
        actual fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder = apply {
            android.setDynamicLinkDomain(dynamicLinkDomain)
        }
        actual fun setHandleCodeInApp(status: Boolean): Builder = apply {
            android.setHandleCodeInApp(status)
        }
        actual fun setIOSBundleId(iOSBundleId: String): Builder = apply {
            android.setIOSBundleId(iOSBundleId)
        }
        actual fun setUrl(url: String): Builder = apply {
            android.setUrl(url)
        }
        actual fun build(): ActionCodeSettings = ActionCodeSettings(android.build())
    }

    actual val canHandleCodeInApp: Boolean
        get() = android.canHandleCodeInApp()
    actual val androidInstallApp: Boolean
        get() = android.androidInstallApp
    actual val androidMinimumVersion: String?
        get() = android.androidMinimumVersion
    actual val androidPackageName: String?
        get() = android.androidPackageName
    actual val iOSBundle: String?
        get() = android.iosBundle
    actual val url: String
        get() = android.url
}