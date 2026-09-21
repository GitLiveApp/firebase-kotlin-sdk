package dev.gitlive.firebase.installations

import cocoapods.FirebaseInstallations.FIRInstallations

/** The underlying Firebase iOS SDK object. */
@Suppress("DEPRECATION")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.ios"))
public val FirebaseInstallations.ios: FIRInstallations get() = compat.ios
