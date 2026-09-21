/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.keepSynced
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The offline synchronization part of the `com.google.firebase.database` layer, which the JS SDK does not have. */
@IgnoreForAndroidUnitTest
class DatabaseNonJsTest {

    private lateinit var database: FirebaseDatabase

    @BeforeTest
    fun initializeFirebase() {
        val app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk-default-rtdb.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk-default-rtdb",
                gcmSenderId = "846484016111",
            ),
        )
        database = FirebaseDatabase.getInstance(app.compat).apply { useEmulator(emulatorHost, 9000) }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        dev.gitlive.firebase.Firebase.apps(context).forEach { it.delete() }
    }

    @Test
    fun testKeepSynced() = runTest {
        val reference = database.getReference("test/compatKeepSynced")
        reference.setValue("synced").await()
        reference.keepSynced(true)
        assertEquals("synced", reference.get().await().value)
        reference.keepSynced(false)
        reference.orderByKey().limitToFirst(1).keepSynced(false)
    }
}
