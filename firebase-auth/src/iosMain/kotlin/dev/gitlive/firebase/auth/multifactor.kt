/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

class MultiFactor(val ios: FIRMultiFactor) {
    val enrolledFactors: List<MultiFactorInfo>
        get() = ios.enrolledFactors.mapNotNull { info -> (info as? FIRMultiFactorInfo)?.let{  MultiFactorInfo(it) } }
    suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = ios.await { enrollWithAssertion(multiFactorAssertion.ios, displayName, it) }.run { Unit }
    suspend fun getSession(): MultiFactorSession = MultiFactorSession(ios.awaitResult { getSessionWithCompletion(completion = it) })
    suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = ios.await { unenrollWithInfo(multiFactorInfo.ios, it) }.run { Unit }
    suspend fun unenroll(factorUid: String) = ios.await { unenrollWithFactorUID(factorUid, it) }.run { Unit }
}

class MultiFactorInfo(val ios: FIRMultiFactorInfo) {
    val displayName: String?
        get() = ios.displayName
    val enrollmentTime: Double
        get() = ios.enrollmentDate.timeIntervalSinceReferenceDate.toDouble()
    val factorId: String
        get() = ios.factorID
    val uid: String
        get() = ios.UID
}

class MultiFactorAssertion(val ios: FIRMultiFactorAssertion) {
    val factorId: String
        get() = ios.factorID
}

class MultiFactorSession(val ios: FIRMultiFactorSession)

class MultiFactorResolver(val ios: FIRMultiFactorResolver) {
    val auth: FirebaseAuth = FirebaseAuth(ios.auth)
    val hints: List<MultiFactorInfo> = ios.hints.mapNotNull { hint -> (hint as? FIRMultiFactorInfo)?.let { MultiFactorInfo(it) } }
    val session: MultiFactorSession = MultiFactorSession(ios.session)

    suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(ios.awaitResult { resolveSignInWithAssertion(assertion.ios, it) })
}