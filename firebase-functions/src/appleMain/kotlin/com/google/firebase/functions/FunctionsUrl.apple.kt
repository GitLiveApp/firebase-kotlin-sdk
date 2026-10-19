/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import platform.Foundation.NSURL

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String): HttpsCallableReference = httpsCallableFromUrl(url.toNSURL(), null)

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, options: HttpsCallableOptions): HttpsCallableReference = httpsCallableFromUrl(url.toNSURL(), options)

public actual fun FirebaseFunctions.getHttpsCallableFromUrl(url: String, init: HttpsCallableOptions.Builder.() -> Unit): HttpsCallableReference = getHttpsCallableFromUrl(url, HttpsCallableOptions.Builder().apply(init).build())

private fun String.toNSURL(): NSURL = requireNotNull(NSURL.URLWithString(this)) { "Not a valid URL: $this" }
