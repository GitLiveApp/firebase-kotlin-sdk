/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import cocoapods.FirebasePerformance.FIRHTTPMethod
import cocoapods.FirebasePerformance.FIRHTTPMethodCONNECT
import cocoapods.FirebasePerformance.FIRHTTPMethodDELETE
import cocoapods.FirebasePerformance.FIRHTTPMethodGET
import cocoapods.FirebasePerformance.FIRHTTPMethodHEAD
import cocoapods.FirebasePerformance.FIRHTTPMethodOPTIONS
import cocoapods.FirebasePerformance.FIRHTTPMethodPATCH
import cocoapods.FirebasePerformance.FIRHTTPMethodPOST
import cocoapods.FirebasePerformance.FIRHTTPMethodPUT
import cocoapods.FirebasePerformance.FIRHTTPMethodTRACE
import cocoapods.FirebasePerformance.FIRHTTPMetric
import com.google.firebase.perf.metrics.HttpMetric
import platform.Foundation.NSURL

public actual fun FirebasePerformance.newHttpMetric(url: String, httpMethod: String): HttpMetric {
    val nsUrl = requireNotNull(NSURL.URLWithString(url)) { "Invalid URL: $url" }
    return HttpMetric(FIRHTTPMetric(nsUrl, httpMethod.toFIRHTTPMethod()))
}

private fun String.toFIRHTTPMethod(): FIRHTTPMethod = when (this) {
    FirebasePerformance.HttpMethod.GET -> FIRHTTPMethodGET
    FirebasePerformance.HttpMethod.PUT -> FIRHTTPMethodPUT
    FirebasePerformance.HttpMethod.POST -> FIRHTTPMethodPOST
    FirebasePerformance.HttpMethod.DELETE -> FIRHTTPMethodDELETE
    FirebasePerformance.HttpMethod.HEAD -> FIRHTTPMethodHEAD
    FirebasePerformance.HttpMethod.PATCH -> FIRHTTPMethodPATCH
    FirebasePerformance.HttpMethod.OPTIONS -> FIRHTTPMethodOPTIONS
    FirebasePerformance.HttpMethod.TRACE -> FIRHTTPMethodTRACE
    FirebasePerformance.HttpMethod.CONNECT -> FIRHTTPMethodCONNECT
    else -> throw IllegalArgumentException("Unsupported HTTP method: $this")
}
