/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.Trace as CompatTrace

/**
 * A custom trace.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.perf.metrics.Trace] this wraps.
 */
public class Trace internal constructor(public val compat: CompatTrace) {
    /** Starts this trace. */
    public fun start() {
        compat.start()
    }

    /** Stops this trace. */
    public fun stop() {
        compat.stop()
    }

    /** Returns the value of the custom attribute with the given [attribute] name. */
    public fun getAttribute(attribute: String): String? = compat.getAttribute(attribute)

    /** Returns all custom attributes associated with this trace. */
    public fun getAttributes(): Map<String, String> = compat.attributes

    /**
     * Gets the value of the metric with the given name in the current trace. If a metric with the
     * given name doesn't exist, it is NOT created and a 0 is returned. This method is atomic.
     *
     * @param metricName Name of the metric to get. Requires no leading or trailing whitespace, no
     *     leading underscore '_' character, max length is 100 characters.
     * @return Value of the metric or 0 if it hasn't yet been set.
     */
    public fun getLongMetric(metricName: String): Long = compat.getLongMetric(metricName)

    /**
     * Atomically increments the metric with the given name in this trace by the incrementBy value. If
     * the metric does not exist, a new one will be created. If the trace has not been started or has
     * already been stopped, returns immediately without taking action.
     *
     * @param metricName Name of the metric to be incremented. Requires no leading or trailing
     *     whitespace, no leading underscore [_] character, max length of 100 characters.
     * @param incrementBy Amount by which the metric has to be incremented.
     */
    public fun incrementMetric(metricName: String, incrementBy: Long) {
        compat.incrementMetric(metricName, incrementBy)
    }

    /** Sets a custom [attribute] to the given [value] on this trace. */
    public fun putAttribute(attribute: String, value: String) {
        compat.putAttribute(attribute, value)
    }

    /**
     * Sets the value of the metric with the given name in this trace to the value provided. If a
     * metric with the given name doesn't exist, a new one will be created. If the trace has not been
     * started or has already been stopped, returns immediately without taking action. This method is
     * atomic.
     *
     * @param metricName Name of the metric to set. Requires no leading or trailing whitespace, no
     *     leading underscore '_' character, max length is 100 characters.
     * @param value The value to which the metric should be set to.
     */
    public fun putMetric(metricName: String, value: Long) {
        compat.putMetric(metricName, value)
    }

    /** Removes the custom attribute with the given [attribute] name from this trace. */
    public fun removeAttribute(attribute: String) {
        compat.removeAttribute(attribute)
    }

    override fun equals(other: Any?): Boolean = other is Trace && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "Trace($compat)"
}
