package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
public actual val Firebase.performance: FirebasePerformance
    get() = TODO("Not yet implemented")

/** Returns the [FirebasePerformance] instance of a given [FirebaseApp]. */
public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance {
    TODO("Not yet implemented")
}

public actual class FirebasePerformance {
    public actual fun newTrace(traceName: String): Trace {
        TODO("Not yet implemented")
    }

    public actual fun isPerformanceCollectionEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
    }
}

public actual open class FirebasePerformanceException internal constructor(message: String) : FirebaseException(message)
