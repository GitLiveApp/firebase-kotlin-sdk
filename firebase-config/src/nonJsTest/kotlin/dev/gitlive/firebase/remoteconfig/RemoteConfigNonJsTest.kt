/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.addOnConfigUpdateListener
import com.google.firebase.remoteconfig.remoteConfig
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The real-time update part of the `com.google.firebase.remoteconfig` layer, which the JS SDK does not have; custom signals are in CustomSignalsTest. */
@IgnoreForAndroidUnitTest
@IgnoreForJvm
class RemoteConfigNonJsTest {

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
    fun testConfigUpdateListener() {
        val registration = Firebase.remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) = Unit

                override fun onError(error: FirebaseRemoteConfigException) = Unit
            },
        )
        registration.remove()
        assertEquals(setOf("a", "b"), ConfigUpdate.create(setOf("a", "b")).updatedKeys)
    }
}
