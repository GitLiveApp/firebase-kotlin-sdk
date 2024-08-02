@file:JsModule("firebase/performance")
@file:JsNonModule

package dev.gitlive.firebase.perf.externals

import dev.gitlive.firebase.externals.FirebaseApp

public external fun getPerformance(app: FirebaseApp? = definedExternally): FirebasePerformance

public external fun trace(performance: FirebasePerformance, name: String): PerformanceTrace

public external interface FirebasePerformance {
    public var dataCollectionEnabled: Boolean
    public var instrumentationEnabled: Boolean
}

public external interface PerformanceTrace {
    public fun getAttribute(attr: String): String?
    public fun getAttributes(): Map<String, String>
    public fun getMetric(metricName: String): Int
    public fun incrementMetric(metricName: String, num: Int? = definedExternally)
    public fun putAttribute(attr: String, value: String)
    public fun putMetric(metricName: String, num: Int)
    public fun removeAttribute(attr: String)
    public fun start()
    public fun stop()
}
