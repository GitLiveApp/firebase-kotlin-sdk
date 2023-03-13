package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.perf.metrics.Trace

actual val Firebase.performance get() =
    FirebasePerformance(com.google.firebase.perf.FirebasePerformance.getInstance())

actual fun Firebase.performance(app: FirebaseApp) =
    FirebasePerformance(com.google.firebase.perf.FirebasePerformance.getInstance())

actual class FirebasePerformance(val android: com.google.firebase.perf.FirebasePerformance){

    actual fun newTrace(traceName: String): Trace = Trace(android.newTrace(traceName))
}