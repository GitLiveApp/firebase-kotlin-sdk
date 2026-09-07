package dev.gitlive.firebase.installations

import dev.gitlive.firebase.android.installations.FirebaseInstallations as AndroidFirebaseInstallations

/** The underlying (relocated) Firebase Android SDK object. */
public val FirebaseInstallations.android: AndroidFirebaseInstallations get() = compat.android
