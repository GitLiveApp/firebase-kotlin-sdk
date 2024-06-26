/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlinx.coroutines.tasks.await

public actual class MultiFactor(public val android: com.google.firebase.auth.MultiFactor) {
    public actual val enrolledFactors: List<MultiFactorInfo>
        get() = android.enrolledFactors.map { MultiFactorInfo(it) }
    public actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) {
        android.enroll(multiFactorAssertion.android, displayName).await()
    }
    public actual suspend fun getSession(): MultiFactorSession = MultiFactorSession(android.session.await())
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) {
        android.unenroll(multiFactorInfo.android).await()
    }
    public actual suspend fun unenroll(factorUid: String) {
        android.unenroll(factorUid).await()
    }
}

public actual class MultiFactorInfo(public val android: com.google.firebase.auth.MultiFactorInfo) {
    public actual val displayName: String?
        get() = android.displayName
    public actual val enrollmentTime: Double
        get() = android.enrollmentTimestamp.toDouble()
    public actual val factorId: String
        get() = android.factorId
    public actual val uid: String
        get() = android.uid
}

public actual class MultiFactorAssertion(public val android: com.google.firebase.auth.MultiFactorAssertion) {
    public actual val factorId: String
        get() = android.factorId
}

public actual class MultiFactorSession(public val android: com.google.firebase.auth.MultiFactorSession)

public actual class MultiFactorResolver(public val android: com.google.firebase.auth.MultiFactorResolver) {
    public actual val auth: FirebaseAuth = FirebaseAuth(android.firebaseAuth)
    public actual val hints: List<MultiFactorInfo> = android.hints.map { MultiFactorInfo(it) }
    public actual val session: MultiFactorSession = MultiFactorSession(android.session)

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(android.resolveSignIn(assertion.android).await())
}
