/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.FirebaseApp

/**
 * The [FirebasePerformance] of [app]. The Android SDK creates one per app through its component runtime, which the
 * `dev.gitlive` wrapper's `Firebase.performance(app)` exposes; the other SDKs have a single instance.
 */
internal expect fun performanceOf(app: FirebaseApp): FirebasePerformance
