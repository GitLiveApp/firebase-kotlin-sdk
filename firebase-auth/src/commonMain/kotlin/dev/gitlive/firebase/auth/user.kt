/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.internal.photoUrlString
import dev.gitlive.firebase.auth.internal.setPhotoUrlString
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseUser as CompatFirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata as CompatFirebaseUserMetadata
import com.google.firebase.auth.UserInfo as CompatUserInfo
import com.google.firebase.auth.UserProfileChangeRequest as CompatUserProfileChangeRequest

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.FirebaseUser] this user wraps. */
public class FirebaseUser internal constructor(public val compat: CompatFirebaseUser) {
    public val uid: String
        get() = compat.uid
    public val displayName: String?
        get() = compat.displayName
    public val email: String?
        get() = compat.email
    public val phoneNumber: String?
        get() = compat.phoneNumber
    public val photoURL: String?
        get() = compat.photoUrlString()
    public val isAnonymous: Boolean
        get() = compat.isAnonymous
    public val isEmailVerified: Boolean
        get() = compat.isEmailVerified
    public val metaData: UserMetaData?
        get() = compat.metadata?.let { UserMetaData(it) }
    public val multiFactor: MultiFactor
        get() = MultiFactor(compat.multiFactor)
    public val providerData: List<UserInfo>
        get() = compat.providerData.map { UserInfo(it) }
    public val providerId: String
        get() = compat.providerId
    public suspend fun delete() {
        compat.delete().await()
    }
    public suspend fun reload() {
        compat.reload().await()
    }
    public suspend fun getIdToken(forceRefresh: Boolean): String? = compat.getIdToken(forceRefresh).await().token
    public suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = AuthTokenResult(compat.getIdToken(forceRefresh).await())
    public suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(compat.linkWithCredential(credential.compat).await())
    public suspend fun reauthenticate(credential: AuthCredential) {
        compat.reauthenticate(credential.compat).await()
    }
    public suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(compat.reauthenticateAndRetrieveData(credential.compat).await())
    public suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings? = null) {
        compat.sendEmailVerification(actionCodeSettings?.toCompat()).await()
    }
    public suspend fun unlink(provider: String): FirebaseUser? = compat.unlink(provider).await().user?.let { FirebaseUser(it) }

    @Deprecated("Use verifyBeforeUpdateEmail instead", replaceWith = ReplaceWith("verifyBeforeUpdateEmail(email)"))
    @Suppress("DEPRECATION")
    public suspend fun updateEmail(email: String) {
        compat.updateEmail(email).await()
    }
    public suspend fun updatePassword(password: String) {
        compat.updatePassword(password).await()
    }
    public suspend fun updatePhoneNumber(credential: PhoneAuthCredential) {
        compat.updatePhoneNumber(credential.compat).await()
    }
    public suspend fun updateProfile(displayName: String? = this.displayName, photoUrl: String? = this.photoURL) {
        val request = CompatUserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUrlString(photoUrl)
            .build()
        compat.updateProfile(request).await()
    }
    public suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings? = null) {
        compat.verifyBeforeUpdateEmail(newEmail, actionCodeSettings?.toCompat()).await()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseUser && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseUser(uid=$uid)"
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.UserInfo] this wraps. */
public class UserInfo internal constructor(public val compat: CompatUserInfo) {
    public val displayName: String?
        get() = compat.displayName
    public val email: String?
        get() = compat.email
    public val phoneNumber: String?
        get() = compat.phoneNumber
    public val photoURL: String?
        get() = compat.photoUrlString()
    public val providerId: String
        get() = compat.providerId
    public val uid: String
        get() = compat.uid
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.FirebaseUserMetadata] this wraps. */
public class UserMetaData internal constructor(public val compat: CompatFirebaseUserMetadata) {
    /** When the account was created, in milliseconds since the epoch, or null when unknown. */
    public val creationTime: Double?
        get() = compat.creationTimestamp.takeIf { it != 0L }?.toDouble()

    /** When the account last signed in, in milliseconds since the epoch, or null when unknown. */
    public val lastSignInTime: Double?
        get() = compat.lastSignInTimestamp.takeIf { it != 0L }?.toDouble()
}
