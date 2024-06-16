@file:JsModule("firebase/performance")
@file:JsNonModule

package dev.gitlive.firebase.perf.externals

import dev.gitlive.firebase.externals.FirebaseApp

external fun getPerformance(app: FirebaseApp? = definedExternally): FirebasePerformance

external fun trace(performance: FirebasePerformance, name: String): PerformanceTrace

external interface FirebasePerformance {
    var dataCollectionEnabled: Boolean
    var instrumentationEnabled: Boolean
}

external interface PerformanceTrace {
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
