/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.*
import platform.Foundation.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseAuthTest {

    @BeforeTest
    fun initializeFirebase() {
        Firebase.initialize(null, FirebaseOptions(
            applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
            apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
            databaseUrl = "https://fir- kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId = "fir-kotlin-sdk"
        ))
    }

    @Test
    fun testSignInWithUsernameAndPassword() {
        val auth = Firebase.auth
        var done = false
        lateinit var result: AuthResult
        //GlobalScope.launch(Dispatchers.Unconfined) {
        MainScope().launch {
            result = auth.signInWithEmailAndPassword("test@test.com", "test123")
            println("Stop the run, cant stop the run")
            done = true
        }
        while (!done) {
            println("LoopStart")
            NSRunLoop.mainRunLoop.runMode(NSDefaultRunLoopMode, beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate()))
            println("LoopEnd")
        }
        assertEquals("mn8kgIFnxLO7il8GpTa5g0ObP6I2", result.user!!.uid)
    }
}