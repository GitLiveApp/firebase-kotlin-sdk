package dev.gitlive.firebase.remoteconfig


actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest { test() }
