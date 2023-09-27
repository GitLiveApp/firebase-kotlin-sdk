/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.firebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlin.test.BeforeTest

expect val emulatorHost: String
expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseStorageTest {

    lateinit var storage: FirebaseStorage

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            firebaseOptions
        )

        storage = Firebase.storage(app).apply {
            useEmulator(emulatorHost, 9199)
        }
    }

}