/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

/** @property ios The underlying Firebase iOS SDK object; null when the iOS SDK refused the trace name. */
public actual class Trace internal constructor(public val ios: FIRTrace?) {
    public actual fun start() {
        ios?.start()
    }

    public actual fun stop() {
        ios?.stop()
    }

    public actual fun getAttribute(attribute: String): String? = ios?.valueForAttribute(attribute)

    public actual val attributes: Map<String, String>
        get() = ios?.attributes?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() }.orEmpty()

    public actual fun getLongMetric(metricName: String): Long = ios?.valueForIntMetric(metricName) ?: 0L

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
        ios?.incrementMetric(metricName, incrementBy)
    }

    public actual fun putAttribute(attribute: String, value: String) {
        ios?.setValue(value, attribute)
    }

    public actual fun putMetric(metricName: String, value: Long) {
        ios?.setIntValue(value, metricName)
    }

    public actual fun removeAttribute(attribute: String) {
        ios?.removeAttribute(attribute)
    }

    override fun equals(other: Any?): Boolean = other is Trace && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "Trace($ios)"

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
