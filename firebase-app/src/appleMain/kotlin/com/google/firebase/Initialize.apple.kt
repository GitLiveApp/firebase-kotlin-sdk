/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import cocoapods.FirebaseCore.FIRApp

/** Configures the default app from `GoogleService-Info.plist`; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?): FirebaseApp? {
    FIRApp.configure()
    return FIRApp.defaultApp()?.let { FirebaseApp(it) }
}

/** Configures the default app with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp {
    FIRApp.configureWithOptions(options.ios)
    return FirebaseApp.getInstance()
}

/** Configures the app named [name] with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp {
    FIRApp.configureWithName(name, options.ios)
    return FirebaseApp.getInstance(name)
}
