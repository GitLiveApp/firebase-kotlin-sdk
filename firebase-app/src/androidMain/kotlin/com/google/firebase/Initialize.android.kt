/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirebaseInitializeKt")

package com.google.firebase

import android.content.Context
import dev.gitlive.firebase.compatOptionsBuilder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

/*
 * Unlike the header stubs around it, this file is shipped: the SDK's FirebaseKt only offers Context overloads, and a
 * top-level function can be overloaded from another file facade. The Context overloads stay more specific, so Android
 * code keeps calling the SDK's functions and only Any? arguments reach these (see buildSrc utils/HeaderStubs.kt keepClasses).
 */

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = initialize(context.asAndroidContext())

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = initialize(context.asAndroidContext(), options)

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = initialize(context.asAndroidContext(), options, name)

public actual fun Firebase.getApps(context: Any?): List<FirebaseApp> = FirebaseAppStatics.getApps(context.asAndroidContext())

public actual fun Firebase.fromResource(context: Any?): FirebaseOptions? = FirebaseAppStatics.fromResource(context.asAndroidContext())

/** The SDK's `delete()` is synchronous on Android, so the task is already complete. */
public actual fun FirebaseApp.deleteApp(): Task<Nothing?> {
    FirebaseAppStatics.delete(this)
    return TaskCompletionSource<Nothing?>().apply { setResult(null) }.task
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

private fun Any?.asAndroidContext(): Context = requireNotNull(this as? Context) { "An android.content.Context is required on Android, got $this" }
