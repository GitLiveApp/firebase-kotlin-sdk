/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import android.net.Uri

public actual interface UserInfo {
    public actual val uid: String
    public actual val providerId: String
    public actual val displayName: String?
    public actual val email: String?
    public actual val phoneNumber: String?
    public actual val isEmailVerified: Boolean
    public actual val photoUrl: Uri?
}

internal class UserInfoImpl(
    override val uid: String,
    override val providerId: String,
    override val displayName: String?,
    override val email: String?,
    override val phoneNumber: String?,
    override val isEmailVerified: Boolean,
    override val photoUrl: Uri?,
    /** The platform SDK's object, for the wrapper's platform accessors. */
    val native: Any? = null,
) : UserInfo

internal class FirebaseUserMetadataImpl(override val creationTimestamp: Long, override val lastSignInTimestamp: Long, val native: Any? = null) : FirebaseUserMetadata

/** @property native The platform SDK's result, null for the result of `unlink`, which the SDKs report as a user. */
internal class AuthResultImpl(override val user: FirebaseUser?, override val credential: AuthCredential?, override val additionalUserInfo: AdditionalUserInfo?, val native: Any? = null) : AuthResult

internal class AdditionalUserInfoImpl(override val providerId: String?, override val username: String?, override val profile: Map<String, Any?>?, override val isNewUser: Boolean, val native: Any? = null) : AdditionalUserInfo

/** A token result keeping the platform SDK's, for the wrapper's platform accessors. */
internal class NativeGetTokenResult(token: String?, claims: Map<String, Any?>, val native: Any) : GetTokenResult(token, claims)

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
    public actual val photoUri: Uri?,
    internal val photoUriSet: Boolean,
) {
    public actual class Builder actual constructor() {
        private var displayName: String? = null
        private var displayNameSet = false
        private var photoUri: Uri? = null
        private var photoUriSet = false

        public actual fun setDisplayName(displayName: String?): Builder = apply {
            this.displayName = displayName
            displayNameSet = true
        }

        public actual fun setPhotoUri(photoUri: Uri?): Builder = apply {
            this.photoUri = photoUri
            photoUriSet = true
        }

        public actual fun build(): UserProfileChangeRequest = UserProfileChangeRequest(displayName, displayNameSet, photoUri, photoUriSet)
    }
}
