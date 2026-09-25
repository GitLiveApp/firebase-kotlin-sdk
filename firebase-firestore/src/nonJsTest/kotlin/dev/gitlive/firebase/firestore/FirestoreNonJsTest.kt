/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.get
import com.google.firebase.firestore.memoryCacheSettings
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The `com.google.firebase.firestore` members that the JS SDK cannot provide: the synchronous `Transaction.get`. */
@IgnoreForAndroidUnitTest
class FirestoreNonJsTest {

    private lateinit var app: dev.gitlive.firebase.FirebaseApp
    private lateinit var firestore: FirebaseFirestore

    @BeforeTest
    fun initializeFirebase() {
        app = dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
            "firestoreNonJsTest",
        )
        firestore = Firebase.firestore(app.compat).apply {
            firestoreSettings = firestoreSettings { setLocalCacheSettings(memoryCacheSettings { }) }
            useEmulator(emulatorHost, 8080)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
    }

    @Test
    fun testTransactionGet() = runTest {
        val document = firestore.collection("compat").document("test").collection("nonJs").document("counter")
        document.set(mapOf("count" to 1L)).await()
        val previous = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            val count = snapshot.getLong("count") ?: 0L
            transaction.update(document, "count", count + 1)
            count
        }.await()
        assertEquals(1L, previous)
        assertEquals(2L, document.get().await().getLong("count"))
        document.delete().await()
    }
}
