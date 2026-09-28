/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("NewHttpMetricKt")

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric

/*
 * Unlike the header stubs around it, this file is shipped (see keepClasses in the build file): common code reaches the
 * SDK's newHttpMetric member through this extension, which binds to the member. Android code calling newHttpMetric
 * resolves the member directly, since a member wins over an extension.
 */

public actual fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric = newHttpMetric(url, httpMethod)
