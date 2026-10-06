/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import kotlin.time.Duration
import kotlin.time.DurationUnit

public actual fun HttpsCallableReference.setTimeout(timeout: Duration) {
    setTimeoutSeconds(timeout.toDouble(DurationUnit.SECONDS))
}

public actual fun HttpsCallableReference.withTimeout(timeout: Duration): HttpsCallableReference = withTimeoutSeconds(timeout.toDouble(DurationUnit.SECONDS))
