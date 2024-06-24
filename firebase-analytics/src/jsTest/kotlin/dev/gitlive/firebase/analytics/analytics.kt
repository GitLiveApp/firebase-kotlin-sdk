package dev.gitlive.firebase.analytics

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = Unit

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
