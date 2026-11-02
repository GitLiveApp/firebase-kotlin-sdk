/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import com.google.firebase.perf.session.PerfSession
import dev.gitlive.firebase.perf.stub
import kotlin.jvm.JvmField

/** Header stub; see FirebasePerformance.android.kt. */
public actual class Trace private constructor() {
    public actual fun start(): Unit = stub()
    public actual fun stop(): Unit = stub()
    public actual fun getAttribute(attribute: String): String? = stub()
    public actual val attributes: Map<String, String> get() = stub()
    public actual fun getLongMetric(metricName: String): Long = stub()
    public actual fun incrementMetric(metricName: String, incrementBy: Long): Unit = stub()
    public actual fun putAttribute(attribute: String, value: String): Unit = stub()
    public actual fun putMetric(metricName: String, value: Long): Unit = stub()
    public actual fun removeAttribute(attribute: String): Unit = stub()

    /** Android-only (hidden in the SDK's API); reached by the `dev.gitlive` wrapper's `Trace.updateSession`. */
    public fun updateSession(session: PerfSession): Unit = stub()

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
