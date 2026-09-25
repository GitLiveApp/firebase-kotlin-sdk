/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

import android.os.Bundle
import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.ConsentBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ParametersBuilder
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.analytics.setConsent
import com.google.firebase.getApps
import com.google.firebase.initialize
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.runBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Written against the Firebase Android SDK API only (`com.google.firebase.analytics.*` and `android.os.Bundle`), as an
 * Android app would be; running on every platform is what the source-compatibility layer promises. Not compiled for the
 * JVM, where analytics is a no-op and firebase-java-sdk's Bundle has no setters.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    @BeforeTest
    fun initializeFirebase() {
        if (Firebase.getApps(context).isEmpty()) {
            Firebase.initialize(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:846484016111:ios:dd1f6688bad7af768c841a")
                    .setApiKey("AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0")
                    .setDatabaseUrl("https://fir-kotlin-sdk.firebaseio.com")
                    .setStorageBucket("fir-kotlin-sdk.appspot.com")
                    .setProjectId("fir-kotlin-sdk")
                    .setGcmSenderId("846484016111")
                    .build(),
            )
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        dev.gitlive.firebase.Firebase.apps(context).forEach { it.delete() }
    }

    @Test
    fun testLogEventsAndProperties() = runBlockingTest {
        val analytics = Firebase.analytics
        assertEquals(analytics, Firebase.analytics)
        assertEquals(dev.gitlive.firebase.Firebase.analytics.compat, analytics)

        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, "email") })
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
            param(FirebaseAnalytics.Param.VALUE, 12.5)
            param(FirebaseAnalytics.Param.TRANSACTION_ID, "T123")
            param(FirebaseAnalytics.Param.QUANTITY, 2L)
            param(
                FirebaseAnalytics.Param.ITEMS,
                arrayOf(
                    Bundle().apply {
                        putString(FirebaseAnalytics.Param.ITEM_ID, "SKU_1")
                        putLong(FirebaseAnalytics.Param.QUANTITY, 2)
                    },
                ),
            )
        }
        analytics.logEvent("custom_event", null)
        analytics.setDefaultEventParameters(Bundle().apply { putString("source", "test") })
        analytics.setDefaultEventParameters(null)
        analytics.setUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD, "email")
        analytics.setUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD, null)
        analytics.setUserId("user-1")
        analytics.setUserId(null)
        analytics.setAnalyticsCollectionEnabled(true)
        analytics.setSessionTimeoutDuration(1_800_000L)
        analytics.setConsent(mapOf(FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.GRANTED))
        analytics.setConsent {
            adStorage = FirebaseAnalytics.ConsentStatus.GRANTED
            analyticsStorage = FirebaseAnalytics.ConsentStatus.GRANTED
            adPersonalization = FirebaseAnalytics.ConsentStatus.DENIED
        }
        analytics.getSessionId().await()
        analytics.resetAnalyticsData()
    }

    @Test
    fun testBundle() {
        val nested = Bundle().apply { putString(FirebaseAnalytics.Param.ITEM_NAME, "Widget") }
        val bundle = Bundle().apply {
            putString("string", "value")
            putInt("int", 3)
            putLong("long", 4L)
            putDouble("double", 1.5)
            putBoolean("boolean", true)
            putBundle("bundle", nested)
        }
        assertEquals("value", bundle.getString("string"))
        assertEquals("default", bundle.getString("missing", "default"))
        assertNull(bundle.getString("missing"))
        assertEquals(3, bundle.getInt("int"))
        assertEquals(7, bundle.getInt("missing", 7))
        assertEquals(4L, bundle.getLong("long"))
        assertEquals(1.5, bundle.getDouble("double"))
        assertTrue(bundle.getBoolean("boolean"))
        assertFalse(bundle.getBoolean("missing"))
        assertEquals("Widget", bundle.getBundle("bundle")?.getString(FirebaseAnalytics.Param.ITEM_NAME))
        assertTrue(bundle.containsKey("int"))
        assertEquals(setOf("string", "int", "long", "double", "boolean", "bundle"), bundle.keySet())
        assertEquals(6, bundle.size())

        val copy = Bundle(bundle)
        copy.remove("int")
        assertFalse(copy.containsKey("int"))
        assertTrue(bundle.containsKey("int"))
        copy.putAll(Bundle().apply { putString("extra", "x") })
        assertEquals("x", copy.getString("extra"))
        copy.clear()
        assertTrue(copy.isEmpty())
        assertFalse(bundle.isEmpty())
    }

    @Test
    fun testBuilders() {
        val bundle = ParametersBuilder().apply {
            param(FirebaseAnalytics.Param.ITEM_ID, "SKU_1")
            param(FirebaseAnalytics.Param.PRICE, 9.99)
            param(FirebaseAnalytics.Param.QUANTITY, 3L)
            param("nested", Bundle().apply { putBoolean("flag", true) })
        }.bundle
        assertEquals("SKU_1", bundle.getString(FirebaseAnalytics.Param.ITEM_ID))
        assertEquals(9.99, bundle.getDouble(FirebaseAnalytics.Param.PRICE))
        assertEquals(3L, bundle.getLong(FirebaseAnalytics.Param.QUANTITY))
        assertTrue(bundle.getBundle("nested")?.getBoolean("flag") == true)

        val consent = ConsentBuilder().apply {
            adStorage = FirebaseAnalytics.ConsentStatus.GRANTED
            adUserData = FirebaseAnalytics.ConsentStatus.DENIED
        }.asMap()
        assertEquals(
            mapOf(FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED, FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED),
            consent,
        )
        assertEquals("login", FirebaseAnalytics.Event.LOGIN)
        assertEquals("allow_personalized_ads", FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS)
    }
}
