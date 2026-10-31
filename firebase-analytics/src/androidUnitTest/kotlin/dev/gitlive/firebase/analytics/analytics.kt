package dev.gitlive.firebase.analytics

import android.content.Context
import org.junit.Ignore

actual val context: Context get() = throw UnsupportedOperationException("Android unit tests have no Context; the Firebase tests are ignored there")

actual typealias IgnoreForAndroidUnitTest = Ignore
