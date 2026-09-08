/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.externals.initializeApp as jsInitializeApp

/** Not supported on JS, where the options cannot be read from a default configuration: always throws. */
public actual fun Firebase.initialize(context: Any?): FirebaseApp? = throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

/** Initializes the default app with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson()))

/** Initializes the app named [name] with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson(), name))
