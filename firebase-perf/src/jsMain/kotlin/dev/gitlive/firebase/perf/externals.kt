@file:JsModule("firebase/performance")
@file:JsNonModule

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.externals.app.FirebaseApp

external fun getPerformance(app: FirebaseApp? = definedExternally): JsFirebasePerformance

external fun trace(performance: JsFirebasePerformance, name: String): JsPerformanceTrace

external interface JsFirebasePerformance {
    var dataCollectionEnabled: Boolean
    var instrumentationEnabled: Boolean
}

external interface JsPerformanceTrace {
    fun getAttribute(attr: String): String?
    fun getAttributes(): Map<String, String>
    fun getMetric(metricName: String): Int
    fun incrementMetric(metricName: String, num: Int? = definedExternally)
    fun putAttribute(attr: String, value: String)
    fun putMetric(metricName: String, num: Int)
    fun removeAttribute(attr: String)
    fun start()
    fun stop()
}
