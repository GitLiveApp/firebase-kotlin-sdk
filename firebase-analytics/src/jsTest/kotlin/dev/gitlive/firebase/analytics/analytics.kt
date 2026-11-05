package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
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

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )

        analytics = Firebase.analytics(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun setUserPropertyShouldNotCrash() {
        analytics.setUserProperty("test_property", "test_value")
    }
}
