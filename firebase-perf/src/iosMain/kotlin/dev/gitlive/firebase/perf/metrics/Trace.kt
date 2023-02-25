package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

actual class Trace internal constructor(val ios: FIRTrace?) {

    actual fun start() {
        ios?.start()
    }

    actual fun stop() {
        ios?.stop()
    }
}