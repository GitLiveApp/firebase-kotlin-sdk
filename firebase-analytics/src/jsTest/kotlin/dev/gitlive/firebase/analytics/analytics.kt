package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

actual val context: Any = Unit

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

class FirebaseAnalyticsJsTest {

    private lateinit var analytics: FirebaseAnalytics
    private lateinit var app: FirebaseApp

    companion object {
        // A fresh instance of the test class is created per test, so the counter has
        // to live here for the generated app names to stay unique across the run.
        private var nextAppId = 0
    }

    @BeforeTest
    fun initializeFirebase() {
        app = Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
            "analyticsJsTest${nextAppId++}",
        )

        analytics = Firebase.analytics(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
    }

    @Test
    fun setUserPropertyShouldNotCrash() {
        analytics.setUserProperty("test_property", "test_value")
    }
}
