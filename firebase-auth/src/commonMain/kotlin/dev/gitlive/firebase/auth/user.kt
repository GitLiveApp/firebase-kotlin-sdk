/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

public expect class FirebaseUser {
    public val uid: String
    public val displayName: String?
    public val email: String?
    public val phoneNumber: String?
    public val photoURL: String?
    public val isAnonymous: Boolean
    public val isEmailVerified: Boolean
    public val metaData: UserMetaData?
    public val multiFactor: MultiFactor
    public val providerData: List<UserInfo>
    public val providerId: String
    public suspend fun delete()
    public suspend fun reload()
    public suspend fun getIdToken(forceRefresh: Boolean): String?
    public suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult
    public suspend fun linkWithCredential(credential: AuthCredential): AuthResult
    public suspend fun reauthenticate(credential: AuthCredential)
    public suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult
    public suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings? = null)
    public suspend fun unlink(provider: String): FirebaseUser?

    @Deprecated("Use verifyBeforeUpdateEmail instead", replaceWith = ReplaceWith("verifyBeforeUpdateEmail(email)"))
    public suspend fun updateEmail(email: String)
    public suspend fun updatePassword(password: String)
    public suspend fun updatePhoneNumber(credential: PhoneAuthCredential)
    public suspend fun updateProfile(displayName: String? = this.displayName, photoUrl: String? = this.photoURL)
    public suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings? = null)
}

public expect class UserInfo {
    public val displayName: String?
    public val email: String?
    public val phoneNumber: String?
    public val photoURL: String?
    public val providerId: String
    public val uid: String
}

public expect class UserMetaData {
    public val creationTime: Double?
    public val lastSignInTime: Double?
}
