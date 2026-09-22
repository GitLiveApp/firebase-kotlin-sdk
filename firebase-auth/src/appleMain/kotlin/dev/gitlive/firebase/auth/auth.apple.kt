/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthCredential
import cocoapods.FirebaseAuth.FIRAuthUIDelegateProtocol
import cocoapods.FirebaseAuth.FIROAuthProvider
import cocoapods.FirebaseAuth.FIRUser
import com.google.firebase.auth.ios as compatIos
import com.google.firebase.auth.FirebaseAuth as CompatFirebaseAuth

/** The underlying Firebase iOS SDK object. */
public val FirebaseAuth.ios: FIRAuth get() = compat.ios

/** The [FirebaseAuth] of the iOS SDK's [ios]. */
public fun FirebaseAuth(ios: FIRAuth): FirebaseAuth = FirebaseAuth(CompatFirebaseAuth.wrap(ios))

public val FirebaseUser.ios: FIRUser get() = compat.ios

public val AuthCredential.ios: FIRAuthCredential get() = compat.compatIos

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
