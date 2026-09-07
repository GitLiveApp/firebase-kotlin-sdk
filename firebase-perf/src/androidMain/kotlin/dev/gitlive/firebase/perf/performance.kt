package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android as publicAndroid
import dev.gitlive.firebase.perf.metrics.Trace

public val FirebasePerformance.android: dev.gitlive.firebase.android.perf.FirebasePerformance get() = dev.gitlive.firebase.android.perf.FirebasePerformance.getInstance()

public actual val Firebase.performance: FirebasePerformance get() =
    FirebasePerformance(dev.gitlive.firebase.android.perf.FirebasePerformance.getInstance())

public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance = FirebasePerformance(app.publicAndroid.get(dev.gitlive.firebase.android.perf.FirebasePerformance::class.java))

public actual class FirebasePerformance(internal val android: dev.gitlive.firebase.android.perf.FirebasePerformance) {

    public actual fun newTrace(traceName: String): Trace = Trace(android.newTrace(traceName))

    public actual fun isPerformanceCollectionEnabled(): Boolean = android.isPerformanceCollectionEnabled

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        android.isPerformanceCollectionEnabled = enable
    }
}

public actual open class FirebasePerformanceException(message: String) : dev.gitlive.firebase.android.FirebaseException(message)
