/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("HttpsCallableTimeoutKt")

package com.google.firebase.functions

import kotlin.time.Duration

/*
 * Unlike the header stubs around it, this file is shipped (see keepClasses in the build file): the SDK's members take a
 * java.util.concurrent.TimeUnit, which common code cannot name, so these extensions take a Duration and call them
 * through FunctionsJvmApi.
 */

public actual fun HttpsCallableReference.setTimeout(timeout: Duration) {
    FunctionsJvmApi.setTimeout(this, timeout.inWholeMilliseconds)
}

public actual fun HttpsCallableReference.withTimeout(timeout: Duration): HttpsCallableReference = FunctionsJvmApi.withTimeout(this, timeout.inWholeMilliseconds)
