/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRMultiFactor
import cocoapods.FirebaseAuth.FIRMultiFactorAssertion
import cocoapods.FirebaseAuth.FIRMultiFactorInfo
import cocoapods.FirebaseAuth.FIRMultiFactorSession

actual class MultiFactor(val ios: FIRMultiFactor) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = ios.enrolledFactors.mapNotNull { info -> (info as? FIRMultiFactorInfo)?.let{  MultiFactorInfo(it) } }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = ios.await { enrollWithAssertion(multiFactorAssertion.ios, displayName, it) }.run { Unit }
    actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(ios.awaitResult { getSessionWithCompletion(completion = it) })
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = ios.await { unenrollWithInfo(multiFactorInfo.ios, it) }.run { Unit }
    actual suspend fun unenroll(factorUid: String) = ios.await { unenrollWithFactorUID(factorUid, it) }.run { Unit }
}

actual class MultiFactorInfo(val ios: FIRMultiFactorInfo) {
    actual val displayName: String?
        get() = ios.displayName
    actual val enrollmentTime: Long
        get() = ios.enrollmentDate.timeIntervalSinceReferenceDate.toLong()
    actual val factorId: String
        get() = ios.factorID
    actual val uid: String
        get() = ios.UID
}

actual class MultiFactorAssertion(val ios: FIRMultiFactorAssertion) {
    actual val factorId: String
        get() = ios.factorID
}

actual class MultiFactorSession(val ios: FIRMultiFactorSession)
