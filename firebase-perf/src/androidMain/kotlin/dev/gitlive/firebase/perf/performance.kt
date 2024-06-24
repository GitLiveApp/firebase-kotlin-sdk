package dev.gitlive.firebase.perf

import com.google.firebase.FirebaseException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.perf.metrics.Trace

public actual val Firebase.performance: FirebasePerformance get() =
    FirebasePerformance(com.google.firebase.perf.FirebasePerformance.getInstance())

public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance =
    FirebasePerformance(app.android.get(com.google.firebase.perf.FirebasePerformance::class.java))

public actual class FirebasePerformance(public val android: com.google.firebase.perf.FirebasePerformance) {

    public actual fun newTrace(traceName: String): Trace = Trace(android.newTrace(traceName))

    public actual fun isPerformanceCollectionEnabled(): Boolean = android.isPerformanceCollectionEnabled

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        android.isPerformanceCollectionEnabled = enable
    }
}

public actual open class FirebasePerformanceException(message: String) : FirebaseException(message)
