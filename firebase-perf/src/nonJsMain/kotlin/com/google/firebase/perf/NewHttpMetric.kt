/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric
import java.net.URL

/**
 * Creates an [HttpMetric] for a request of [httpMethod] (one of [FirebasePerformance.HttpMethod]) to [url]; the Android
 * SDK's `FirebasePerformance.newHttpMetric(String, String)`. It is an extension because [HttpMetric] does not exist in
 * the JS SDK, so the member is `@unavailable` in the common API; on Android the SDK's own member binds.
 */
public expect fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric

/** The Android SDK's `FirebasePerformance.newHttpMetric(URL, String)`, over the `java.net.URL` stand-in of firebase-app. */
public expect fun FirebasePerformance.newHttpMetric(url: URL, httpMethod: String): HttpMetric
