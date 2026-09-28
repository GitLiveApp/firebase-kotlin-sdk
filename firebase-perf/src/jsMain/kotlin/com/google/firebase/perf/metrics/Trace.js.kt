/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

import com.google.firebase.perf.rethrow
import dev.gitlive.firebase.perf.externals.PerformanceTrace
import kotlin.js.Json

/** @property js The underlying Firebase JS SDK object. */
public actual class Trace internal constructor(public val js: PerformanceTrace) {
    public actual fun start(): Unit = rethrow { js.start() }

    public actual fun stop(): Unit = rethrow { js.stop() }

    public actual fun getAttribute(attribute: String): String? = rethrow { js.getAttribute(attribute) }

    public actual val attributes: Map<String, String>
        get() = rethrow {
            val entries = js("Object.entries") as (Json) -> Array<Array<String>>
            entries(js.getAttributes()).associate { entry -> entry[0] to entry[1] }
        }

    public actual fun getLongMetric(metricName: String): Long = rethrow { js.getMetric(metricName).toLong() }

    public actual fun incrementMetric(metricName: String, incrementBy: Long): Unit = rethrow { js.incrementMetric(metricName, incrementBy.toInt()) }

    public actual fun putAttribute(attribute: String, value: String): Unit = rethrow { js.putAttribute(attribute, value) }

    public actual fun putMetric(metricName: String, value: Long): Unit = rethrow { js.putMetric(metricName, value.toInt()) }

    public actual fun removeAttribute(attribute: String): Unit = rethrow { js.removeAttribute(attribute) }

    override fun equals(other: Any?): Boolean = other is Trace && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "Trace($js)"

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
