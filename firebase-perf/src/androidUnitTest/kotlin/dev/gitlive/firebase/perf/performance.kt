@file:JvmName("tests")
package dev.gitlive.firebase.perf

import kotlinx.coroutines.CoroutineScope
import org.junit.Ignore

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = ""

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest { test() }
actual typealias IgnoreForAndroidUnitTest = Ignore
