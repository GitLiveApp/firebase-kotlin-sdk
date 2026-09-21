/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.FirebaseApp

@Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")
internal actual fun performanceOf(app: FirebaseApp): FirebasePerformance = FirebasePerformance.getInstance()
