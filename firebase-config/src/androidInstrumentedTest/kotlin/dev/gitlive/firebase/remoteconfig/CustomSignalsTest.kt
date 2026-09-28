/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.CustomSignals
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.customSignals
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.setCustomSignals
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * The custom signal part of the `com.google.firebase.remoteconfig` layer: the JS SDK does not have it, and
 * firebase-java-sdk lacks it too, so this test exists for Android and Apple only.
 */
class CustomSignalsTest {

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
    fun testCustomSignals() = runTest {
        val signals: CustomSignals = customSignals {
            put("compat_string", "value")
            put("compat_long", 42L)
            put("compat_double", 42.5)
            put("compat_removed", null)
        }
        Firebase.remoteConfig.setCustomSignals(signals).await()
        Firebase.remoteConfig.setCustomSignals(CustomSignals.Builder().put("compat_string", "other").build()).await()
    }
}
