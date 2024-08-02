package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.session.PerfSession

public actual class Trace internal constructor(private val android: Trace) {

    public actual fun start() {
        android.start()
    }

    public actual fun stop() {
        android.stop()
    }

    public actual fun getLongMetric(metricName: String): Long = android.getLongMetric(metricName)

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
        android.incrementMetric(metricName, incrementBy)
    }

    public actual fun putMetric(metricName: String, value: Long) {
        android.putMetric(metricName, value)
    }

    public fun getAttributes(): Map<String, String> = android.attributes

    public fun getAttribute(attribute: String): String? = android.getAttribute(attribute)

    public fun putAttribute(attribute: String, value: String) {
        android.putAttribute(attribute, value)
    }

    public fun removeAttribute(attribute: String) {
        android.removeAttribute(attribute)
    }

    public fun updateSession(session: PerfSession) {
        android.updateSession(session.android)
    }
}
