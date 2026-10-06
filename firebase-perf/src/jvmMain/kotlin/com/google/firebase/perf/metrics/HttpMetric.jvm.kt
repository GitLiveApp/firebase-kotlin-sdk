/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import kotlin.jvm.JvmField

/**
 * An in-memory HTTP metric (see FirebasePerformance.jvm.kt).
 *
 * @property url The request URL.
 * @property httpMethod The HTTP method, one of [com.google.firebase.perf.FirebasePerformance.HttpMethod].
 */
public actual class HttpMetric internal constructor(public val url: String, public val httpMethod: String) {
    private val customAttributes = mutableMapOf<String, String>()

    /** The value last given to [setHttpResponseCode], or null. */
    public var httpResponseCode: Int? = null
        private set

    /** The value last given to [setRequestPayloadSize], or null. */
    public var requestPayloadSize: Long? = null
        private set

    /** The value last given to [setResponseContentType], or null. */
    public var responseContentType: String? = null
        private set

    /** The value last given to [setResponsePayloadSize], or null. */
    public var responsePayloadSize: Long? = null
        private set

    public actual fun getAttribute(attribute: String): String? = customAttributes[attribute]

    public actual val attributes: Map<String, String> get() = customAttributes.toMap()

    public actual fun putAttribute(attribute: String, value: String) {
        customAttributes[attribute] = value
    }

    public actual fun removeAttribute(attribute: String) {
        customAttributes.remove(attribute)
    }

    public actual fun setHttpResponseCode(responseCode: Int) {
        httpResponseCode = responseCode
    }

    public actual fun setRequestPayloadSize(bytes: Long) {
        requestPayloadSize = bytes
    }

    public actual fun setResponseContentType(contentType: String?) {
        responseContentType = contentType
    }

    public actual fun setResponsePayloadSize(bytes: Long) {
        responsePayloadSize = bytes
    }

    public actual fun start() {
    }

    public actual fun stop() {
    }

    override fun toString(): String = "HttpMetric($httpMethod $url)"

    public actual companion object {
        /** Maximum length of a custom attribute key. */
        @JvmField
        public actual val MAX_ATTRIBUTE_KEY_LENGTH: Int = 40

        /** Maximum length of a custom attribute value. */
        @JvmField
        public actual val MAX_ATTRIBUTE_VALUE_LENGTH: Int = 100

        /** Maximum number of custom attributes. */
        @JvmField
        public actual val MAX_TRACE_CUSTOM_ATTRIBUTES: Int = 5

        /** Maximum length of a trace name. */
        @JvmField
        public actual val MAX_TRACE_NAME_LENGTH: Int = 100
    }
}
