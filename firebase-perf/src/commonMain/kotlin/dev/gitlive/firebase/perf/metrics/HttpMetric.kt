package dev.gitlive.firebase.perf.metrics

public expect class HttpMetric {
    public fun getAttribute(attribute: String): String?
    public fun getAttributes(): Map<String, String>
    public fun putAttribute(attribute: String, value: String)
    public fun removeAttribute(attribute: String)
    public fun setHttpResponseCode(responseCode: Int)
    public fun setRequestPayloadSize(bytes: Long)
    public fun setResponseContentType(contentType: String)
    public fun setResponsePayloadSize(bytes: Long)
    public fun start()
    public fun stop()
}