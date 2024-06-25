/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

public actual class MultiFactor(public val ios: FIRMultiFactor) {
    public actual val enrolledFactors: List<MultiFactorInfo>
        get() = ios.enrolledFactors.mapNotNull { info -> (info as? FIRMultiFactorInfo)?.let { MultiFactorInfo(it) } }
    public actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?): Unit = ios.await { enrollWithAssertion(multiFactorAssertion.ios, displayName, it) }
    public actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(ios.awaitResult { getSessionWithCompletion(completion = it) })
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit = ios.await { unenrollWithInfo(multiFactorInfo.ios, it) }
    public actual suspend fun unenroll(factorUid: String): Unit = ios.await { unenrollWithFactorUID(factorUid, it) }
}

public actual class MultiFactorInfo(public val ios: FIRMultiFactorInfo) {
    public actual val displayName: String?
        get() = ios.displayName
    public actual val enrollmentTime: Double
        get() = ios.enrollmentDate.timeIntervalSinceReferenceDate
    public actual val factorId: String
        get() = ios.factorID
    public actual val uid: String
        get() = ios.UID
}

public actual class MultiFactorAssertion(public val ios: FIRMultiFactorAssertion) {
    public actual val factorId: String
        get() = ios.factorID
}

public actual class MultiFactorSession(public val ios: FIRMultiFactorSession)

public actual class MultiFactorResolver(public val ios: FIRMultiFactorResolver) {
    public actual val auth: FirebaseAuth = FirebaseAuth(ios.auth)
    public actual val hints: List<MultiFactorInfo> = ios.hints.mapNotNull { hint -> (hint as? FIRMultiFactorInfo)?.let { MultiFactorInfo(it) } }
    public actual val session: MultiFactorSession = MultiFactorSession(ios.session)

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(ios.awaitResult { resolveSignInWithAssertion(assertion.ios, it) })
}
