package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.HttpMetric as AndroidHttpMetric

public val HttpMetric.android: AndroidHttpMetric get() = android

public actual class HttpMetric(internal val android: AndroidHttpMetric) {
    public actual fun getAttribute(attribute: String): String? = android.getAttribute(attribute)

    public actual fun getAttributes(): Map<String, String> = android.attributes

    public actual fun putAttribute(attribute: String, value: String) {
        android.putAttribute(attribute, value)
    }

    public actual fun removeAttribute(attribute: String) {
        android.removeAttribute(attribute)
    }

    public actual fun setHttpResponseCode(responseCode: Int) {
        android.setHttpResponseCode(responseCode)
    }

    public actual fun setRequestPayloadSize(bytes: Long) {
        android.setRequestPayloadSize(bytes)
    }

    public actual fun setResponseContentType(contentType: String) {
        android.setResponseContentType(contentType)
    }

    public actual fun setResponsePayloadSize(bytes: Long) {
        android.setRequestPayloadSize(bytes)
    }

    public actual fun start() {
        android.start()
    }

    public actual fun stop() {
        android.stop()
    }

    // TODO: android.markRequestComplete()
    // TODO: android.markRequestStart()
}