/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

/**
 * What the out-of-band code of an email action does, mirroring `com.google.firebase.auth.ActionCodeResult`; from
 * [FirebaseAuth.checkActionCode].
 */
public interface ActionCodeResult {
    /** One of [PASSWORD_RESET], [VERIFY_EMAIL], [RECOVER_EMAIL], [SIGN_IN_WITH_EMAIL_LINK], [VERIFY_BEFORE_CHANGE_EMAIL], [REVERT_SECOND_FACTOR_ADDITION] or [ERROR]. */
    public val operation: Int

    /** The details of the action, an [ActionCodeEmailInfo] or [ActionCodeMultiFactorInfo] for the operations that carry them. */
    public val info: ActionCodeInfo?

    /** The [EMAIL] or [FROM_EMAIL] of the action. */
    @Deprecated("Use info", ReplaceWith("info"))
    public fun getData(key: Int): String?

    /** The operations of [operation]. */
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Operation

    /** The keys of [getData]. */
    @Retention(AnnotationRetention.SOURCE)
    public annotation class ActionDataKey

    public companion object {
        public const val PASSWORD_RESET: Int = 0
        public const val VERIFY_EMAIL: Int = 1
        public const val RECOVER_EMAIL: Int = 2
        public const val ERROR: Int = 3
        public const val SIGN_IN_WITH_EMAIL_LINK: Int = 4
        public const val VERIFY_BEFORE_CHANGE_EMAIL: Int = 5
        public const val REVERT_SECOND_FACTOR_ADDITION: Int = 6

        public const val EMAIL: Int = 0
        public const val FROM_EMAIL: Int = 1
    }
}

/** The details of an email action, mirroring `com.google.firebase.auth.ActionCodeInfo`: the email it applies to. */
public expect open class ActionCodeInfo() {
    public open val email: String
}

/** The details of an email change action: the [previousEmail] it reverts to, and the new [email]. */
public abstract class ActionCodeEmailInfo : ActionCodeInfo() {
    public abstract val previousEmail: String
}

/** The details of a second factor revert action: the [multiFactorInfo] being removed. */
public abstract class ActionCodeMultiFactorInfo : ActionCodeInfo() {
    public abstract val multiFactorInfo: MultiFactorInfo
}

/** The parts of an email action link, mirroring `com.google.firebase.auth.ActionCodeUrl`. */
public expect class ActionCodeUrl {
    public val apiKey: String?
    public val code: String?
    public val continueUrl: String?
    public val languageCode: String?

    /** An [ActionCodeResult] operation. */
    public val operation: Int

    public companion object {
        /** Parses [link], or returns null when it is not an email action link. */
        public fun parseLink(link: String): ActionCodeUrl?
    }
}

/**
 * How the link of an email action behaves, mirroring `com.google.firebase.auth.ActionCodeSettings`: built with
 * [Builder] from [newBuilder] or the `actionCodeSettings` extension.
 */
public expect class ActionCodeSettings {
    /** The continue URL, opened when the action completes. */
    public val url: String?

    /** Whether the link is opened in the app rather than a web page. */
    public fun canHandleCodeInApp(): Boolean

    public val iosBundle: String?
    public val androidPackageName: String?
    public val androidInstallApp: Boolean
    public val androidMinimumVersion: String?

    /** The Firebase Hosting domain the link uses, or null for the project default. */
    public val linkDomain: String?

    public class Builder {
        public val url: String?
        public val handleCodeInApp: Boolean
        public val iosBundleId: String?
        public val linkDomain: String?

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("linkDomain"))
        public val dynamicLinkDomain: String?

        /** Sets the continue URL, which must be allowlisted in the console. */
        public fun setUrl(url: String): Builder

        /** Sets whether the link is opened in the app; the app's package or bundle must then be set. */
        public fun setHandleCodeInApp(handleCodeInApp: Boolean): Builder

        /** Sets the iOS app the link opens. */
        public fun setIOSBundleId(iOSBundleId: String): Builder

        /** Sets the Android app the link opens, whether to install it, and the minimum version that handles the link. */
        public fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder

        /** Sets the Firebase Hosting domain the link uses. */
        public fun setLinkDomain(linkDomain: String): Builder

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("setLinkDomain(dynamicLinkDomain)"))
        public fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder

        public fun build(): ActionCodeSettings
    }

    public companion object {
        public fun newBuilder(): Builder
    }
}
