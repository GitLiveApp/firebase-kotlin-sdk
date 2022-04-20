/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend CoroutineScope.() -> Unit)

class FirebaseFirestoreTest {

    @Serializable
    data class FirestoreTest(
        val prop1: String, 
        val time: Double = 0.0,
        val count: Int = 0, 
        val list: List<String> = emptyList(),
    )

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(context).isEmpty() }
            ?.apply {
                initialize(
                    context,
                    FirebaseOptions(
                        applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                        apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                        databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
                Firebase.firestore.useEmulator(emulatorHost, 8080)
            }
    }

    @Test
    fun testStringOrderBy() = runTest {
        setupFirestoreData()
        val resultDocs = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1")
            .get()
            .documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testFieldOrderBy() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1")).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testStringOrderByAscending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testFieldOrderByAscending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1"), Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testStringOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("aaa", resultDocs[2].get("prop1"))
    }

    @Test
    fun testFieldOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1"), Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("aaa", resultDocs[2].get("prop1"))
    }

    @Test
    fun testServerTimestampFieldValue() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTimestampFieldValue")
            .document("test")

        doc.set(FirestoreTest.serializer(), FirestoreTest("ServerTimestamp", FieldValue.serverTimestamp))

        assertNotEquals(FieldValue.serverTimestamp, doc.get().get("time"))
        assertNotEquals(FieldValue.serverTimestamp, doc.get().data(FirestoreTest.serializer()).time)
    }

    @Test
    fun testServerTimestampBehaviorNone() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTimestampBehaviorNone")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            withTimeout(5000) {
                doc.snapshots.filter { it.exists }.first()
            }
        }
        delay(100) // makes possible to catch pending writes snapshot

        doc.set(
            FirestoreTest.serializer(),
            FirestoreTest("ServerTimestampBehavior", FieldValue.serverTimestamp)
        )

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get<Double?>("time", ServerTimestampBehavior.NONE))
    }

    @Test
    fun testServerTimestampBehaviorEstimate() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTimestampBehaviorEstimate")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            withTimeout(5000) {
                doc.snapshots.filter { it.exists }.first()
            }
        }
        delay(100) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTest.serializer(), FirestoreTest("ServerTimestampBehavior", FieldValue.serverTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get<Double?>("time", ServerTimestampBehavior.ESTIMATE))
        assertNotEquals(0.0, pendingWritesSnapshot.data(FirestoreTest.serializer(), ServerTimestampBehavior.ESTIMATE).time)
    }

    @Test
    fun testServerTimestampBehaviorPrevious() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTimestampBehaviorPrevious")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            withTimeout(5000) {
                doc.snapshots.filter { it.exists }.first()
            }
        }
        delay(100) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTest.serializer(), FirestoreTest("ServerTimestampBehavior", FieldValue.serverTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get<Double?>("time", ServerTimestampBehavior.PREVIOUS))
    }

    @Test
    fun testDocumentAutoId() = runTest {
        val doc = Firebase.firestore
            .collection("testDocumentAutoId")
            .document

        doc.set(FirestoreTest.serializer(), FirestoreTest("AutoId"))

        val resultDoc = Firebase.firestore
            .collection("testDocumentAutoId")
            .document(doc.id)
            .get()

        assertEquals(true, resultDoc.exists)
        assertEquals("AutoId", resultDoc.get("prop1"))
    }

    @Test
    fun testStartAfterDocumentSnapshot() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.limit(2).get().documents // First 2 results
        assertEquals(2, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))

        val lastDocumentSnapshot = firstPage.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.startAfter(lastDocumentSnapshot).get().documents
        assertEquals(1, secondPage.size)
        assertEquals("ccc", secondPage[0].get("prop1"))
    }

    @Test
    fun testStartAfterFieldValues() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.get().documents
        assertEquals(3, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))
        assertEquals("ccc", firstPage[2].get("prop1"))

        val secondPage = query.startAfter("bbb").get().documents
        assertEquals(1, secondPage.size)
        assertEquals("ccc", secondPage[0].get("prop1"))
    }

    @Test
    fun testStartAtDocumentSnapshot() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.limit(2).get().documents // First 2 results
        assertEquals(2, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))

        val lastDocumentSnapshot = firstPage.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.startAt(lastDocumentSnapshot).get().documents
        assertEquals(2, secondPage.size)
        assertEquals("bbb", secondPage[0].get("prop1"))
        assertEquals("ccc", secondPage[1].get("prop1"))
    }

    @Test
    fun testStartAtFieldValues() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.get().documents // First 2 results
        assertEquals(3, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))
        assertEquals("ccc", firstPage[2].get("prop1"))

        val secondPage = query.startAt("bbb").get().documents
        assertEquals(2, secondPage.size)
        assertEquals("bbb", secondPage[0].get("prop1"))
        assertEquals("ccc", secondPage[1].get("prop1"))
    }

    @Test
    fun testEndBeforeDocumentSnapshot() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.limit(2).get().documents // First 2 results
        assertEquals(2, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))

        val lastDocumentSnapshot = firstPage.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.endBefore(lastDocumentSnapshot).get().documents
        assertEquals(1, secondPage.size)
        assertEquals("aaa", secondPage[0].get("prop1"))
    }

    @Test
    fun testEndBeforeFieldValues() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.get().documents
        assertEquals(3, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))
        assertEquals("ccc", firstPage[2].get("prop1"))

        val secondPage = query.endBefore("bbb").get().documents
        assertEquals(1, secondPage.size)
        assertEquals("aaa", secondPage[0].get("prop1"))
    }

    @Test
    fun testEndAtDocumentSnapshot() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.limit(2).get().documents // First 2 results
        assertEquals(2, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))

        val lastDocumentSnapshot = firstPage.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.endAt(lastDocumentSnapshot).get().documents
        assertEquals(2, secondPage.size)
        assertEquals("aaa", secondPage[0].get("prop1"))
        assertEquals("bbb", secondPage[1].get("prop1"))
    }

    @Test
    fun testEndAtFieldValues() = runTest {
        setupFirestoreData()
        val query = Firebase.firestore
            .collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING)

        val firstPage = query.get().documents // First 2 results
        assertEquals(3, firstPage.size)
        assertEquals("aaa", firstPage[0].get("prop1"))
        assertEquals("bbb", firstPage[1].get("prop1"))
        assertEquals("ccc", firstPage[2].get("prop1"))

        val secondPage = query.endAt("bbb").get().documents
        assertEquals(2, secondPage.size)
        assertEquals("aaa", secondPage[0].get("prop1"))
        assertEquals("bbb", secondPage[1].get("prop1"))
    }

    @Test
    fun testIncrementFieldValue() = runTest {
        val doc = Firebase.firestore
            .collection("testFirestoreIncrementFieldValue")
            .document("test1")

        doc.set(FirestoreTest.serializer(), FirestoreTest("increment1", count = 0))
        val dataBefore = doc.get().data(FirestoreTest.serializer())
        assertEquals(0, dataBefore.count)

        doc.update("count" to FieldValue.increment(5))
        val dataAfter = doc.get().data(FirestoreTest.serializer())
        assertEquals(5, dataAfter.count)
    }

    @Test
    fun testArrayUnion() = runTest {
        val doc = Firebase.firestore
            .collection("testFirestoreArrayUnion")
            .document("test1")

        doc.set(FirestoreTest.serializer(), FirestoreTest("increment1", list = listOf("first")))
        val dataBefore = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first"), dataBefore.list)

        doc.update("list" to FieldValue.arrayUnion("second"))
        val dataAfter = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first", "second"), dataAfter.list)
    }

    @Test
    fun testArrayRemove() = runTest {
        val doc = Firebase.firestore
            .collection("testFirestoreArrayRemove")
            .document("test1")

        doc.set(FirestoreTest.serializer(), FirestoreTest("increment1", list = listOf("first", "second")))
        val dataBefore = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first", "second"), dataBefore.list)

        doc.update("list" to FieldValue.arrayRemove("second"))
        val dataAfter = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first"), dataAfter.list)
    }

    private suspend fun setupFirestoreData() {
        Firebase.firestore.collection("testFirestoreQuerying")
            .document("one")
            .set(FirestoreTest.serializer(), FirestoreTest("aaa"))
        Firebase.firestore.collection("testFirestoreQuerying")
            .document("two")
            .set(FirestoreTest.serializer(), FirestoreTest("bbb"))
        Firebase.firestore.collection("testFirestoreQuerying")
            .document("three")
            .set(FirestoreTest.serializer(), FirestoreTest("ccc"))
    }
}
