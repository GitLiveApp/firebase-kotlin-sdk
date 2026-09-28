/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import kotlin.jvm.JvmField

/** An in-memory trace (see FirebasePerformance.jvm.kt). @property name The name given to [com.google.firebase.perf.FirebasePerformance.newTrace]. */
public actual class Trace internal constructor(public val name: String) {
    private val metrics = mutableMapOf<String, Long>()
    private val customAttributes = mutableMapOf<String, String>()
    private var started = false
    private var stopped = false
    private val isRunning get() = started && !stopped

    public actual fun start() {
        started = true
    }

    public actual fun stop() {
        if (started) stopped = true
    }

    public actual fun getAttribute(attribute: String): String? = customAttributes[attribute]

    public actual val attributes: Map<String, String> get() = customAttributes.toMap()

    public actual fun getLongMetric(metricName: String): Long = metrics[metricName] ?: 0L

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
        if (isRunning) metrics[metricName] = getLongMetric(metricName) + incrementBy
    }

    public actual fun putAttribute(attribute: String, value: String) {
        customAttributes[attribute] = value
    }

    public actual fun putMetric(metricName: String, value: Long) {
        if (isRunning) metrics[metricName] = value
    }

    public actual fun removeAttribute(attribute: String) {
        customAttributes.remove(attribute)
    }

    override fun toString(): String = "Trace($name)"

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
