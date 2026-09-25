/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.activeDownloadTasks
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import com.google.firebase.storage.getFile
import com.google.firebase.storage.maxDownloadRetryTimeMillis
import com.google.firebase.storage.putFile
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A file the platform test can write and read: a java.io.File on Android and the JVM, an NSURL on Apple platforms. */
expect fun temporaryFile(name: String): Any

/** The file download part of the `com.google.firebase.storage` layer, which the JS SDK does not have. */
@IgnoreForAndroidUnitTest
class StorageNonJsTest {

    private lateinit var app: dev.gitlive.firebase.FirebaseApp
    private lateinit var storage: FirebaseStorage

    @BeforeTest
    fun initializeFirebase() {
        app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
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
        storage = FirebaseStorage.getInstance(app.compat).apply {
            useEmulator(emulatorHost, 9199)
            maxOperationRetryTimeMillis = 30_000
            maxUploadRetryTimeMillis = 30_000
        }
    }

    @Test
    fun testDownloadRetryTime() {
        storage.maxDownloadRetryTimeMillis = 20_000
        assertEquals(20_000, storage.maxDownloadRetryTimeMillis)
    }

    @Test
    fun testFileUploadAndDownload() = runTest {
        val ref = storage.getReference("test/compatFile.txt")
        ref.putBytes("file contents".encodeToByteArray()).await()
        val destination = temporaryFile("compatDownload.txt")
        val task: FileDownloadTask = ref.getFile(destination)
        val (bytesTransferred, totalByteCount) = task.await()
        assertEquals(13, totalByteCount)
        assertEquals(13, bytesTransferred)
        assertTrue(ref.activeDownloadTasks.isEmpty())
        val reUploaded = ref.child("../compatReupload.txt").putFile(destination).await()
        assertEquals(13, reUploaded.totalByteCount)
    }
}
