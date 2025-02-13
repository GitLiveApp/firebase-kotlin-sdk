/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

public val MultiFactor.ios: FIRMultiFactor get() = ios

public actual class MultiFactor(internal val ios: FIRMultiFactor) {
    public actual val enrolledFactors: List<MultiFactorInfo>
        get() = ios.enrolledFactors().mapNotNull { info -> (info as? FIRMultiFactorInfo)?.let { MultiFactorInfo(it) } }
    public actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?): Unit = ios.await { enrollWithAssertion(multiFactorAssertion.ios, displayName, it) }
    public actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(ios.awaitResult { getSessionWithCompletion(completion = it) })
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit = ios.await { unenrollWithInfo(multiFactorInfo.ios, it) }
    public actual suspend fun unenroll(factorUid: String): Unit = ios.await { unenrollWithFactorUID(factorUid, it) }
}

public val MultiFactorInfo.ios: FIRMultiFactorInfo get() = ios

public actual class MultiFactorInfo(internal val ios: FIRMultiFactorInfo) {
    public actual val displayName: String?
        get() = ios.displayName()
    public actual val enrollmentTime: Double
        get() = ios.enrollmentDate().timeIntervalSinceReferenceDate
    public actual val factorId: String
        get() = ios.factorID()
    public actual val uid: String
        get() = ios.UID()
}

public val MultiFactorAssertion.ios: FIRMultiFactorAssertion get() = ios

public actual class MultiFactorAssertion(internal val ios: FIRMultiFactorAssertion) {
    public actual val factorId: String
        get() = ios.factorID()
}

public val MultiFactorSession.ios: FIRMultiFactorSession get() = ios

public actual class MultiFactorSession(internal val ios: FIRMultiFactorSession)

public val MultiFactorResolver.ios: FIRMultiFactorResolver get() = ios

public actual class MultiFactorResolver(internal val ios: FIRMultiFactorResolver) {
    public actual val auth: FirebaseAuth = FirebaseAuth(ios.auth())
    public actual val hints: List<MultiFactorInfo> = ios.hints().mapNotNull { hint -> (hint as? FIRMultiFactorInfo)?.let { MultiFactorInfo(it) } }
    public actual val session: MultiFactorSession = MultiFactorSession(ios.session())

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(ios.awaitResult { resolveSignInWithAssertion(assertion.ios, it) })
}
