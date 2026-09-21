/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import dev.gitlive.firebase.externals.deleteApp as jsDeleteApp
import dev.gitlive.firebase.externals.getApps as jsGetApps
import dev.gitlive.firebase.externals.initializeApp as jsInitializeApp

/** Not supported on JS, where the options cannot be read from a default configuration: always throws. */
public actual fun Firebase.initialize(context: Any?): FirebaseApp? = throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

/** Initializes the default app with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson()))

/** Initializes the app named [name] with the given [options]; the [context] is ignored. */
public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson(), name))

/** All initialized apps; the [context] is ignored. */
public actual fun Firebase.getApps(context: Any?): List<FirebaseApp> = jsGetApps().map { FirebaseApp(it) }

/** Always null: JS has no default configuration to read options from. */
public actual fun Firebase.fromResource(context: Any?): FirebaseOptions? = null

public actual fun FirebaseApp.deleteApp(): Task<Nothing?> {
    val source = TaskCompletionSource<Nothing?>()
    jsDeleteApp(js).then({ source.setResult(null) }, { source.setException(FirebaseException(it.message ?: "The Firebase app could not be deleted", it)) })
    return source.task
}
