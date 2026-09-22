/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

public actual open class ActionCodeInfo actual constructor() {
    public actual open val email: String get() = throw UnsupportedOperationException("no email")
}

internal class ActionCodeInfoImpl(override val email: String) : ActionCodeInfo()

internal class ActionCodeEmailInfoImpl(override val email: String, override val previousEmail: String) : ActionCodeEmailInfo()

internal class ActionCodeMultiFactorInfoImpl(override val email: String, override val multiFactorInfo: MultiFactorInfo) : ActionCodeMultiFactorInfo()

internal class ActionCodeResultImpl(override val operation: Int, override val info: ActionCodeInfo?) : ActionCodeResult {
    @Deprecated("Use info", ReplaceWith("info"))
    override fun getData(key: Int): String? = when (key) {
        ActionCodeResult.EMAIL -> info?.email
        ActionCodeResult.FROM_EMAIL -> (info as? ActionCodeEmailInfo)?.previousEmail
        else -> null
    }
}

/**
 * @property apiKey The API key of the project.
 * @property code The out-of-band code.
 * @property continueUrl The URL to continue to after the action.
 * @property languageCode The language of the action's UI.
 * @property operation The [ActionCodeResult] operation.
 */
public actual class ActionCodeUrl internal constructor(
    public actual val apiKey: String?,
    public actual val code: String?,
    public actual val continueUrl: String?,
    public actual val languageCode: String?,
    public actual val operation: Int,
) {
    public actual companion object {
        public actual fun parseLink(link: String): ActionCodeUrl? = parseActionCodeUrl(link)
    }
}

/**
 * A pure Kotlin value; the platform SDKs' forms are built from it when it is used.
 *
 * @property url The continue URL.
 * @property iosBundle The iOS app the link opens.
 * @property androidPackageName The Android app the link opens.
 * @property androidInstallApp Whether to install the Android app when it is missing.
 * @property androidMinimumVersion The minimum version of the Android app that handles the link.
 * @property linkDomain The Firebase Hosting domain the link uses.
 */
public actual class ActionCodeSettings internal constructor(
    public actual val url: String?,
    private val handleCodeInApp: Boolean,
    public actual val iosBundle: String?,
    public actual val androidPackageName: String?,
    public actual val androidInstallApp: Boolean,
    public actual val androidMinimumVersion: String?,
    public actual val linkDomain: String?,
    internal val dynamicLinkDomain: String?,
) {
    public actual fun canHandleCodeInApp(): Boolean = handleCodeInApp

    public actual class Builder internal constructor() {
        private var urlValue: String? = null
        private var handleCodeInAppValue = false
        private var iOSBundleIdValue: String? = null
        private var linkDomainValue: String? = null
        private var dynamicLinkDomainValue: String? = null
        internal var androidPackageName: String? = null
        internal var androidInstallApp: Boolean = false
        internal var androidMinimumVersion: String? = null

        public actual val url: String? get() = urlValue
        public actual val handleCodeInApp: Boolean get() = handleCodeInAppValue
        public actual val iosBundleId: String? get() = iOSBundleIdValue
        public actual val linkDomain: String? get() = linkDomainValue

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("linkDomain"))
        public actual val dynamicLinkDomain: String? get() = dynamicLinkDomainValue

        public actual fun setUrl(url: String): Builder = apply { urlValue = url }
        public actual fun setHandleCodeInApp(handleCodeInApp: Boolean): Builder = apply { handleCodeInAppValue = handleCodeInApp }
        public actual fun setIOSBundleId(iOSBundleId: String): Builder = apply { iOSBundleIdValue = iOSBundleId }
        public actual fun setAndroidPackageName(androidPackageName: String, installIfNotAvailable: Boolean, minimumVersion: String?): Builder = apply {
            this.androidPackageName = androidPackageName
            androidInstallApp = installIfNotAvailable
            androidMinimumVersion = minimumVersion
        }
        public actual fun setLinkDomain(linkDomain: String): Builder = apply { linkDomainValue = linkDomain }

        @Deprecated("Dynamic Links is deprecated; use setLinkDomain", ReplaceWith("setLinkDomain(dynamicLinkDomain)"))
        public actual fun setDynamicLinkDomain(dynamicLinkDomain: String): Builder = apply { dynamicLinkDomainValue = dynamicLinkDomain }

        public actual fun build(): ActionCodeSettings = ActionCodeSettings(urlValue, handleCodeInAppValue, iOSBundleIdValue, androidPackageName, androidInstallApp, androidMinimumVersion, linkDomainValue, dynamicLinkDomainValue)
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder()
    }
}
