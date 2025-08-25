package dev.gitlive.firebase.perf.metrics

public actual class Trace {
    public actual fun start() {
        error("Not supported in JVM")
    }

    public actual fun stop() {
        error("Not supported in JVM")
    }

    public actual fun getLongMetric(metricName: String): Long {
        error("Not supported in JVM")
    }

    public actual fun incrementMetric(metricName: String, incrementBy: Long) {
        error("Not supported in JVM")
    }

    public actual fun putMetric(metricName: String, value: Long) {
        error("Not supported in JVM")
    }

    public actual fun getAttribute(attribute: String): String? {
        error("Not supported in JVM")
    }

    public actual fun getAttributes(): Map<String, String> {
        error("Not supported in JVM")
    }

    public actual fun putAttribute(attribute: String, value: String) {
        error("Not supported in JVM")
    }

    public actual fun removeAttribute(attribute: String) {
        error("Not supported in JVM")
    }
}
