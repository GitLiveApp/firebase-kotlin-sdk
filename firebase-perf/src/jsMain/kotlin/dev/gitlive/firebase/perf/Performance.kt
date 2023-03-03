package dev.gitlive.firebase.perf

import dev.gitlive.firebase.perf.metrics.Trace

actual class Performance {

    actual fun newTrace(traceName: String): Trace {
        return Trace()
    }
}