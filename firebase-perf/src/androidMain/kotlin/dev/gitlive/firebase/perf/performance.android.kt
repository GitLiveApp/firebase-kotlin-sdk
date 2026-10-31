/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("PerformanceKt")
@file:JvmMultifileClass

package dev.gitlive.firebase.perf

/** The underlying Firebase Android SDK object. */
public val FirebasePerformance.android: com.google.firebase.perf.FirebasePerformance get() = compat
