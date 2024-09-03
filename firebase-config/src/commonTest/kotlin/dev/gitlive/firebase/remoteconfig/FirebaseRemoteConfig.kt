/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseRemoteConfigTest {
    private val defaults = arrayOf(
        "test_default_boolean" to true,
        "test_default_double" to 42.0,
        "test_default_long" to 42L,
        "test_default_string" to "Hello World",
    )

    lateinit var remoteConfig: FirebaseRemoteConfig

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

        remoteConfig = Firebase.remoteConfig(app)
    }

    @AfterTest
    fun tearDown() = runTest {
        remoteConfig.reset()
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testGettingValues() = runTest {
        remoteConfig.setDefaults(*defaults)

        assertEquals(true, remoteConfig["test_default_boolean"])
        assertEquals(42.0, remoteConfig["test_default_double"])
        assertEquals(42L, remoteConfig["test_default_long"])
        assertEquals("Hello World", remoteConfig["test_default_string"])
        assertEquals("Hello World", remoteConfig["test_default_string"])

        val value: FirebaseRemoteConfigValue = remoteConfig["test_default_string"]
        assertEquals("Hello World", value.asString())
        assertEquals(ValueSource.Default, value.getSource())
        assertEquals("Hello World", value.asByteArray().decodeToString())
    }

    @Test
    fun testGetAll() = runTest {
        remoteConfig.setDefaults(*defaults)
        val all = remoteConfig.all
        assertEquals(true, all["test_default_boolean"]?.asBoolean())
        assertEquals(42.0, all["test_default_double"]?.asDouble())
        assertEquals(42L, all["test_default_long"]?.asLong())
        assertEquals("Hello World", all["test_default_string"]?.asString())
        assertEquals("Hello World", all["test_default_string"]?.asByteArray()?.decodeToString())
    }

    @Test
    fun testGetKeysByPrefix() = runTest {
        remoteConfig.setDefaults(*defaults)
        val keys = remoteConfig.getKeysByPrefix("test_default")
        assertEquals(
            setOf(
                "test_default_boolean",
                "test_default_double",
                "test_default_long",
                "test_default_string",
            ),
            keys,
        )
    }

    @Test
    fun testGetInfo() = runTest {
        assertEquals(
            FirebaseRemoteConfigInfo(
                configSettings = FirebaseRemoteConfigSettings(),
                fetchTime = Instant.fromEpochMilliseconds(-1),
                lastFetchStatus = FetchStatus.NoFetchYet,
            ).toString(),
            remoteConfig.info.toString(),
        )
    }

    @Test
    fun testSetConfigSettings() = runTest {
        remoteConfig.settings {
            fetchTimeout = 42.seconds
            minimumFetchInterval = 42.seconds
        }
        val info = remoteConfig.info
        assertEquals(42.seconds, info.configSettings.fetchTimeout)
        assertEquals(42.seconds, info.configSettings.minimumFetchInterval)
    }

    // Unfortunately Firebase Remote Config is not implemented by Firebase emulator so it may be
    // tested against a real project only. Add "test_remote_string": "Hello from remote!" config
    // value in Firebase console for enabling this test case.
    @Test
    @Ignore
    fun testFetch() = runTest {
        remoteConfig.settings {
            minimumFetchInterval = 1.minutes
        }

        remoteConfig.fetch()
        remoteConfig.activate()

        val value: FirebaseRemoteConfigValue = remoteConfig["test_remote_string"]
        assertEquals("Hello from remote!", value.asString())
        assertEquals(ValueSource.Remote, value.getSource())
    }

    @Test
    @Ignore
    fun testFetchAndActivate() = runTest {
        remoteConfig.settings {
            minimumFetchInterval = 1.minutes
        }

        remoteConfig.fetchAndActivate()

        val value: FirebaseRemoteConfigValue = remoteConfig["test_remote_string"]
        assertEquals("Hello from remote!", value.asString())
        assertEquals(ValueSource.Remote, value.getSource())
    }
}
