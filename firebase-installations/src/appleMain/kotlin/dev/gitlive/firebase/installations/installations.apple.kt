package dev.gitlive.firebase.installations

import swiftPMImport.dev.gitlive.firebase.installations.FIRInstallations

/** The underlying Firebase iOS SDK object. */
public val FirebaseInstallations.ios: FIRInstallations get() = compat.ios
