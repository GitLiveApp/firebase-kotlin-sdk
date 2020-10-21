/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlinx.coroutines.tasks.await

actual class MultiFactor(val android: com.google.firebase.auth.MultiFactor) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = android.enrolledFactors.map { MultiFactorInfo(it) }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = android.enroll(multiFactorAssertion.android, displayName).await().run { Unit }
    actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(android.session.await())
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = android.unenroll(multiFactorInfo.android).await().run { Unit }
    actual suspend fun unenroll(factorUid: String) = android.unenroll(factorUid).await().run { Unit }
}

actual class MultiFactorInfo(val android: com.google.firebase.auth.MultiFactorInfo) {
    actual val displayName: String?
        get() = android.displayName
    actual val enrollmentTime: Double
        get() = android.enrollmentTimestamp.toDouble()
    actual val factorId: String
        get() = android.factorId
    actual val uid: String
        get() = android.uid
}

actual class MultiFactorAssertion(val android: com.google.firebase.auth.MultiFactorAssertion) {
    actual val factorId: String
        get() = android.factorId
}

actual class MultiFactorSession(val android: com.google.firebase.auth.MultiFactorSession)

actual class MultiFactorResolver(val android: com.google.firebase.auth.MultiFactorResolver) {
    actual val auth: FirebaseAuth = FirebaseAuth(android.firebaseAuth)
    actual val hints: List<MultiFactorInfo> = android.hints.map { MultiFactorInfo(it) }
    actual val session: MultiFactorSession = MultiFactorSession(android.session)

    actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(android.resolveSignIn(assertion.android).await())
}