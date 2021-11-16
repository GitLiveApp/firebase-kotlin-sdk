package dev.gitlive.firebase.remoteconfig

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = GlobalScope
    .promise {
        try {
            test()
        } catch (e: Throwable) {
            e.log()
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
