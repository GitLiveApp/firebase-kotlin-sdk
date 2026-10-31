/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import java.net.URL

/*
 * The Kotlin extensions of the Android SDK's firebase-functions (FunctionsKt), as plain common code: on Android and the
 * JVM the facade is a header stub that is stripped, so the SDK's own facade binds.
 */

/** The [FirebaseFunctions] instance of the default [FirebaseApp]; the Android SDK's `Firebase.functions`. */
public val Firebase.functions: FirebaseFunctions
    get() = FirebaseFunctions.getInstance()

/** The [FirebaseFunctions] instance of the default [FirebaseApp] for [regionOrCustomDomain]; see [FirebaseFunctions.getInstance]. */
public fun Firebase.functions(regionOrCustomDomain: String): FirebaseFunctions = FirebaseFunctions.getInstance(regionOrCustomDomain)

/** The [FirebaseFunctions] instance of [app]. */
public fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions.getInstance(app)

/** The [FirebaseFunctions] instance of [app] for [regionOrCustomDomain]; see [FirebaseFunctions.getInstance]. */
public fun Firebase.functions(app: FirebaseApp, regionOrCustomDomain: String): FirebaseFunctions = FirebaseFunctions.getInstance(app, regionOrCustomDomain)

/** A reference to the callable HTTPS trigger [name], with the [HttpsCallableOptions] built by [init]. */
public fun FirebaseFunctions.getHttpsCallable(name: String, init: HttpsCallableOptions.Builder.() -> Unit): HttpsCallableReference = getHttpsCallable(name, HttpsCallableOptions.Builder().apply(init).build())

/** A reference to the callable HTTPS trigger at [url], with the [HttpsCallableOptions] built by [init]. */
public fun FirebaseFunctions.getHttpsCallableFromUrl(url: URL, init: HttpsCallableOptions.Builder.() -> Unit): HttpsCallableReference = getHttpsCallableFromUrl(url, HttpsCallableOptions.Builder().apply(init).build())
