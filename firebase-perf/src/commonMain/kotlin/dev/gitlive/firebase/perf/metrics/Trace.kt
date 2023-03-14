package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.perf.session.PerfSession

expect class Trace {

    fun start()
    fun stop()
    fun getLongMetric(metricName: String): Long
    fun incrementMetric(metricName: String, incrementBy: Long)
    fun putMetric(metricName: String, value: Long)
    fun getAttributes(): Map<String, String>
    fun getAttribute(attribute: String): String?
    fun putAttribute(attribute: String, value: String)
    fun removeAttribute(attribute: String)
    fun updateSession(session: PerfSession)
}