/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import dev.gitlive.firebase.perf.stub
import kotlin.jvm.JvmField

/** Header stub; see FirebasePerformance.android.kt. */
public actual class HttpMetric private constructor() {
    public actual fun getAttribute(attribute: String): String? = stub()
    public actual val attributes: Map<String, String> get() = stub()
    public actual fun putAttribute(attribute: String, value: String): Unit = stub()
    public actual fun removeAttribute(attribute: String): Unit = stub()
    public actual fun setHttpResponseCode(responseCode: Int): Unit = stub()
    public actual fun setRequestPayloadSize(bytes: Long): Unit = stub()
    public actual fun setResponseContentType(contentType: String?): Unit = stub()
    public actual fun setResponsePayloadSize(bytes: Long): Unit = stub()
    public actual fun start(): Unit = stub()
    public actual fun stop(): Unit = stub()

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
