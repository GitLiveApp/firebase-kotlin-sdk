/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseRemoteConfigTest {
    private val defaults = arrayOf(
        "test_default_boolean" to true,
        "test_default_double" to 42.0,
        "test_default_long" to 42L,
        "test_default_string" to "Hello World",
    )

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(context).isEmpty() }
            ?.apply {
                initialize(
                    context,
                    FirebaseOptions(
                        applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                        apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                        databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
            }
    }

    @AfterTest
    fun tearDown() = runTest {
        Firebase.remoteConfig.reset()
    }

    @Test
    fun testGettingValues() = runTest {
        val remoteConfig = Firebase.remoteConfig
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
        Firebase.remoteConfig.setDefaults(*defaults)
        val all = Firebase.remoteConfig.all
        assertEquals(true, all["test_default_boolean"]?.asBoolean())
        assertEquals(42.0, all["test_default_double"]?.asDouble())
        assertEquals(42L, all["test_default_long"]?.asLong())
        assertEquals("Hello World", all["test_default_string"]?.asString())
        assertEquals("Hello World", all["test_default_string"]?.asByteArray()?.decodeToString())
    }

    @Test
    fun testGetKeysByPrefix() = runTest {
        Firebase.remoteConfig.setDefaults(*defaults)
        val keys = Firebase.remoteConfig.getKeysByPrefix("test_default")
        assertEquals(
            setOf(
                "test_default_boolean",
                "test_default_double",
                "test_default_long",
                "test_default_string"
            ),
            keys
        )
    }

    @Test
    fun testGetInfo() = runTest {
        assertEquals(
            FirebaseRemoteConfigInfo(
                configSettings = FirebaseRemoteConfigSettings(),
                fetchTimeMillis = -1,
                lastFetchStatus = FetchStatus.NoFetchYet
            ).toString(),
            Firebase.remoteConfig.info.toString()
        )
    }

    @Test
    fun testSetConfigSettings() = runTest {
        Firebase.remoteConfig.settings {
            fetchTimeoutInSeconds = 42
            minimumFetchIntervalInSeconds = 42
        }
        val info = Firebase.remoteConfig.info
        assertEquals(42, info.configSettings.fetchTimeoutInSeconds)
        assertEquals(42, info.configSettings.minimumFetchIntervalInSeconds)
    }

    // Unfortunately Firebase Remote Config is not implemented by Firebase emulator so it may be
    // tested against a real project only. Add "test_remote_string": "Hello from remote!" config
    // value in Firebase console for enabling this test case.
    @Test
    @Ignore
    fun testFetch() = runTest {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.settings {
            minimumFetchIntervalInSeconds = 60
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
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.settings {
            minimumFetchIntervalInSeconds = 60
        }

        remoteConfig.fetchAndActivate()

        val value: FirebaseRemoteConfigValue = remoteConfig["test_remote_string"]
        assertEquals("Hello from remote!", value.asString())
        assertEquals(ValueSource.Remote, value.getSource())
    }
}
