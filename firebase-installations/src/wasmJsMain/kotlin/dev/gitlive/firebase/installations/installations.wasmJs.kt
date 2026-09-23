package dev.gitlive.firebase.installations

import dev.gitlive.firebase.installations.externals.Installations

/** The underlying Firebase JS SDK object. */
public val FirebaseInstallations.js: Installations get() = compat.js
