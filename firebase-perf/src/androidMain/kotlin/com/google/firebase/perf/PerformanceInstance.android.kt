/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("PerformanceInstanceKt")

package com.google.firebase.perf

import com.google.firebase.FirebaseApp

// Shipped (see keepClasses in the build file): the SDK's per-app instance is reached through FirebaseApp.get(Class), which
// takes a java.lang.Class that common code cannot name, so PerformanceStatics.java calls it after the stubs are stripped.
internal actual fun performanceOf(app: FirebaseApp): FirebasePerformance = PerformanceStatics.getInstance(app)
