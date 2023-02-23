package dev.gitlive.firebase.perf

import cocoapods.FirebasePerformance.FIRPerformance
import dev.gitlive.firebase.perf.metrics.Trace

actual class Performance {

    actual fun newTrace(traceName: String): Trace = Trace(FIRPerformance.sharedInstance().traceWithName(traceName))
}