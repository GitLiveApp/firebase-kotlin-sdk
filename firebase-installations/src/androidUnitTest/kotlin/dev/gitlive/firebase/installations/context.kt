package dev.gitlive.firebase.installations

import android.content.Context
import org.junit.Ignore

actual val context: Context get() = throw UnsupportedOperationException("Android unit tests have no Context; the Firebase tests are ignored there")

actual typealias IgnoreForAndroidUnitTest = Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForJvm
