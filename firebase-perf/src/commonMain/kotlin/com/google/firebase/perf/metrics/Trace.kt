/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

/**
 * A custom trace: a period of time between [start] and [stop] that is reported with its metrics and custom attributes,
 * as the Android SDK's `com.google.firebase.perf.metrics.Trace`.
 */
public expect class Trace {
    /** Starts this trace. */
    public fun start()

    /** Stops this trace and reports it. */
    public fun stop()

    /** The value of the custom attribute [attribute], or null if it is not set. */
    public fun getAttribute(attribute: String): String?

    /** All custom attributes of this trace. */
    public val attributes: Map<String, String>

    /** The value of the metric [metricName], or 0 if it has not been set; the metric is not created. */
    public fun getLongMetric(metricName: String): Long

    /**
     * Atomically increments the metric [metricName] by [incrementBy], creating it if needed. Does nothing if the trace
     * has not been started or has already been stopped.
     */
    public fun incrementMetric(metricName: String, incrementBy: Long)

    /** Sets the custom attribute [attribute] to [value]. */
    public fun putAttribute(attribute: String, value: String)

    /**
     * Sets the metric [metricName] to [value], creating it if needed. Does nothing if the trace has not been started or
     * has already been stopped.
     */
    public fun putMetric(metricName: String, value: Long)

    /** Removes the custom attribute [attribute]. */
    public fun removeAttribute(attribute: String)

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
