package dev.gitlive.firebase.perf.metrics

public actual class HttpMetric {
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

    public actual fun setHttpResponseCode(responseCode: Int) {
        error("Not supported in JVM")
    }

    public actual fun setRequestPayloadSize(bytes: Long) {
        error("Not supported in JVM")
    }

    public actual fun setResponseContentType(contentType: String) {
        error("Not supported in JVM")
    }

    public actual fun setResponsePayloadSize(bytes: Long) {
        error("Not supported in JVM")
    }

    public actual fun start() {
        error("Not supported in JVM")
    }

    public actual fun stop() {
        error("Not supported in JVM")
    }
}