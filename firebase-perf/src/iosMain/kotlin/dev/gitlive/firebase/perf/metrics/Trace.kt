package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

actual class Trace internal constructor(val ios: FIRTrace?) {

    actual fun start() {
        ios?.start()
    }

    actual fun stop() {
        ios?.stop()
    }

    actual fun getLongMetric(metricName: String): Long = ios?.valueForIntMetric(metricName) ?: 0L

    actual fun incrementMetric(metricName: String, incrementBy: Long) {
        ios?.incrementMetric(metricName, incrementBy)
    }

    actual fun putMetric(metricName: String, value: Long) {
        ios?.setIntValue(value, metricName)
    }

    actual fun getAttributes(): Map<String, String> = ios?.attributes as? Map<String, String> ?: mutableMapOf()

    actual fun getAttribute(attribute: String): String? = ios?.valueForAttribute(attribute)

    actual fun putAttribute(attribute: String, value: String) {
        ios?.setValue(attribute, value)
    }

    actual fun removeAttribute(attribute: String) {
        ios?.removeAttribute(attribute)
    }

    actual fun updateSession(session: dev.gitlive.firebase.perf.session.PerfSession) {
    }
}