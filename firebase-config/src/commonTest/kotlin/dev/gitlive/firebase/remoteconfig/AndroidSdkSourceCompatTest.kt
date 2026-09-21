/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the `com.google.firebase.remoteconfig` layer exactly as Android app code would (the static accessors,
 * the Task API, the settings builder in both its chained and property forms, the getters, the constants and the
 * exception classes), on every platform.
 */
@IgnoreForAndroidUnitTest
@IgnoreForJvm
class AndroidSdkSourceCompatTest {

    private val defaults = mapOf(
        "compat_boolean" to true,
        "compat_double" to 42.5,
        "compat_long" to 42L,
        "compat_string" to "Hello World",
    )

    @BeforeTest
    fun initializeFirebase() {
        dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
    }

    // The app is left initialised: deleting it asynchronously (as the JS SDK does) would race the next test's setup.
    @AfterTest
    fun tearDown() = runTest {
        FirebaseRemoteConfig.getInstance().reset().await()
    }

    @Test
    fun testInstances() {
        val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        assertEquals(remoteConfig, Firebase.remoteConfig)
        assertEquals(remoteConfig, dev.gitlive.firebase.Firebase.remoteConfig.compat)
        assertEquals(FirebaseRemoteConfig.getInstance(com.google.firebase.FirebaseApp.getInstance()), Firebase.remoteConfig(com.google.firebase.FirebaseApp.getInstance()))
    }

    @Test
    fun testDefaultsAndGetters() = runTest {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(defaults).await()
        assertEquals(true, remoteConfig.getBoolean("compat_boolean"))
        assertEquals(42.5, remoteConfig.getDouble("compat_double"))
        assertEquals(42L, remoteConfig.getLong("compat_long"))
        assertEquals("Hello World", remoteConfig.getString("compat_string"))
        assertEquals(FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING, remoteConfig.getString("compat_missing"))
        assertEquals(FirebaseRemoteConfig.DEFAULT_VALUE_FOR_LONG, remoteConfig.getLong("compat_missing"))

        val value: FirebaseRemoteConfigValue = remoteConfig["compat_string"]
        assertEquals("Hello World", value.asString())
        assertEquals("Hello World", value.asByteArray().decodeToString())
        assertEquals(FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT, value.source)
        assertEquals(FirebaseRemoteConfig.VALUE_SOURCE_STATIC, remoteConfig.getValue("compat_missing").source)
        assertEquals(42L, remoteConfig.all.getValue("compat_long").asLong())
        assertEquals(defaults.keys, remoteConfig.getKeysByPrefix("compat_"))
    }

    @Test
    fun testSettingsAndInfo() = runTest {
        val remoteConfig = Firebase.remoteConfig
        val settings = remoteConfigSettings {
            fetchTimeoutInSeconds = 42
            minimumFetchIntervalInSeconds = 43
        }
        assertEquals(42, settings.fetchTimeoutInSeconds)
        assertEquals(43, settings.minimumFetchIntervalInSeconds)
        remoteConfig.setConfigSettingsAsync(settings).await()
        val info = remoteConfig.ensureInitialized().await()
        assertEquals(42, info.configSettings.fetchTimeoutInSeconds)
        assertEquals(43, remoteConfig.info.configSettings.minimumFetchIntervalInSeconds)
        assertEquals(FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET, info.lastFetchStatus)
        assertEquals(-1, info.fetchTimeMillis)

        val chained: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder().setFetchTimeoutInSeconds(10).setMinimumFetchIntervalInSeconds(20).build()
        assertEquals(10, chained.fetchTimeoutInSeconds)
        assertEquals(20, chained.toBuilder().minimumFetchIntervalInSeconds)
        assertEquals(60, FirebaseRemoteConfigSettings.Builder().fetchTimeoutInSeconds)
    }

    @Test
    fun testExceptions() {
        val throttled = FirebaseRemoteConfigFetchThrottledException(1234)
        assertEquals(1234, throttled.throttleEndTimeMillis)
        assertEquals(FirebaseRemoteConfigException.Code.UNKNOWN, throttled.code)
        val server = FirebaseRemoteConfigServerException(503, "unavailable")
        assertEquals(503, server.httpStatusCode)
        assertEquals("unavailable", server.message)
        val client: FirebaseRemoteConfigException = FirebaseRemoteConfigClientException("offline", IllegalStateException("cause"))
        assertTrue(client.cause is IllegalStateException)
        assertNull(FirebaseRemoteConfigException("x").cause)
        assertEquals(FirebaseRemoteConfigException.Code.CONFIG_UPDATE_UNAVAILABLE, FirebaseRemoteConfigException("x", FirebaseRemoteConfigException.Code.CONFIG_UPDATE_UNAVAILABLE).code)
        assertEquals(4, FirebaseRemoteConfigException.Code.CONFIG_UPDATE_UNAVAILABLE.value())
    }
}
