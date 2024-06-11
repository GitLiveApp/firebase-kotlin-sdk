package dev.gitlive.firebase.perf.metrics

expect class Trace {

    fun start()
    fun stop()
    fun getLongMetric(metricName: String): Long
    fun incrementMetric(metricName: String, incrementBy: Long)
    fun putMetric(metricName: String, value: Long)
}
