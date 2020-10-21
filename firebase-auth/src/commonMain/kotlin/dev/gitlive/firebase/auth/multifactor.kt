/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

expect class MultiFactor {
    val enrolledFactors: List<MultiFactorInfo>
    suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?)
    suspend fun getSession(): MultiFactorSession
    suspend fun unenroll(multiFactorInfo: MultiFactorInfo)
    suspend fun unenroll(factorUid: String)
}

expect class MultiFactorInfo {
    val displayName: String?
    val enrollmentTime: Double
    val factorId: String
    val uid: String
}

expect class MultiFactorAssertion {
    val factorId: String
}

expect class MultiFactorSession

expect class MultiFactorResolver {
    val auth: FirebaseAuth
    val hints: List<MultiFactorInfo>
    val session: MultiFactorSession

    suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult
}