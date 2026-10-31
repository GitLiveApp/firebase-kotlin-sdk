/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirebaseKt")
@file:JvmMultifileClass

package com.google.firebase

import android.content.Context
import dev.gitlive.firebase.stub

/*
 * Header stubs of the SDK's own `Firebase.initialize` extensions (see buildSrc utils/HeaderStubs.kt). With the common
 * Firebase.kt they form the `FirebaseKt` facade, the SDK's own, so this module's calls to them bind at runtime.
 */

public actual fun Firebase.initialize(context: Context): FirebaseApp? = stub()

public actual fun Firebase.initialize(context: Context, options: FirebaseOptions): FirebaseApp = stub()

public actual fun Firebase.initialize(context: Context, options: FirebaseOptions, name: String): FirebaseApp = stub()
