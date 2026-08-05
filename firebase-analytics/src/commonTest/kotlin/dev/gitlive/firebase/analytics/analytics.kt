/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseAnalyticsTest {

    lateinit var analytics: FirebaseAnalytics

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
    fun testAnalyticsShouldNotCrash() {
        assertNotNull(analytics)

        // This should not crash, otherwise the test will fail
        analytics.logEvent("test") {
            param("key", "value")
        }
    }
}

class FirebaseAnalyticsParametersTest {

    @Test
    fun storesSupportedParameterTypes() {
        val parameters = FirebaseAnalyticsParameters().apply {
            param("string", "value")
            param("double", 1.5)
            param("long", 2L)
            param("int", 3)
            param("boolean", true)
        }

        assertEquals(
            mapOf<String, Any>(
                "string" to "value",
                "double" to 1.5,
                "long" to 2L,
                "int" to 3,
                "boolean" to true,
            ),
            parameters.parameters,
        )
    }
}

class FirebaseAnalyticsConsentBuilderTest {

    @Test
    fun storesSupportedConsentSettings() {
        val builder = FirebaseAnalyticsConsentBuilder().apply {
            adPersonalization = FirebaseAnalytics.ConsentStatus.GRANTED
            adStorage = FirebaseAnalytics.ConsentStatus.DENIED
            adUserData = FirebaseAnalytics.ConsentStatus.GRANTED
            analyticsStorage = FirebaseAnalytics.ConsentStatus.DENIED
        }

        assertEquals(
            mapOf(
                FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.GRANTED,
                FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.GRANTED,
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
            ),
            builder.consentSettings,
        )
    }
}
