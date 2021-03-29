package dev.gitlive.firebase.config

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }