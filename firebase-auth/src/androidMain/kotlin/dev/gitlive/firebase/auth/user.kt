/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.net.Uri
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

public val FirebaseUser.android: com.google.firebase.auth.FirebaseUser get() = android

public actual class FirebaseUser internal constructor(internal val android: com.google.firebase.auth.FirebaseUser) {
    public actual val uid: String
        get() = android.uid
    public actual val displayName: String?
        get() = android.displayName
    public actual val email: String?
        get() = android.email
    public actual val phoneNumber: String?
        get() = android.phoneNumber
    public actual val photoURL: String?
        get() = android.photoUrl?.toString()
    public actual val isAnonymous: Boolean
        get() = android.isAnonymous
    public actual val isEmailVerified: Boolean
        get() = android.isEmailVerified
    public actual val metaData: UserMetaData?
        get() = android.metadata?.let { UserMetaData(it) }
    public actual val multiFactor: MultiFactor
        get() = MultiFactor(android.multiFactor)
    public actual val providerData: List<UserInfo>
        get() = android.providerData.map { UserInfo(it) }
    public actual val providerId: String
        get() = android.providerId
    public actual suspend fun delete() {
        android.delete().await()
    }
    public actual suspend fun reload() {
        android.reload().await()
    }
    public actual suspend fun getIdToken(forceRefresh: Boolean): String? = android.getIdToken(forceRefresh).await().token
    public actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = android.getIdToken(forceRefresh).await().run { AuthTokenResult(this) }
    public actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(android.linkWithCredential(credential.android).await())
    public actual suspend fun reauthenticate(credential: AuthCredential) {
        android.reauthenticate(credential.android).await()
    }
    public actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(android.reauthenticateAndRetrieveData(credential.android).await())
    public actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) {
        val request = actionCodeSettings?.let { android.sendEmailVerification(it.toAndroid()) } ?: android.sendEmailVerification()
        request.await()
    }
    public actual suspend fun unlink(provider: String): FirebaseUser? = android.unlink(provider).await().user?.let { FirebaseUser(it) }

    @Suppress("DEPRECATION")
    public actual suspend fun updateEmail(email: String) {
        android.updateEmail(email).await()
    }
    public actual suspend fun updatePassword(password: String) {
        android.updatePassword(password).await()
    }
    public actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) {
        android.updatePhoneNumber(credential.android).await()
    }
    public actual suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        val request = UserProfileChangeRequest.Builder()
            .apply { setDisplayName(displayName) }
            .apply { photoUri = photoUrl?.let { Uri.parse(it) } }
            .build()
        android.updateProfile(request).await()
    }
    public actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) {
        android.verifyBeforeUpdateEmail(newEmail, actionCodeSettings?.toAndroid()).await()
    }
}

public val UserInfo.android: com.google.firebase.auth.UserInfo get() = android

public actual class UserInfo(internal val android: com.google.firebase.auth.UserInfo) {
    public actual val displayName: String?
        get() = android.displayName
    public actual val email: String?
        get() = android.email
    public actual val phoneNumber: String?
        get() = android.phoneNumber
    public actual val photoURL: String?
        get() = android.photoUrl?.toString()
    public actual val providerId: String
        get() = android.providerId
    public actual val uid: String
        get() = android.uid
}

public val UserMetaData.android: com.google.firebase.auth.FirebaseUserMetadata get() = android

public actual class UserMetaData(internal val android: com.google.firebase.auth.FirebaseUserMetadata) {
    public actual val creationTime: Double?
        get() = android.creationTimestamp.toDouble()
    public actual val lastSignInTime: Double?
        get() = android.lastSignInTimestamp.toDouble()
}
