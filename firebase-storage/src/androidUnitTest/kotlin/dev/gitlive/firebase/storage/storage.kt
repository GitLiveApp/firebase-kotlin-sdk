package dev.gitlive.firebase.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = ""

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking { test() }
