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
}
