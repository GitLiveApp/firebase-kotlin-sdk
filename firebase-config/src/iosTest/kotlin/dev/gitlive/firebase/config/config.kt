package dev.gitlive.firebase.config

import kotlinx.coroutines.runBlocking

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }