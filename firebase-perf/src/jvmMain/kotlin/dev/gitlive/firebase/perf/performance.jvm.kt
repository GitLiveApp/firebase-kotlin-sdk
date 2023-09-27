package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
actual val Firebase.performance: FirebasePerformance
    get() = TODO("Not yet implemented")

/** Returns the [FirebasePerformance] instance of a given [FirebaseApp]. */
actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance {
    TODO("Not yet implemented")
}

actual class FirebasePerformance {
    actual fun newTrace(traceName: String): Trace {
        TODO("Not yet implemented")
    }

    actual fun isPerformanceCollectionEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun setPerformanceCollectionEnabled(enable: Boolean) {
    }

}

actual open class FirebasePerformanceException : FirebaseException()