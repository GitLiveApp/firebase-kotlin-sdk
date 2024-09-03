package dev.gitlive.firebase.firestore

import kotlin.test.Ignore

actual typealias IgnoreJs = Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
