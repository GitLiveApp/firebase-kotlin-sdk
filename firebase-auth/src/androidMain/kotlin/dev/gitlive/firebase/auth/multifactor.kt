/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlinx.coroutines.tasks.await

class MultiFactor(val android: com.google.firebase.auth.MultiFactor) {
    val enrolledFactors: List<MultiFactorInfo>
        get() = android.enrolledFactors.map { MultiFactorInfo(it) }
    suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) = android.enroll(multiFactorAssertion.android, displayName).await().run { Unit }
    suspend fun getSession(): MultiFactorSession = MultiFactorSession(android.session.await())
    suspend fun unenroll(multiFactorInfo: MultiFactorInfo) = android.unenroll(multiFactorInfo.android).await().run { Unit }
    suspend fun unenroll(factorUid: String) = android.unenroll(factorUid).await().run { Unit }
}

class MultiFactorInfo(val android: com.google.firebase.auth.MultiFactorInfo) {
    val displayName: String?
        get() = android.displayName
    val enrollmentTime: Double
        get() = android.enrollmentTimestamp.toDouble()
    val factorId: String
        get() = android.factorId
    val uid: String
        get() = android.uid
}

class MultiFactorAssertion(val android: com.google.firebase.auth.MultiFactorAssertion) {
    val factorId: String
        get() = android.factorId
}

class MultiFactorSession(val android: com.google.firebase.auth.MultiFactorSession)

class MultiFactorResolver(val android: com.google.firebase.auth.MultiFactorResolver) {
    val auth: FirebaseAuth = FirebaseAuth(android.firebaseAuth)
    val hints: List<MultiFactorInfo> = android.hints.map { MultiFactorInfo(it) }
    val session: MultiFactorSession = MultiFactorSession(android.session)

    suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(android.resolveSignIn(assertion.android).await())
}