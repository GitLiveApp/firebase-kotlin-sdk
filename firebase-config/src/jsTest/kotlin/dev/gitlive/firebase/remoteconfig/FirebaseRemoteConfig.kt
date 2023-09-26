package dev.gitlive.firebase.remoteconfig

import kotlin.time.Duration.Companion.minutes


actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
