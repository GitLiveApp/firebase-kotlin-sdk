package dev.gitlive.firebase.analytics

import android.content.Context

actual val context: Context = Context()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
