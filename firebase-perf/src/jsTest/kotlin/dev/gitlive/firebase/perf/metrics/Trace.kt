package dev.gitlive.firebase.perf.metrics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = GlobalScope
    .promise {
        try {
            test()
        } catch (e: dynamic) {
            (e as? Throwable)?.log()
            throw e
        }
    }.asDynamic()

internal fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}