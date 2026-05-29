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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

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
            setMaxOperationRetryTime(30.seconds)
            setMaxUploadRetryTime(30.seconds)
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

    @Test
    fun testUpdateMetadata() = runTest {
        val data = createTestData()
        val ref = storage.reference("test").child("testUpdateMetadata.txt")
        ref.putData(data, storageMetadata { contentType = "text/plain" })

        val metadata = storageMetadata {
            cacheControl = "public,max-age=300"
            contentDisposition = "attachment; filename=testUpdateMetadata.txt"
            contentEncoding = "identity"
            contentLanguage = "en"
            contentType = "text/markdown"
            setCustomMetadata("updated", "true")
        }

        val updatedMetadata = ref.updateMetadata(metadata)
        val fetchedMetadata = ref.getMetadata()

        assertNotNull(updatedMetadata)
        assertEquals(metadata.cacheControl, updatedMetadata.cacheControl)
        assertEquals(metadata.contentDisposition, updatedMetadata.contentDisposition)
        assertEquals(metadata.contentEncoding, updatedMetadata.contentEncoding)
        assertEquals(metadata.contentLanguage, updatedMetadata.contentLanguage)
        assertEquals(metadata.contentType, updatedMetadata.contentType)
        assertEquals(metadata.customMetadata["updated"], updatedMetadata.customMetadata["updated"])

        assertNotNull(fetchedMetadata)
        assertEquals(metadata.cacheControl, fetchedMetadata.cacheControl)
        assertEquals(metadata.contentDisposition, fetchedMetadata.contentDisposition)
        assertEquals(metadata.contentEncoding, fetchedMetadata.contentEncoding)
        assertEquals(metadata.contentLanguage, fetchedMetadata.contentLanguage)
        assertEquals(metadata.contentType, fetchedMetadata.contentType)
        assertEquals(metadata.customMetadata["updated"], fetchedMetadata.customMetadata["updated"])
    }

    @Test
    fun testPaginatedList() = runTest {
        val data = createTestData()
        val ref = storage.reference("test/testPaginatedList")
        ref.child("one.txt").putData(data)
        ref.child("two.txt").putData(data)
        ref.child("three.txt").putData(data)

        val firstPage = ref.list(maxResults = 2)
        val pageToken = assertNotNull(firstPage.pageToken)
        val secondPage = ref.list(maxResults = 2, pageToken = pageToken)
        val itemNames = (firstPage.items + secondPage.items).map { it.name }

        assertEquals(2, firstPage.items.size)
        assertEquals(1, secondPage.items.size)
        assertTrue("one.txt" in itemNames)
        assertTrue("two.txt" in itemNames)
        assertTrue("three.txt" in itemNames)
    }
}

expect fun createTestData(): Data
