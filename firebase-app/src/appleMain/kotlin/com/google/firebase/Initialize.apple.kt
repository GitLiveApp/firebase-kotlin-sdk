/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = FirebaseApp.initializeApp(context)

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp.initializeApp(context, options)

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp.initializeApp(context, options, name)
