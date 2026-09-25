/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseCore.FIROptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import dev.gitlive.firebase.compatOptionsBuilder

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

/** All initialized apps; the [context] is ignored. */
public actual fun Firebase.getApps(context: Any?): List<FirebaseApp> = FIRApp.allApps().orEmpty().values.map { FirebaseApp(it as FIRApp) }

/** The options from `GoogleService-Info.plist`, or null when the bundle has none; the [context] is ignored. */
public actual fun Firebase.fromResource(context: Any?): FirebaseOptions? = FIROptions.defaultOptions()?.let { FirebaseOptions(it) }

public actual fun FirebaseApp.deleteApp(): Task<Nothing?> {
    val source = TaskCompletionSource<Nothing?>()
    ios.deleteApp { success ->
        if (success) source.setResult(null) else source.setException(FirebaseException("The Firebase app could not be deleted"))
    }
    return source.task
}

public actual fun FirebaseOptions(
    applicationId: String,
    apiKey: String,
    databaseUrl: String?,
    gaTrackingId: String?,
    storageBucket: String?,
    projectId: String?,
    gcmSenderId: String?,
    authDomain: String?,
): FirebaseOptions = compatOptionsBuilder(applicationId, apiKey, databaseUrl, gaTrackingId, storageBucket, projectId, gcmSenderId).build()
