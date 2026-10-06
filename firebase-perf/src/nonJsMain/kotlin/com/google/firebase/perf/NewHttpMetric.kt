/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric

/**
 * Creates an [HttpMetric] for a request of [httpMethod] (one of [FirebasePerformance.HttpMethod]) to [url]; the Android
 * SDK's `FirebasePerformance.newHttpMetric(String, String)`. It is an extension because [HttpMetric] does not exist on
 * JS; on Android the SDK's own member binds.
 */
public expect fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric
