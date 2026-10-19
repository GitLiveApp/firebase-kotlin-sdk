package dev.gitlive.firebase.installations

import cocoapods.FirebaseInstallations.FIRInstallations

/** The underlying Firebase iOS SDK object. */
@Suppress("DEPRECATION")
public val FirebaseInstallations.ios: FIRInstallations get() = compat.ios
