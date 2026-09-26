/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.android.gms.tasks.Task

public actual abstract class MultiFactor actual constructor() {
    public actual abstract val enrolledFactors: List<MultiFactorInfo>
    public actual abstract fun enroll(assertion: MultiFactorAssertion, displayName: String?): Task<Nothing?>
    public actual abstract fun getSession(): Task<MultiFactorSession>
    public actual abstract fun unenroll(info: MultiFactorInfo): Task<Nothing?>
    public actual abstract fun unenroll(factorUid: String): Task<Nothing?>
}

/** @property native The platform SDK's assertion. */
public actual abstract class MultiFactorAssertion actual constructor() {
    public actual abstract val factorId: String
    internal abstract val native: Any
}

/** @property native The platform SDK's info, when the SDK produced it. */
public actual abstract class MultiFactorInfo actual constructor() {
    public actual abstract val displayName: String?
    public actual abstract val enrollmentTimestamp: Long
    public actual abstract val factorId: String
    public actual abstract val uid: String
    internal abstract val native: Any?

    public actual companion object {
        public actual val FACTOR_ID_KEY: String = "factorIdKey"
    }
}

public actual class PhoneMultiFactorInfo actual constructor(
    override val uid: String,
    override val displayName: String?,
    override val enrollmentTimestamp: Long,
    public actual val phoneNumber: String,
) : MultiFactorInfo() {
    override val factorId: String get() = PhoneMultiFactorGenerator.FACTOR_ID
    override var native: Any? = null
        internal set

    internal constructor(uid: String, displayName: String?, enrollmentTimestamp: Long, phoneNumber: String, native: Any) : this(uid, displayName, enrollmentTimestamp, phoneNumber) {
        this.native = native
    }
}

public actual class TotpMultiFactorInfo internal constructor(
    override val uid: String,
    override val displayName: String?,
    override val enrollmentTimestamp: Long,
    override val native: Any,
) : MultiFactorInfo() {
    override val factorId: String get() = TotpMultiFactorGenerator.FACTOR_ID
}

public actual abstract class MultiFactorSession actual constructor()

internal class MultiFactorSessionImpl(val native: Any) : MultiFactorSession()

public actual abstract class MultiFactorResolver actual constructor() {
    public actual abstract val firebaseAuth: FirebaseAuth
    public actual abstract val hints: List<MultiFactorInfo>
    public actual abstract val session: MultiFactorSession
    public actual abstract fun resolveSignIn(assertion: MultiFactorAssertion): Task<AuthResult>
}

/** @property credential The verified phone credential. */
public actual class PhoneMultiFactorAssertion actual constructor(public val credential: PhoneAuthCredential) : MultiFactorAssertion() {
    override val factorId: String get() = PhoneMultiFactorGenerator.FACTOR_ID
    override val native: Any by lazy { nativePhoneMultiFactorAssertion(credential) }
}

public actual class PhoneMultiFactorGenerator actual constructor() {
    public actual companion object {
        public actual val FACTOR_ID: String = "phone"

        public actual fun getAssertion(credential: PhoneAuthCredential): PhoneMultiFactorAssertion = PhoneMultiFactorAssertion(credential)
    }
}

public actual class TotpMultiFactorAssertion internal constructor(override val native: Any) : MultiFactorAssertion() {
    override val factorId: String get() = TotpMultiFactorGenerator.FACTOR_ID
}

public actual class TotpMultiFactorGenerator private constructor() {
    public actual companion object {
        public actual val FACTOR_ID: String = "totp"

        public actual fun generateSecret(session: MultiFactorSession): Task<TotpSecret> = nativeTotpGenerateSecret(session)

        public actual fun getAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): TotpMultiFactorAssertion = TotpMultiFactorAssertion(nativeTotpAssertionForEnrollment(secret, oneTimePassword))

        public actual fun getAssertionForSignIn(enrollmentId: String, oneTimePassword: String): TotpMultiFactorAssertion = TotpMultiFactorAssertion(nativeTotpAssertionForSignIn(enrollmentId, oneTimePassword))
    }
}

public actual interface TotpSecret {
    public actual val sharedSecretKey: String
    public actual fun generateQrCodeUrl(accountName: String, issuer: String): String
    public actual fun openInOtpApp(qrCodeUrl: String)
}
