/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRHTTPMetric

/** @property ios The underlying Firebase iOS SDK object. */
public actual class HttpMetric internal constructor(public val ios: FIRHTTPMetric) {
    public actual fun getAttribute(attribute: String): String? = ios.valueForAttribute(attribute)

    public actual val attributes: Map<String, String>
        get() = ios.attributes.mapKeys { it.key.toString() }.mapValues { it.value.toString() }

    public actual fun putAttribute(attribute: String, value: String) {
        ios.setValue(value, attribute)
    }

    public actual fun removeAttribute(attribute: String) {
        ios.removeAttribute(attribute)
    }

    public actual fun setHttpResponseCode(responseCode: Int) {
        ios.responseCode = responseCode.toLong()
    }

    public actual fun setRequestPayloadSize(bytes: Long) {
        ios.requestPayloadSize = bytes
    }

    public actual fun setResponseContentType(contentType: String?) {
        ios.responseContentType = contentType
    }

    public actual fun setResponsePayloadSize(bytes: Long) {
        ios.responsePayloadSize = bytes
    }

    public actual fun start() {
        ios.start()
    }

    public actual fun stop() {
        ios.stop()
    }

    override fun equals(other: Any?): Boolean = other is HttpMetric && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "HttpMetric($ios)"

    public actual companion object {
        /** Maximum length of a custom attribute key. */
        public actual val MAX_ATTRIBUTE_KEY_LENGTH: Int = 40

        /** Maximum length of a custom attribute value. */
        public actual val MAX_ATTRIBUTE_VALUE_LENGTH: Int = 100

        /** Maximum number of custom attributes. */
        public actual val MAX_TRACE_CUSTOM_ATTRIBUTES: Int = 5

        /** Maximum length of a trace name. */
        public actual val MAX_TRACE_NAME_LENGTH: Int = 100
    }
}
