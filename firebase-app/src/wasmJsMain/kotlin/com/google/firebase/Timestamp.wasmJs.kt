/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

internal actual fun currentTimeMillis(): Long = dateNow().toLong()

private fun dateNow(): Double = js("Date.now()")
