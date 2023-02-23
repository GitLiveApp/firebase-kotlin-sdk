package dev.gitlive.firebase.perf

import dev.gitlive.firebase.perf.metrics.Trace

expect class Performance {

    fun newTrace(traceName: String): Trace
}