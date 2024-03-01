package dev.gitlive.firebase.firestore

import org.junit.Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreJs
actual typealias IgnoreForAndroidUnitTest = Ignore
