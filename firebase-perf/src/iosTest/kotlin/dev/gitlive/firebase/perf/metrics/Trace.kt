package dev.gitlive.firebase.perf.metrics

import kotlinx.coroutines.*
import platform.Foundation.*

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking {
    val testRun = MainScope().async { test() }
    while (testRun.isActive) {
        NSRunLoop.mainRunLoop.runMode(
            NSDefaultRunLoopMode,
            beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate())
        )
        yield()
    }
    testRun.await()
}