package dev.gitlive.firebase.installations

import dev.gitlive.firebase.installations.externals.Installations

/** The underlying Firebase JS SDK object. */
@Suppress("DEPRECATION")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.js"))
public val FirebaseInstallations.js: Installations get() = compat.js
