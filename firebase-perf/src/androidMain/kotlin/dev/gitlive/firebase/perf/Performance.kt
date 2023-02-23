package dev.gitlive.firebase.perf

import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import dev.gitlive.firebase.perf.metrics.Trace

actual class Performance {

    actual fun newTrace(traceName: String): Trace = Trace(Firebase.performance.newTrace(traceName))
}