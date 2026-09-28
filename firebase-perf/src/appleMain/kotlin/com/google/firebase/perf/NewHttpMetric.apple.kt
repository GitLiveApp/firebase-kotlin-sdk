/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import cocoapods.FirebasePerformance.FIRHTTPMethod
import cocoapods.FirebasePerformance.FIRHTTPMetric
import com.google.firebase.perf.metrics.HttpMetric
import platform.Foundation.NSURL

public actual fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric {
    val nsUrl = requireNotNull(NSURL.URLWithString(url)) { "Invalid URL: $url" }
    return HttpMetric(FIRHTTPMetric(nsUrl, httpMethod.toFIRHTTPMethod()))
}

private fun String.toFIRHTTPMethod(): FIRHTTPMethod = when (this) {
    FirebasePerformance.HttpMethod.GET -> FIRHTTPMethod.FIRHTTPMethodGET
    FirebasePerformance.HttpMethod.PUT -> FIRHTTPMethod.FIRHTTPMethodPUT
    FirebasePerformance.HttpMethod.POST -> FIRHTTPMethod.FIRHTTPMethodPOST
    FirebasePerformance.HttpMethod.DELETE -> FIRHTTPMethod.FIRHTTPMethodDELETE
    FirebasePerformance.HttpMethod.HEAD -> FIRHTTPMethod.FIRHTTPMethodHEAD
    FirebasePerformance.HttpMethod.PATCH -> FIRHTTPMethod.FIRHTTPMethodPATCH
    FirebasePerformance.HttpMethod.OPTIONS -> FIRHTTPMethod.FIRHTTPMethodOPTIONS
    FirebasePerformance.HttpMethod.TRACE -> FIRHTTPMethod.FIRHTTPMethodTRACE
    FirebasePerformance.HttpMethod.CONNECT -> FIRHTTPMethod.FIRHTTPMethodCONNECT
    else -> throw IllegalArgumentException("Unsupported HTTP method: $this")
}
