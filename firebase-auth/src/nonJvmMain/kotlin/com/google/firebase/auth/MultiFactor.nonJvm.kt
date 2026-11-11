/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import dev.gitlive.firebase.auth.TO_JSON_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.auth.TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_QR_CODE_URL_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_SECRET_PARAMETERS_ANDROID_ONLY

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
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TO_JSON_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual open fun toJson(): org.json.JSONObject = throw UnsupportedOperationException(TO_JSON_ANDROID_ONLY)

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
    actual override val uid: String,
    actual override val displayName: String?,
    actual override val enrollmentTimestamp: Long,
    public actual val phoneNumber: String,
) : MultiFactorInfo() {
    actual override val factorId: String get() = PhoneMultiFactorGenerator.FACTOR_ID
    override var native: Any? = null
        internal set

    internal constructor(uid: String, displayName: String?, enrollmentTimestamp: Long, phoneNumber: String, native: Any) : this(uid, displayName, enrollmentTimestamp, phoneNumber) {
        this.native = native
    }
}

public actual class TotpMultiFactorInfo internal constructor(
    actual override val uid: String,
    actual override val displayName: String?,
    actual override val enrollmentTimestamp: Long,
    override val native: Any,
) : MultiFactorInfo() {
    actual override val factorId: String get() = TotpMultiFactorGenerator.FACTOR_ID
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
public actual class PhoneMultiFactorAssertion actual constructor(internal val credential: PhoneAuthCredential) : MultiFactorAssertion() {
    actual override val factorId: String get() = PhoneMultiFactorGenerator.FACTOR_ID
    override val native: Any by lazy { nativePhoneMultiFactorAssertion(credential) }
}

public actual class PhoneMultiFactorGenerator actual constructor() {
    public actual companion object {
        public actual val FACTOR_ID: String = "phone"

        public actual fun getAssertion(credential: PhoneAuthCredential): PhoneMultiFactorAssertion = PhoneMultiFactorAssertion(credential)
    }
}

public actual class TotpMultiFactorAssertion internal constructor(override val native: Any) : MultiFactorAssertion() {
    @Deprecated(TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual constructor(oneTimePassword: String, secret: TotpSecret, enrollmentId: String) : this(Unit) {
        throw UnsupportedOperationException(TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY)
    }

    actual override val factorId: String get() = TotpMultiFactorGenerator.FACTOR_ID
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

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY, ReplaceWith("openInOtpApp(qrCodeUrl)"), DeprecationLevel.ERROR)
    public actual fun openInOtpApp(qrCodeUrl: String, fallbackUrl: String, activity: android.app.Activity)

    // The Android-only parameters, which the iOS and JS SDKs do not expose; implemented by [TotpSecretBase].
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual val codeIntervalSeconds: Int

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual val codeLength: Int

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual val enrollmentCompletionDeadline: Long

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual val hashAlgorithm: String

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual val sessionInfo: String

    @Deprecated(TOTP_QR_CODE_URL_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun generateQrCodeUrl(): String
}

/** The base of the platform secrets: the Android-only parameters fail, as common code cannot call them and the platform SDKs do not expose them. */
internal abstract class TotpSecretBase : TotpSecret {
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override val codeIntervalSeconds: Int get() = throw UnsupportedOperationException(TOTP_SECRET_PARAMETERS_ANDROID_ONLY)

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override val codeLength: Int get() = throw UnsupportedOperationException(TOTP_SECRET_PARAMETERS_ANDROID_ONLY)

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override val enrollmentCompletionDeadline: Long get() = throw UnsupportedOperationException(TOTP_SECRET_PARAMETERS_ANDROID_ONLY)

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override val hashAlgorithm: String get() = throw UnsupportedOperationException(TOTP_SECRET_PARAMETERS_ANDROID_ONLY)

    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override val sessionInfo: String get() = throw UnsupportedOperationException(TOTP_SECRET_PARAMETERS_ANDROID_ONLY)

    @Deprecated(TOTP_QR_CODE_URL_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    override fun generateQrCodeUrl(): String = throw UnsupportedOperationException(TOTP_QR_CODE_URL_ANDROID_ONLY)

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY, ReplaceWith("openInOtpApp(qrCodeUrl)"), DeprecationLevel.ERROR)
    override fun openInOtpApp(qrCodeUrl: String, fallbackUrl: String, activity: android.app.Activity): Unit = throw UnsupportedOperationException(TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY)
}
