package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.perf.externals.PerformanceTrace
import dev.gitlive.firebase.perf.rethrow

actual class Trace internal constructor(private val js: PerformanceTrace) {

    actual fun start() = rethrow { js.start() }
    actual fun stop() = rethrow { js.stop() }
    actual fun getLongMetric(metricName: String) = rethrow { js.getMetric(metricName).toLong() }
    actual fun incrementMetric(metricName: String, incrementBy: Long) = rethrow { js.incrementMetric(metricName, incrementBy.toInt()) }
    actual fun putMetric(metricName: String, value: Long) = rethrow { js.putMetric(metricName, value.toInt()) }
    fun getAttribute(attribute: String): String? = rethrow { js.getAttribute(attribute) }
    fun putAttribute(attribute: String, value: String) = rethrow { js.putAttribute(attribute, value) }
    fun removeAttribute(attribute: String) = rethrow { js.removeAttribute(attribute) }

    fun primitiveHashMap(container: dynamic): HashMap<String, String> {
        val m = HashMap<String, String>().asDynamic()
        m.map = container
        val keys = js("Object.keys")
        m.`$size` = keys(container).length
        return m
    }
}
