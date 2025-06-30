package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.js
import dev.gitlive.firebase.perf.externals.getPerformance
import dev.gitlive.firebase.perf.externals.trace
import dev.gitlive.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.externals.FirebasePerformance as JsFirebasePerformance

public actual val Firebase.performance: FirebasePerformance
    get() = rethrow {
        FirebasePerformance(getPerformance())
    }

public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance = rethrow {
    FirebasePerformance(getPerformance(app.js))
}

public val FirebasePerformance.js: JsFirebasePerformance get() = js

public actual class FirebasePerformance internal constructor(internal val js: JsFirebasePerformance) {

    public actual fun newTrace(traceName: String): Trace = rethrow {
        Trace(trace(js, traceName))
    }

    public actual fun isPerformanceCollectionEnabled(): Boolean = js.dataCollectionEnabled

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        js.dataCollectionEnabled = enable
    }

    public fun isInstrumentationEnabled(): Boolean = js.instrumentationEnabled

    public fun setInstrumentationEnabled(enable: Boolean) {
        js.instrumentationEnabled = enable
    }
}

public actual open class FirebasePerformanceException(code: String, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic) = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code ->
        when {
            else -> {
                println("Unknown error code in ${JSON.stringify(error)}")
                FirebasePerformanceException(code, error as Throwable)
            }
        }
    }
