package dev.gitlive.firebase.installations

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

actual val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForJvm
