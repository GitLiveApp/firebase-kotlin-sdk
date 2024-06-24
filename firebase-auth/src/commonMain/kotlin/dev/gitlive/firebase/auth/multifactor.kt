/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

public expect class MultiFactor {
    public val enrolledFactors: List<MultiFactorInfo>
    public suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?)
    public suspend fun getSession(): MultiFactorSession
    public suspend fun unenroll(multiFactorInfo: MultiFactorInfo)
    public suspend fun unenroll(factorUid: String)
}

public expect class MultiFactorInfo {
    public val displayName: String?
    public val enrollmentTime: Double
    public val factorId: String
    public val uid: String
}

public expect class MultiFactorAssertion {
    public val factorId: String
}

public expect class MultiFactorSession

public expect class MultiFactorResolver {
    public val auth: FirebaseAuth
    public val hints: List<MultiFactorInfo>
    public val session: MultiFactorSession

    public suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult
}
