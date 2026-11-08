/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import android.net.Uri
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.UnsupportedOnJs
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.buffered
import kotlinx.io.readString
import com.google.firebase.storage.getFile

/** The URL of a file the platform test can write and read; on JS a bare name, as its SDK cannot use files. */
expect fun temporaryFile(name: String): Uri

/** Whether the platform SDK downloads to files; the JS SDK does not. */
expect val supportsFileDownloads: Boolean

/** Whether the platform SDK uploads from file URLs; the JS SDK does not, and on the JVM firebase-java-sdk's `ContentResolver` cannot open them yet. */
expect val supportsFileUploads: Boolean

/** The file download part of the `com.google.firebase.storage` layer; on JS, whose SDK has no file downloads, the download fails. */
@OptIn(UnsupportedOnJs::class)
@IgnoreForAndroidUnitTest
class FileDownloadTest {

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
        ).also {
            // useEmulator is only valid before the storage instance is used and the iOS SDK caches the instance per app and
            // bucket, so it is applied once, when the app is created (as the FirebaseStorageTest fixture does), not per test.
            FirebaseStorage.getInstance(it.compat).apply {
                useEmulator(emulatorHost, 9199)
                maxOperationRetryTimeMillis = 30_000
                maxUploadRetryTimeMillis = 30_000
            }
        }
        storage = FirebaseStorage.getInstance(app.compat)
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
        if (!supportsFileDownloads) {
            assertIs<UnsupportedOperationException>(assertFails { task.await() })
            assertTrue(ref.activeDownloadTasks.isEmpty())
            return@runTest
        }
        val (bytesTransferred, totalByteCount) = task.await()
        assertEquals(13, totalByteCount)
        assertEquals(13, bytesTransferred)
        assertTrue(ref.activeDownloadTasks.isEmpty())
        if (!supportsFileUploads) return@runTest
        val reUploaded = ref.child("../compatReupload.txt").putFile(destination).await()
        assertEquals(13, reUploaded.totalByteCount)
    }

    @Test
    fun testDownloadToPath() = runTest {
        val ref = storage.getReference("test/compatPath.txt")
        ref.putBytes("path contents".encodeToByteArray()).await()
        // The temporary directory needs the platform file system, which the JS test (whose SDK has no file downloads) lacks.
        val destination = if (supportsFileDownloads) Path(SystemTemporaryDirectory, "compatDownloadPath.txt") else Path("compatDownloadPath.txt")
        val task: FileDownloadTask = ref.getFile(destination)
        if (!supportsFileDownloads) {
            assertIs<UnsupportedOperationException>(assertFails { task.await() })
            return@runTest
        }
        val (bytesTransferred, totalByteCount) = task.await()
        assertEquals(13, totalByteCount)
        assertEquals(13, bytesTransferred)
        assertEquals("path contents", SystemFileSystem.source(destination).buffered().use { it.readString() })
    }
}
