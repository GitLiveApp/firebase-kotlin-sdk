package dev.gitlive.firebase.perf.metrics

public actual class Trace {
    public actual fun start() {
    }

    public actual fun stop() {
    }

    public actual fun getLongMetric(metricName: String): Long {
        TODO("Not yet implemented")
    }

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
    }

    public actual fun putMetric(metricName: String, value: Long) {
    }
}
