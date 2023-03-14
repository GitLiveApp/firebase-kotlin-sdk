package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.metrics.Trace

actual class Trace internal constructor(private val android: Trace) {

    actual fun start() = android.start()

    actual fun stop() = android.stop()
}