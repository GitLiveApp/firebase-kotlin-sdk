package dev.gitlive.firebase.installations

import org.junit.Ignore

actual val context: Any = dev.gitlive.firebase.testContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

actual typealias IgnoreForJvm = Ignore
