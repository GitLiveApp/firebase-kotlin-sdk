/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

public actual interface UserInfo {
    public actual val uid: String
    public actual val providerId: String
    public actual val displayName: String?
    public actual val email: String?
    public actual val phoneNumber: String?
    public actual val isEmailVerified: Boolean

    /** The profile photo URL. */
    public val photoUrl: String?
}

internal class UserInfoImpl(
    override val uid: String,
    override val providerId: String,
    override val displayName: String?,
    override val email: String?,
    override val phoneNumber: String?,
    override val isEmailVerified: Boolean,
    override val photoUrl: String?,
) : UserInfo

internal class FirebaseUserMetadataImpl(override val creationTimestamp: Long, override val lastSignInTimestamp: Long) : FirebaseUserMetadata

internal class AuthResultImpl(override val user: FirebaseUser?, override val credential: AuthCredential?, override val additionalUserInfo: AdditionalUserInfo?) : AuthResult

internal class AdditionalUserInfoImpl(override val providerId: String?, override val username: String?, override val profile: Map<String, Any?>?, override val isNewUser: Boolean) : AdditionalUserInfo

internal class SignInMethodQueryResultImpl(override val signInMethods: List<String>?) : SignInMethodQueryResult

public actual abstract class FirebaseAuthSettings actual constructor() {
    public actual abstract fun setAppVerificationDisabledForTesting(disabled: Boolean)
}

/**
 * A pure Kotlin value: only the fields that were set are updated.
 *
 * @property displayName The display name to set, when [displayNameSet].
 * @property photoUri The photo URL to set, when [photoUriSet].
 */
public actual class UserProfileChangeRequest internal constructor(
    public actual val displayName: String?,
    internal val displayNameSet: Boolean,
    public val photoUri: String?,
    internal val photoUriSet: Boolean,
) {
    public actual class Builder actual constructor() {
        private var displayName: String? = null
        private var displayNameSet = false
        private var photoUri: String? = null
        private var photoUriSet = false

        public actual fun setDisplayName(displayName: String?): Builder = apply {
            this.displayName = displayName
            displayNameSet = true
        }

        /** Sets the photo URL, or clears it when null. */
        public fun setPhotoUri(photoUri: String?): Builder = apply {
            this.photoUri = photoUri
            photoUriSet = true
        }

        public actual fun build(): UserProfileChangeRequest = UserProfileChangeRequest(displayName, displayNameSet, photoUri, photoUriSet)
    }
}
