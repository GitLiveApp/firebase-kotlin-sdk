package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.firebase


actual class Trace internal constructor(private val js: firebase.performance.Trace) {

    actual fun start() = js.start()

    actual fun stop() = js.stop()
}
