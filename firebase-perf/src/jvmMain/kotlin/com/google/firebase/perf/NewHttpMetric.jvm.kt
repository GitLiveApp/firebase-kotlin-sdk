/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric

public actual fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric = HttpMetric(url, httpMethod)
