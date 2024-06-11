package dev.gitlive.firebase.perf

import cocoapods.FirebasePerformance.FIRPerformance
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

actual val Firebase.performance get() =
    FirebasePerformance(FIRPerformance.sharedInstance())

actual fun Firebase.performance(app: FirebaseApp) =
    FirebasePerformance(FIRPerformance.sharedInstance())

actual class FirebasePerformance(val ios: FIRPerformance) {

    actual fun newTrace(traceName: String): Trace = Trace(ios.traceWithName(traceName))

    actual fun isPerformanceCollectionEnabled(): Boolean = ios.isDataCollectionEnabled()

    actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        ios.dataCollectionEnabled = enable
    }
}

actual open class FirebasePerformanceException(message: String) : FirebaseException(message)
