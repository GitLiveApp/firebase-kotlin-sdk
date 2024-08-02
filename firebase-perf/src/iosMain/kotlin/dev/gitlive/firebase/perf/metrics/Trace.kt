package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

public actual class Trace internal constructor(public val ios: FIRTrace?) {

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
}
