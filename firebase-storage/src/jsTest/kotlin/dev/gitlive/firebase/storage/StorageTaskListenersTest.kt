/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The Kotlin [com.google.firebase.storage.StorageTask] shared by the Apple and JS SDKs, which dispatches listeners itself. */
class StorageTaskListenersTest {

    private lateinit var storage: FirebaseStorage

    @BeforeTest
    fun initializeFirebase() {
        val app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
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
            // As in AndroidSdkSourceCompatTest: useEmulator is applied once, when the app is created.
            FirebaseStorage.getInstance(it.compat).apply {
                useEmulator(emulatorHost, 9199)
                maxOperationRetryTimeMillis = 30_000
                maxUploadRetryTimeMillis = 30_000
            }
        }
        storage = FirebaseStorage.getInstance(app.compat)
    }

    /** A listener that throws does not stop the other listeners, so `await()` still resumes. */
    @Test
    fun testThrowingListenerDoesNotStarveTheOthers() = runTest {
        val ref = storage.getReference("test/compatTaskThrowing.bin")
        val task: UploadTask = ref.putBytes(ByteArray(512))
        val secondSuccess = CompletableDeferred<Boolean>()
        task.addOnSuccessListener { throw IllegalStateException("listener failure") }
        task.addOnSuccessListener { secondSuccess.complete(true) }
        task.await()
        assertTrue(secondSuccess.await())
        assertTrue(task.isSuccessful)
        assertEquals(512, task.result.totalByteCount)
    }
}
