package dev.gitlive.firebase.perf

import com.google.firebase.FirebaseException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.perf.metrics.Trace

actual val Firebase.performance get() =
    FirebasePerformance(com.google.firebase.perf.FirebasePerformance.getInstance())

actual fun Firebase.performance(app: FirebaseApp) =
    FirebasePerformance(app.android.get(com.google.firebase.perf.FirebasePerformance::class.java))

actual class FirebasePerformance(val android: com.google.firebase.perf.FirebasePerformance) {

    actual fun newTrace(traceName: String): Trace = Trace(android.newTrace(traceName))

    actual fun isPerformanceCollectionEnabled() = android.isPerformanceCollectionEnabled

    actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        android.isPerformanceCollectionEnabled = enable
    }
}

actual open class FirebasePerformanceException(message: String) : FirebaseException(message)
