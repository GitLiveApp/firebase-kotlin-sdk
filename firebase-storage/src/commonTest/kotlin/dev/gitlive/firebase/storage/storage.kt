/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )

        storage = Firebase.storage(app).apply {
            useEmulator(emulatorHost, 9199)
            setMaxOperationRetryTimeMillis(10000)
            setMaxUploadRetryTimeMillis(10000)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testStorageNotNull() = runTest {
        assertNotNull(storage)
    }

    @Test
    fun testUploadShouldNotCrash() = runTest {
        val data = createTestData()
        val ref = storage.reference("test").child("testUploadShouldNotCrash.txt")

        ref.putData(data)
    }

    @Test
    fun testUploadMetadata() = runTest {
        val data = createTestData()
        val ref = storage.reference("test").child("testUploadMetadata.txt")
        val metadata = storageMetadata {
            contentType = "text/plain"
        }
        ref.putData(data, metadata)

        val metadataResult = ref.getMetadata()

        assertNotNull(metadataResult)
        assertNotNull(metadataResult.contentType)
        assertEquals(metadata.contentType, metadataResult.contentType)
    }

    @Test
    fun testUploadCustomMetadata() = runTest {
        val data = createTestData()
        val ref = storage.reference("test").child("testUploadCustomMetadata.txt")
        val metadata = storageMetadata {
            contentType = "text/plain"
            setCustomMetadata("key", "value")
        }
        ref.putData(data, metadata)

        val metadataResult = ref.getMetadata()

        assertNotNull(metadataResult)
        assertEquals(metadata.customMetadata["key"], metadataResult.customMetadata["key"])
    }
}

expect fun createTestData(): Data
