package dev.gitlive.firebase.installations

import org.junit.Ignore

actual val context: Any = ""

actual typealias IgnoreForAndroidUnitTest = Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForJvm
