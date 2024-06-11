package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
expect val Firebase.performance: FirebasePerformance

/** Returns the [FirebasePerformance] instance of a given [FirebaseApp]. */
expect fun Firebase.performance(app: FirebaseApp): FirebasePerformance

expect class FirebasePerformance {

    fun newTrace(traceName: String): Trace

    fun isPerformanceCollectionEnabled(): Boolean

    fun setPerformanceCollectionEnabled(enable: Boolean)
}

expect open class FirebasePerformanceException : FirebaseException
