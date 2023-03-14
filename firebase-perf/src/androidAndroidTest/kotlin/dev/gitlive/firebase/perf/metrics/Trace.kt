
@file:JvmName("tests")
package dev.gitlive.firebase.perf.metrics

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking { test() }