/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import kotlin.time.Duration

public actual fun HttpsCallableReference.setTimeout(timeout: Duration) {
    setTimeoutMillis(timeout.inWholeMilliseconds)
}

public actual fun HttpsCallableReference.withTimeout(timeout: Duration): HttpsCallableReference = withTimeoutMillis(timeout.inWholeMilliseconds)
