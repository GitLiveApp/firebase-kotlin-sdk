/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

/**
 * A metric of one HTTP request, as the Android SDK's `com.google.firebase.perf.metrics.HttpMetric`. The JS SDK has no
 * custom HTTP metrics (it records network requests itself), so this class exists on Android, the JVM and Apple platforms.
 */
public expect class HttpMetric {
    /** The value of the custom attribute [attribute], or null if it is not set. */
    public fun getAttribute(attribute: String): String?

    /** All custom attributes of this metric. */
    public val attributes: Map<String, String>

    /** Sets the custom attribute [attribute] to [value]. */
    public fun putAttribute(attribute: String, value: String)

    /** Removes the custom attribute [attribute]. */
    public fun removeAttribute(attribute: String)

    /** Sets the HTTP response status code. */
    public fun setHttpResponseCode(responseCode: Int)

    /** Sets the size of the request payload in bytes. */
    public fun setRequestPayloadSize(bytes: Long)

    /** Sets the content type of the response. */
    public fun setResponseContentType(contentType: String?)

    /** Sets the size of the response payload in bytes. */
    public fun setResponsePayloadSize(bytes: Long)

    /** Marks the start of the request. */
    public fun start()

    /** Marks the end of the response and reports the metric. */
    public fun stop()

    public companion object {
        /** Maximum length of a custom attribute key. */
        public val MAX_ATTRIBUTE_KEY_LENGTH: Int

        /** Maximum length of a custom attribute value. */
        public val MAX_ATTRIBUTE_VALUE_LENGTH: Int

        /** Maximum number of custom attributes. */
        public val MAX_TRACE_CUSTOM_ATTRIBUTES: Int

        /** Maximum length of a trace name. */
        public val MAX_TRACE_NAME_LENGTH: Int
    }
}
