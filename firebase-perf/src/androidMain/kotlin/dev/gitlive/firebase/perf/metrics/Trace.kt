package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.perf.session.PerfSession
import com.google.firebase.perf.metrics.Trace as AndroidTrace

public val Trace.android: AndroidTrace get() = android

public actual class Trace internal constructor(internal val android: AndroidTrace) {

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

    public actual fun getAttributes(): Map<String, String> = android.attributes

    public actual fun getAttribute(attribute: String): String? = android.getAttribute(attribute)

    public actual fun putAttribute(attribute: String, value: String) {
        android.putAttribute(attribute, value)
    }

    public actual fun removeAttribute(attribute: String) {
        android.removeAttribute(attribute)
    }

    public fun updateSession(session: PerfSession) {
        android.updateSession(session.android)
    }
}
