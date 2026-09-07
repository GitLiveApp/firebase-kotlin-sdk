package dev.gitlive.firebase.installations

import com.google.firebase.installations.FirebaseInstallations as AndroidFirebaseInstallations

/** The underlying Firebase Android SDK object. */
public val FirebaseInstallations.android: AndroidFirebaseInstallations get() = compat
