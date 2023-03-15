package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.firebase
import dev.gitlive.firebase.perf.session.PerfSession


actual class Trace internal constructor(private val js: firebase.performance.Trace) {

    actual fun start() = js.start()

    actual fun stop() = js.stop()
    actual fun getLongMetric(metricName: String) = js.getMetric(metricName).toLong()

    actual fun incrementMetric(metricName: String, incrementBy: Long) = js.incrementMetric(metricName, incrementBy as Number)

    actual fun putMetric(metricName: String, value: Long) = js.putMetric(metricName, value as Number)

    actual fun getAttributes(): Map<String, String> = js.getAttributes()

    actual fun getAttribute(attribute: String): String? = js.getAttribute(attribute)

    actual fun putAttribute(attribute: String, value: String) = js.putAttribute(attribute, value)

    actual fun removeAttribute(attribute: String) = js.removeAttribute(attribute)
    actual fun updateSession(session: PerfSession) {
    }
}
