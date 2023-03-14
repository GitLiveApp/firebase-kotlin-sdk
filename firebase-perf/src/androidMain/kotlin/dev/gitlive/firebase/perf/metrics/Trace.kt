package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.Trace

actual class Trace internal constructor(private val android: Trace) {

    actual fun start() = android.start()

    actual fun stop() = android.stop()

    actual fun getLongMetric(metricName: String): Long = android.getLongMetric(metricName)

    actual fun incrementMetric(metricName: String, incrementBy: Long) = android.incrementMetric(metricName, incrementBy)

    actual fun putMetric(metricName: String, value: Long) = android.putMetric(metricName, value)

    actual fun getAttributes(): Map<String, String> = android.attributes

    actual fun getAttribute(attribute: String): String? = android.getAttribute(attribute)

    actual fun putAttribute(attribute: String, value: String) = android.putAttribute(attribute, value)

    actual fun removeAttribute(attribute: String) = android.removeAttribute(attribute)

    actual fun updateSession(session: dev.gitlive.firebase.perf.session.PerfSession) = android.updateSession(session.android)
}