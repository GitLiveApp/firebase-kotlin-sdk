/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp

/** Returns the [FirebaseInstallations] instance of the default [FirebaseApp]. */
public val Firebase.installations: FirebaseInstallations
    get() = FirebaseInstallations.getInstance()

/** Returns the [FirebaseInstallations] instance of the given [app]. */
public fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations.getInstance(app)
