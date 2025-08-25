package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRHTTPMetric
import cocoapods.FirebasePerformance.FIRHTTPMethod
import platform.Foundation.NSDictionary
import platform.Foundation.NSURL

public val HttpMetric.ios: FIRHTTPMetric? get() = ios

public actual class HttpMetric internal constructor(
    internal val ios: FIRHTTPMetric?
) {

    public actual fun start() {
        ios?.start()
    }

    public actual fun stop() {
        ios?.stop()
    }

    public actual fun getAttribute(attribute: String): String? =
        ios?.valueForAttribute(attribute)

    public actual fun getAttributes(): Map<String, String> {
        val attributesDict = ios?.attributes
        return attributesDict?.let { dict ->
            dict.keys.map { key ->
                key.toString() to dict[key]?.toString().orEmpty()
            }.toMap()
        } ?: emptyMap()
    }

    public actual fun putAttribute(attribute: String, value: String) {
        ios?.setValue(value, attribute)
    }

    public actual fun removeAttribute(attribute: String) {
        ios?.removeAttribute(attribute)
    }

    public actual fun setHttpResponseCode(responseCode: Int) {
        ios?.responseCode = responseCode.toLong()
    }

    public actual fun setRequestPayloadSize(bytes: Long) {
        ios?.requestPayloadSize = bytes
    }

    public actual fun setResponseContentType(contentType: String) {
        ios?.responseContentType = contentType
    }

    public actual fun setResponsePayloadSize(bytes: Long) {
        ios?.responsePayloadSize = bytes
    }
}
