/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FunctionsUrlKt")

package com.google.firebase.functions

/*
 * Unlike the header stubs around it, this file is shipped (see keepClasses in the build file): the SDK's members take a
 * java.net.URL, which common code cannot name, so these same-named extensions take a String and call them through
 * FunctionsJvmApi.
 */

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String): HttpsCallableReference = FunctionsJvmApi.getHttpsCallableFromUrl(this, url)

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, options: HttpsCallableOptions): HttpsCallableReference = FunctionsJvmApi.getHttpsCallableFromUrl(this, url, options)

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, init: HttpsCallableOptions.Builder.() -> Unit): HttpsCallableReference = getHttpsCallableFromUrl(url, HttpsCallableOptions.Builder().apply(init).build())
