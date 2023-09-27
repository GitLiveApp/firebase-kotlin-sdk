package dev.gitlive.firebase.database

actual val emulatorHost: String = "127.0.0.1" // in JS tests connection is refused if we use localhost

actual val context: Any = Unit

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
