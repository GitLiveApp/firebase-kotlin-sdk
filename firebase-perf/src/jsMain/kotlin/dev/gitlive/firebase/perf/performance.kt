package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.perf.metrics.Trace

actual val Firebase.performance: FirebasePerformance
    get() = TODO()

actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance = TODO()

actual class FirebasePerformance {

    actual fun newTrace(traceName: String): Trace = TODO()
}