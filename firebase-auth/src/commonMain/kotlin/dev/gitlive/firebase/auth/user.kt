/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

expect class FirebaseUser {
    val uid: String
    val displayName: String?
    val email: String?
    val phoneNumber: String?
    val photoURL: String?
    val isAnonymous: Boolean
    val isEmailVerified: Boolean
    val metaData: MetaData?
    val multiFactor: MultiFactor
    val providerData: List<UserInfo>
    val providerId: String
    suspend fun delete()
    suspend fun reload()
    suspend fun getIdToken(forceRefresh: Boolean): String?
    suspend fun linkWithCredential(credential: AuthCredential): AuthResult
    suspend fun reauthenticate(credential: AuthCredential)
    suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult
    suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings? = null)
    suspend fun unlink(provider: String): FirebaseUser?
    suspend fun updateEmail(email: String)
    suspend fun updatePassword(password: String)
    suspend fun updatePhoneNumber(credential: PhoneAuthCredential)
    suspend fun updateProfile(buildRequest: UserProfileChangeRequest.Builder.() -> Unit)
    suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings? = null)
}

expect class UserInfo {
    val displayName: String?
    val email: String?
    val phoneNumber: String?
    val photoURL: String?
    val providerId: String
    val uid: String
}

expect class MetaData {
    val creationTime: Long?
    val lastSignInTime: Long?
}

expect class UserProfileChangeRequest {
    class Builder {
        fun setDisplayName(displayName: String?): Builder
        fun setPhotoURL(photoURL: String?): Builder
        fun build(): UserProfileChangeRequest
    }
    val displayName: String?
    val photoURL: String?
}
