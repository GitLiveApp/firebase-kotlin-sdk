package dev.gitlive.firebase.perf.metrics

actual class Trace {
    actual fun start() {
    }

    actual fun stop() {
    }

    actual fun getLongMetric(metricName: String): Long {
        TODO("Not yet implemented")
    }

    actual fun incrementMetric(metricName: String, incrementBy: Long) {
    }

    actual fun putMetric(metricName: String, value: Long) {
    }
}
