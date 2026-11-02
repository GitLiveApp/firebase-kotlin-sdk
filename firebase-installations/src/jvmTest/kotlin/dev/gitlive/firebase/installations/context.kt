package dev.gitlive.firebase.installations

import android.content.Context
import org.junit.Ignore

actual val context: Context = dev.gitlive.firebase.testContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

actual typealias IgnoreForJvm = Ignore
