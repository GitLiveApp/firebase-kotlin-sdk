/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*
import platform.Foundation.NSURL

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
    actual suspend fun getIdToken(forceRefresh: Boolean): String?  = ios.awaitResult { getIDTokenForcingRefresh(forceRefresh, it) }
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(ios.awaitExpectedResult { linkWithCredential(credential.ios, it) })
    actual suspend fun reauthenticate(credential: AuthCredential) {
        val result: FIRAuthDataResult = ios.awaitExpectedResult { reauthenticateWithCredential(credential.ios, it) }
    }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(ios.awaitExpectedResult { reauthenticateWithCredential(credential.ios, it) })

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
    actual suspend fun updateProfile(buildRequest: UserProfileChangeRequest.Builder.() -> Unit) {
        val request = UserProfileChangeRequest.Builder(this.ios).apply(buildRequest).build()
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
