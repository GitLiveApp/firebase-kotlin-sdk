package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

public val Trace.ios: FIRTrace? get() = ios

public actual class Trace internal constructor(internal val ios: FIRTrace?) {

    public actual fun start() {
        ios?.start()
    }

    public actual fun stop() {
        ios?.stop()
    }

    public actual fun getLongMetric(metricName: String): Long = ios?.valueForIntMetric(metricName) ?: 0L

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
        ios?.incrementMetric(metricName, incrementBy)
    }

    public actual fun putMetric(metricName: String, value: Long) {
        ios?.setIntValue(value, metricName)
    }

    public actual fun getAttribute(attribute: String): String? {
        TODO("Not yet implemented")
    }

    public actual fun getAttributes(): Map<String, String> {
        TODO("Not yet implemented")
    }

    public actual fun putAttribute(attribute: String, value: String) {
        TODO("Not yet implemented")
    }

    public actual fun removeAttribute(attribute: String) {
        TODO("Not yet implemented")
    }
}
