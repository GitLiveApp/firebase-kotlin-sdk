/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.net.Uri
import kotlinx.coroutines.tasks.await

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
    actual suspend fun getIdToken(forceRefresh: Boolean) = android.getIdToken(forceRefresh).await().token
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(android.linkWithCredential(credential.android).await())
    actual suspend fun reauthenticate(credential: AuthCredential) = android.reauthenticate(credential.android).await().run { Unit }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(android.reauthenticateAndRetrieveData(credential.android).await())
    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) {
        val request = actionCodeSettings?.android?.let { android.sendEmailVerification(it) } ?: android.sendEmailVerification()
        request.await().run { Unit }
    }
    actual suspend fun unlink(provider: String): FirebaseUser? = android.unlink(provider).await().user?.let { FirebaseUser(it) }
    actual suspend fun updateEmail(email: String) = android.updateEmail(email).await().run { Unit }
    actual suspend fun updatePassword(password: String) = android.updatePassword(password).await().run { Unit }
    actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) = android.updatePhoneNumber(credential.android).await().run { Unit }
    actual suspend fun updateProfile(buildRequest: UserProfileChangeRequest.Builder.() -> Unit) {
        val request = UserProfileChangeRequest.Builder().apply(buildRequest).build()
        android.updateProfile(request.android).await()
    }
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
