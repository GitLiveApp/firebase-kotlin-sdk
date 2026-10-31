/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context
import swiftPMImport.dev.gitlive.firebase.app.FIRApp

/** Configures the default app from `GoogleService-Info.plist`; the [context] is a placeholder and is ignored. */
public actual fun Firebase.initialize(context: Context): FirebaseApp? {
    FIRApp.configure()
    return FIRApp.defaultApp()?.let { FirebaseApp(it) }
}

/** Configures the default app with the given [options]; the [context] is a placeholder and is ignored. */
public actual fun Firebase.initialize(context: Context, options: FirebaseOptions): FirebaseApp {
    FIRApp.configureWithOptions(options.ios)
    return FirebaseApp.getInstance()
}

/** Configures the app named [name] with the given [options]; the [context] is a placeholder and is ignored. */
public actual fun Firebase.initialize(context: Context, options: FirebaseOptions, name: String): FirebaseApp {
    FIRApp.configureWithName(name, options.ios)
    return FirebaseApp.getInstance(name)
}
