/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.AdditionalUserInfoImpl
import com.google.firebase.auth.AuthResultImpl
import com.google.firebase.auth.FirebaseUserImpl
import com.google.firebase.auth.FirebaseUserMetadataImpl
import com.google.firebase.auth.NativeGetTokenResult
import com.google.firebase.auth.UserInfoImpl
import com.google.firebase.auth.toCompat
import swiftPMImport.dev.gitlive.firebase.auth.FIRAdditionalUserInfo
import swiftPMImport.dev.gitlive.firebase.auth.FIRAuth
import swiftPMImport.dev.gitlive.firebase.auth.FIRAuthCredential
import swiftPMImport.dev.gitlive.firebase.auth.FIRAuthDataResult
import swiftPMImport.dev.gitlive.firebase.auth.FIRAuthTokenResult
import swiftPMImport.dev.gitlive.firebase.auth.FIRAuthUIDelegateProtocol
import swiftPMImport.dev.gitlive.firebase.auth.FIROAuthCredential
import swiftPMImport.dev.gitlive.firebase.auth.FIROAuthProvider
import swiftPMImport.dev.gitlive.firebase.auth.FIRPhoneAuthCredential
import swiftPMImport.dev.gitlive.firebase.auth.FIRUser
import swiftPMImport.dev.gitlive.firebase.auth.FIRUserInfoProtocol
import swiftPMImport.dev.gitlive.firebase.auth.FIRUserMetadata
import com.google.firebase.auth.ios as compatIos
import com.google.firebase.auth.FirebaseAuth as CompatFirebaseAuth
import com.google.firebase.auth.OAuthCredential as CompatOAuthCredential
import com.google.firebase.auth.PhoneAuthCredential as CompatPhoneAuthCredential

/** The underlying Firebase iOS SDK object. */
public val FirebaseAuth.ios: FIRAuth get() = compat.ios

/** The [FirebaseAuth] of the iOS SDK's [ios]. */
public fun FirebaseAuth(ios: FIRAuth): FirebaseAuth = FirebaseAuth(CompatFirebaseAuth.wrap(ios))

public val FirebaseUser.ios: FIRUser get() = compat.ios

public val UserInfo.ios: FIRUserInfoProtocol
    get() = when (val info = compat) {
        is FirebaseUserImpl -> info.ios
        else -> (info as UserInfoImpl).native as FIRUserInfoProtocol
    }

public val UserMetaData.ios: FIRUserMetadata get() = (compat as FirebaseUserMetadataImpl).native as FIRUserMetadata

/** The iOS SDK's result; the result of `unlink`, which the SDK reports as a user, has none. */
public val AuthResult.ios: FIRAuthDataResult get() = checkNotNull((compat as AuthResultImpl).native) { "unlink has no FIRAuthDataResult" } as FIRAuthDataResult

public val AdditionalUserInfo.ios: FIRAdditionalUserInfo get() = (compat as AdditionalUserInfoImpl).native as FIRAdditionalUserInfo

public val AuthTokenResult.ios: FIRAuthTokenResult get() = (compat as NativeGetTokenResult).native as FIRAuthTokenResult

public val AuthCredential.ios: FIRAuthCredential get() = compat.compatIos

public val PhoneAuthCredential.ios: FIRPhoneAuthCredential get() = compat.compatIos as FIRPhoneAuthCredential

public val OAuthCredential.ios: FIROAuthCredential get() = compat.compatIos as FIROAuthCredential

/** The [AuthCredential] of the iOS SDK's [ios], e.g. one obtained from a provider SDK. */
public fun AuthCredential(ios: FIRAuthCredential): AuthCredential = AuthCredential(ios.toCompat())

public fun PhoneAuthCredential(ios: FIRPhoneAuthCredential): PhoneAuthCredential = PhoneAuthCredential(ios.toCompat() as CompatPhoneAuthCredential)

public fun OAuthCredential(ios: FIROAuthCredential): OAuthCredential = OAuthCredential(ios.toCompat() as CompatOAuthCredential)

/** The iOS SDK's provider configured as this one. */
public val OAuthProvider.ios: FIROAuthProvider
    get() = FIROAuthProvider.providerWithProviderID(compat.providerId, compat.firebaseAuth.ios).apply {
        setScopes(compat.scopes)
        @Suppress("UNCHECKED_CAST")
        setCustomParameters(compat.customParameters as Map<Any?, *>)
    }

public actual interface PhoneVerificationProvider {
    public val delegate: FIRAuthUIDelegateProtocol?
    public suspend fun getVerificationCode(): String
}
