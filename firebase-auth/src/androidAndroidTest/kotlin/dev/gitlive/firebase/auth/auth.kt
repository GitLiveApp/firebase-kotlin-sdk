/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.assertEquals

val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

@RunWith(AndroidJUnit4::class)
@SmallTest
class FirebaseAuthTest {

    @Before
    fun initializeFirebase() {
        Firebase.initialize(context, FirebaseOptions(
            applicationId ="fir-kotlin-sdk",
            apiKey = "AIzaSyDft_DSsVA7KPJj_GItUvMLjk6wbOdGBOs",
            databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId ="fir-kotlin-sdk"
        ))

    }

    @org.junit.Test
    fun testSignInWithUsernameAndPassword() = runBlocking {
        val result = Firebase.auth.signInWithEmailAndPassword("test@test.com", "test123")
        assertEquals("mn8kgIFnxLO7il8GpTa5g0ObP6I2", result.user!!.uid)
    }
}