package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.HttpMetric
import dev.gitlive.firebase.perf.metrics.Trace

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
public actual val Firebase.performance: FirebasePerformance
    get() = error("Not supported in JVM")

/** Returns the [FirebasePerformance] instance of a given [FirebaseApp]. */
public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance {
    error("Not supported in JVM")
}

public actual class FirebasePerformance {
    public actual fun newTrace(traceName: String): Trace {
        error("Not supported in JVM")
    }

    public actual fun isPerformanceCollectionEnabled(): Boolean {
        error("Not supported in JVM")
    }

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        error("Not supported in JVM")
    }

    public actual fun newHttpMetric(url: String, httpMethod: String): HttpMetric {
        error("Not supported in JVM")
    }
}

public actual open class FirebasePerformanceException internal constructor(message: String) : FirebaseException(message)
