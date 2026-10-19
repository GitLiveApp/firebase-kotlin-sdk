/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.TaskState
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import com.google.firebase.storage.component3
import com.google.firebase.storage.storage
import com.google.firebase.storage.storageMetadata
import com.google.firebase.storage.taskState
import com.google.firebase.storage.uploadSessionUri
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the `com.google.firebase.storage` layer exactly as Android app code would (the static accessors, the
 * Task-based reference API, upload tasks with their snapshots, listeners and the taskState flow, metadata built either
 * way, listing, the exception codes), on every platform, against the Storage emulator.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

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
    fun testInstances() {
        assertEquals(storage, Firebase.storage(app.compat))
        assertEquals(app.compat, storage.app)
        assertEquals(30_000, storage.maxUploadRetryTimeMillis)
        assertEquals(30_000, storage.maxOperationRetryTimeMillis)
        // The SDKs render a root path as "/" or "", depending on how the reference was reached, so compare the bucket and the trimmed path.
        assertEquals(storage.reference.bucket, storage.getReference("test").root.bucket)
        assertEquals(storage.reference.path.trim('/'), storage.getReference("test").root.path.trim('/'))
        assertEquals("test", storage.getReference("test/compat.txt").parent?.name)
        assertEquals(storage, dev.gitlive.firebase.Firebase.storage(app).compat)
    }

    @Test
    fun testUploadDownloadAndMetadata() = runTest {
        val ref = storage.getReference("test").child("compatBytes.txt")
        val metadata: StorageMetadata = storageMetadata {
            contentType = "text/plain"
            setCustomMetadata("key", "value")
        }
        val snapshot: UploadTask.TaskSnapshot = ref.putBytes("hello".encodeToByteArray(), metadata).await()
        val (bytesTransferred, totalByteCount, uploaded) = snapshot
        assertEquals(5, bytesTransferred)
        assertEquals(5, totalByteCount)
        assertEquals("text/plain", uploaded?.contentType)
        assertEquals(ref, snapshot.storage)
        assertNull(snapshot.error)
        // The Android SDK's member (a Uri) wins over the String extension there, so only its string form is used.
        val uploadSession: String? = snapshot.uploadSessionUri?.toString()
        assertTrue(uploadSession == null || uploadSession.isNotEmpty())

        assertEquals("hello", ref.getBytes(1024).await().decodeToString())
        assertTrue(ref.getDownloadUrl().await().toString().contains("compatBytes.txt"))

        val fetched = ref.getMetadata().await()
        assertEquals("text/plain", fetched.contentType)
        assertEquals("value", fetched.getCustomMetadata("key"))
        assertEquals(setOf("key"), fetched.customMetadataKeys)
        assertEquals("compatBytes.txt", fetched.name)
        assertEquals("test/compatBytes.txt", fetched.path)
        assertEquals(5, fetched.sizeBytes)
        assertTrue(fetched.creationTimeMillis > 0)
        assertNotNull(fetched.generation)

        val updated = ref.updateMetadata(StorageMetadata.Builder(fetched).setContentType("text/markdown").setCustomMetadata("key", null).build()).await()
        assertEquals("text/markdown", updated.contentType)
        assertNull(updated.getCustomMetadata("key"))

        ref.delete().await()
        val exception = assertFailsWith<StorageException> { ref.getBytes(1024).await() }
        assertEquals(StorageException.ERROR_OBJECT_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun testTaskStateAndListeners() = runTest {
        val ref = storage.getReference("test/compatTask.bin")
        val task: UploadTask = ref.putBytes(ByteArray(256 * 1024) { it.toByte() })
        val states = mutableListOf<TaskState<UploadTask.TaskSnapshot>>()
        task.taskState.collect { states += it }
        assertTrue(task.isComplete)
        assertTrue(task.isSuccessful)
        assertFalse(task.isInProgress)
        assertFalse(task.isPaused)
        assertFalse(task.isCanceled)
        assertNull(task.exception)
        assertTrue(states.all { it is TaskState.InProgress }, "$states")
        assertEquals(256 * 1024, task.snapshot.totalByteCount)
        assertEquals(256 * 1024, task.result.bytesTransferred)
        assertEquals(task, task.snapshot.task)

        val success = CompletableDeferred<Long>()
        task.addOnSuccessListener { success.complete(it.bytesTransferred) }
        assertEquals(256 * 1024, success.await())
        val complete = CompletableDeferred<Boolean>()
        task.addOnCompleteListener { complete.complete(it.isSuccessful) }
        assertTrue(complete.await())
        assertTrue(ref.activeUploadTasks.isEmpty())
    }

    @Test
    fun testList() = runTest {
        val folder = storage.getReference("test/compatList")
        folder.child("one.txt").putBytes("1".encodeToByteArray()).await()
        folder.child("two.txt").putBytes("2".encodeToByteArray()).await()
        val (items, prefixes, pageToken) = folder.listAll().await()
        assertEquals(setOf("one.txt", "two.txt"), items.map { it.name }.toSet())
        assertTrue(prefixes.isEmpty())
        assertNull(pageToken)
        val firstPage = folder.list(1).await()
        assertEquals(1, firstPage.items.size)
        val token = assertNotNull(firstPage.pageToken)
        val secondPage = folder.list(1, token).await()
        assertEquals(1, secondPage.items.size)
        assertTrue(firstPage.items.first() != secondPage.items.first())
        assertEquals("compatList", folder.name)
        assertEquals("/test/compatList", folder.path)
        assertEquals(storage.reference.bucket, folder.bucket)
    }

    @Test
    fun testExceptions() {
        assertEquals(-13010, StorageException.ERROR_OBJECT_NOT_FOUND)
        assertEquals(-13040, StorageException.ERROR_CANCELED)
        val unknown = StorageException.fromException(IllegalStateException("boom"))
        assertEquals(StorageException.ERROR_UNKNOWN, unknown.errorCode)
        assertFalse(unknown.isRecoverableException)
        assertNull(StorageException.fromExceptionAndHttpCode(null, 200))
        assertEquals(StorageException.ERROR_NOT_AUTHORIZED, StorageException.fromExceptionAndHttpCode(null, 403)?.errorCode)
        assertEquals(403, StorageException.fromExceptionAndHttpCode(null, 403)?.httpResultCode)
    }
}
