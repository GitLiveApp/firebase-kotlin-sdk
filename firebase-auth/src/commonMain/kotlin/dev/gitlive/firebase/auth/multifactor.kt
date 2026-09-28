/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.MultiFactor as CompatMultiFactor
import com.google.firebase.auth.MultiFactorAssertion as CompatMultiFactorAssertion
import com.google.firebase.auth.MultiFactorInfo as CompatMultiFactorInfo
import com.google.firebase.auth.MultiFactorResolver as CompatMultiFactorResolver
import com.google.firebase.auth.MultiFactorSession as CompatMultiFactorSession

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.MultiFactor] this wraps. */
public class MultiFactor internal constructor(public val compat: CompatMultiFactor) {
    public val enrolledFactors: List<MultiFactorInfo>
        get() = compat.enrolledFactors.map { MultiFactorInfo(it) }
    public suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) {
        compat.enroll(multiFactorAssertion.compat, displayName).await()
    }
    public suspend fun getSession(): MultiFactorSession = MultiFactorSession(compat.getSession().await())
    public suspend fun unenroll(multiFactorInfo: MultiFactorInfo) {
        compat.unenroll(multiFactorInfo.compat).await()
    }
    public suspend fun unenroll(factorUid: String) {
        compat.unenroll(factorUid).await()
    }
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.MultiFactorInfo] this wraps. */
public class MultiFactorInfo internal constructor(public val compat: CompatMultiFactorInfo) {
    public val displayName: String?
        get() = compat.displayName

    /** When the factor was enrolled, in milliseconds since the epoch. */
    public val enrollmentTime: Double
        get() = compat.enrollmentTimestamp.toDouble()
    public val factorId: String
        get() = compat.factorId
    public val uid: String
        get() = compat.uid
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.MultiFactorAssertion] this wraps. */
public class MultiFactorAssertion internal constructor(public val compat: CompatMultiFactorAssertion) {
    public val factorId: String
        get() = compat.factorId
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.MultiFactorSession] this wraps. */
public class MultiFactorSession internal constructor(public val compat: CompatMultiFactorSession)

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.MultiFactorResolver] this wraps. */
public class MultiFactorResolver internal constructor(public val compat: CompatMultiFactorResolver) {
    public val auth: FirebaseAuth get() = FirebaseAuth(compat.firebaseAuth)
    public val hints: List<MultiFactorInfo> get() = compat.hints.map { MultiFactorInfo(it) }
    public val session: MultiFactorSession get() = MultiFactorSession(compat.session)

    public suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = AuthResult(compat.resolveSignIn(assertion.compat).await())
}
