package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.externals.jsObjectEntries
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.perf.externals.PerformanceTrace
import dev.gitlive.firebase.perf.rethrow

public val Trace.js: PerformanceTrace get() = js

public actual class Trace internal constructor(internal val js: PerformanceTrace) {

    public actual fun start(): Unit = rethrow { js.start() }
    public actual fun stop(): Unit = rethrow { js.stop() }
    public actual fun getLongMetric(metricName: String): Long = rethrow { js.getMetric(metricName).toLong() }
    public actual fun incrementMetric(metricName: String, incrementBy: Long): Unit = rethrow { js.incrementMetric(metricName, incrementBy.toInt()) }
    public actual fun putMetric(metricName: String, value: Long): Unit = rethrow { js.putMetric(metricName, value.toInt()) }
    public actual fun getAttribute(attribute: String): String? = rethrow { js.getAttribute(attribute) }
    public actual fun getAttributes(): Map<String, String> = rethrow {
        val entries = jsObjectEntries(js.getAttributes())
        buildMap {
            for (index in 0 until entries.length) {
                val entry = entries[index]!!
                put(entry[0].toKotlin() as String, entry[1].toKotlin() as String)
            }
        }
    }
    public actual fun putAttribute(attribute: String, value: String): Unit = rethrow { js.putAttribute(attribute, value) }
    public actual fun removeAttribute(attribute: String): Unit = rethrow { js.removeAttribute(attribute) }
}
