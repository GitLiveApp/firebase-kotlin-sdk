/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FunctionsUrlKt")

package com.google.firebase.functions

import kotlin.jvm.JvmName

/*
 * Multiplatform counterparts of `FirebaseFunctions.getHttpsCallableFromUrl(java.net.URL, ...)` taking the URL as a
 * String, since `java.net.URL` is JVM-only. Shipped on Android and the JVM too (see keepClasses in the build file): a
 * member cannot be given such an overload without shadowing these, so they are extension functions of the same name and
 * Android code passing a URL keeps binding to the SDK's members.
 */

/** A reference to the callable HTTPS trigger at [url], e.g. `https://us-central1-project.cloudfunctions.net/function`. */
public expect fun FirebaseFunctions.getHttpsCallableFromUrl(url: String): HttpsCallableReference

/** A reference to the callable HTTPS trigger at [url], called with [options]. */
public expect fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, options: HttpsCallableOptions): HttpsCallableReference

/** A reference to the callable HTTPS trigger at [url], with the [HttpsCallableOptions] built by [init]. */
public expect fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, init: HttpsCallableOptions.Builder.() -> Unit): HttpsCallableReference
