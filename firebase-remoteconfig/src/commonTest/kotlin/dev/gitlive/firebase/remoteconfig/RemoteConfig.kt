/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseRemoteConfigTest {

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

    @Test
    fun testGettingValues() = runTest {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaults(
            mapOf(
                "test_default_boolean" to true,
                "test_default_double" to 42.0,
                "test_default_long" to 42L,
                "test_default_string" to "Hello World",
            )
        )

        assertEquals(true, remoteConfig.getBoolean("test_default_boolean"))
        assertEquals(42.0, remoteConfig.getDouble("test_default_double"))
        assertEquals(42L, remoteConfig.getLong("test_default_long"))
        assertEquals("Hello World", remoteConfig.getString("test_default_string"))

        val value = remoteConfig.getValue("test_default_string")
        assertEquals("Hello World", value.asString())
        assertEquals(ValueSource.Default, value.getSource())
    }

    // Unfortunately Firebase Remote Config is not implemented by Firebase emulator so it may be
    // tested against a real project only. Add "test_remote_string": "Hello from remote!" remote
    // config value in Firebase console for enabling this test case.
    @Test
    @Ignore
    fun testFetchingValue() = runTest {
        val remoteConfig = Firebase.remoteConfig
        val settings = RemoteConfigSettings(minimumFetchIntervalInSeconds = 60)
        remoteConfig.setConfigSettings(settings)

        remoteConfig.fetch()
        remoteConfig.activate()

        val value = remoteConfig.getValue("test_remote_string")
        assertEquals("Hello from remote!", value.asString())
        assertEquals(ValueSource.Remote, value.getSource())
    }
}
