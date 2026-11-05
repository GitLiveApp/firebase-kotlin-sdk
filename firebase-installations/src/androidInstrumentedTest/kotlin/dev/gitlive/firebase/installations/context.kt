package dev.gitlive.firebase.installations

import androidx.test.platform.app.InstrumentationRegistry

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForJvm
