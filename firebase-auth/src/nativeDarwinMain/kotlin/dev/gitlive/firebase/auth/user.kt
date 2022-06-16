/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*
import platform.Foundation.NSURL

actual class FirebaseUser internal constructor(val native: FIRUser) {
    actual val uid: String
        get() = native.uid
    actual val displayName: String?
        get() = native.displayName
    actual val email: String?
        get() = native.email
    actual val phoneNumber: String?
        get() = native.phoneNumber
    actual val photoURL: String?
        get() = native.photoURL?.absoluteString
    actual val isAnonymous: Boolean
        get() = native.anonymous
    actual val isEmailVerified: Boolean
        get() = native.emailVerified
    actual val metaData: UserMetaData?
        get() = UserMetaData(native.metadata)
    actual val providerData: List<UserInfo>
        get() = native.providerData.mapNotNull { provider -> (provider as? FIRUserInfoProtocol)?.let { UserInfo(it) } }
    actual val providerId: String
        get() = native.providerID

    actual suspend fun delete() = native.await { deleteWithCompletion(it) }.run { Unit }

    actual suspend fun reload() = native.await { reloadWithCompletion(it) }.run { Unit }

    actual suspend fun getIdToken(forceRefresh: Boolean): String? =
        native.awaitResult { getIDTokenForcingRefresh(forceRefresh, it) }

    actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult =
        AuthTokenResult(native.awaitResult { getIDTokenResultForcingRefresh(forceRefresh, it) })

    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult =
        AuthResult(native.awaitResult { linkWithCredential(credential.native, it) })

    actual suspend fun reauthenticate(credential: AuthCredential) =
        native.awaitResult<FIRUser, FIRAuthDataResult?> { reauthenticateWithCredential(credential.native, it) }.run { Unit }

    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult =
        AuthResult(native.awaitResult { reauthenticateWithCredential(credential.native, it) })

    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) = native.await {
        actionCodeSettings?.let { settings -> sendEmailVerificationWithActionCodeSettings(settings.toNative(), it) }
            ?: sendEmailVerificationWithCompletion(it)
    }

    actual suspend fun unlink(provider: String): FirebaseUser? {
        val user: FIRUser? = native.awaitResult { unlinkFromProvider(provider, it) }
        return user?.let {
            FirebaseUser(it)
        }
    }
    actual suspend fun updateEmail(email: String) = native.await { updateEmail(email, it) }.run { Unit }
    actual suspend fun updatePassword(password: String) = native.await { updatePassword(password, it) }.run { Unit }
    actual suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        val request = native.profileChangeRequest().apply {
            this.displayName = displayName
            this.photoURL = photoUrl?.let { NSURL.URLWithString(it) }
        }
        native.await { request.commitChangesWithCompletion(it) }
    }
    actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) = native.await {
        actionCodeSettings?.let { actionSettings -> sendEmailVerificationBeforeUpdatingEmail(newEmail, actionSettings.toNative(), it) } ?: sendEmailVerificationBeforeUpdatingEmail(newEmail, it)
    }.run { Unit }
}

actual class UserInfo(val native: FIRUserInfoProtocol) {
    actual val displayName: String?
        get() = native.displayName
    actual val email: String?
        get() = native.email
    actual val phoneNumber: String?
        get() = native.phoneNumber
    actual val photoURL: String?
        get() = native.photoURL?.absoluteString
    actual val providerId: String
        get() = native.providerID
    actual val uid: String
        get() = native.uid
}

actual class UserMetaData(val native: FIRUserMetadata) {
    actual val creationTime: Double?
        get() = native.creationDate?.timeIntervalSinceReferenceDate?.toDouble()
    actual val lastSignInTime: Double?
        get() = native.lastSignInDate?.timeIntervalSinceReferenceDate?.toDouble()
}
