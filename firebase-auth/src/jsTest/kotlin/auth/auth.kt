/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

//expect val context: Any

class FirebaseAuthTest {

    @BeforeTest
    fun initializeFirebase() {
        Firebase.initialize(null, FirebaseOptions(
            applicationId ="fir-kotlin-sdk",
            apiKey = "AIzaSyDft_DSsVA7KPJj_GItUvMLjk6wbOdGBOs",
            databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId ="fir-kotlin-sdk"
        ))

    }

    @Test
    fun testSignInWithUsernameAndPassword() = GlobalScope.promise {
        val result = Firebase.auth.signInWithEmailAndPassword("test@test.com", "test123")
        assertEquals("mn8kgIFnxLO7il8GpTa5g0ObP6I2", result.user!!.uid)
    }
}