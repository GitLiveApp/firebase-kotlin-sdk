package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.session.PerfSession

actual class Trace internal constructor(private val android: Trace) {

    actual fun start() = android.start()

    actual fun stop() = android.stop()

    actual fun getLongMetric(metricName: String): Long = android.getLongMetric(metricName)

    actual fun incrementMetric(metricName: String, incrementBy: Long) = android.incrementMetric(metricName, incrementBy)

    actual fun putMetric(metricName: String, value: Long) = android.putMetric(metricName, value)

    fun getAttributes(): Map<String, String> = android.attributes

    fun getAttribute(attribute: String): String? = android.getAttribute(attribute)

    fun putAttribute(attribute: String, value: String) = android.putAttribute(attribute, value)

    fun removeAttribute(attribute: String) = android.removeAttribute(attribute)

    fun updateSession(session: PerfSession) = android.updateSession(session.android)
}
