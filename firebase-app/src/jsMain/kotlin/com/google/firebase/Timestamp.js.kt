/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import kotlin.js.Date

internal actual fun currentTimeMillis(): Long = Date.now().toLong()
