/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.*
import kotlin.test.*

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseFirestoreTest {

    @Serializable
    data class FirestoreTest(val prop1: String, val time: Double? = 0.0)

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
                        projectId = "fir-kotlin-sdk"
                    )
                )
                Firebase.firestore.useEmulator(emulatorHost, 8080)
            }
    }

    @Test
    fun testStringOrderBy() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy("prop1").get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("ccc", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testFieldOrderBy() = runTest {
        setupFirestoreData()
        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy(FieldPath("prop1")).get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("ccc", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testStringOrderByAscending() = runTest {
        setupFirestoreData()
        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy("prop1", Direction.ASCENDING).get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("ccc", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testFieldOrderByAscending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy(FieldPath("prop1"), Direction.ASCENDING).get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("ccc", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testStringOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy("prop1", Direction.DESCENDING).get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("aaa", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testFieldOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = Firebase.firestore.collection("FirebaseFirestoreTest")
            .orderBy(FieldPath("prop1"), Direction.DESCENDING).get().documentChanges
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].document.get("prop1"))
        assertEquals("bbb", resultDocs[1].document.get("prop1"))
        assertEquals("aaa", resultDocs[2].document.get("prop1"))
    }

    @Test
    fun testServerTimestampFieldValue() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTimestampFieldValue")
            .document("test")
        doc.set(
            FirestoreTest.serializer(),
            FirestoreTest("ServerTimestamp"),
        )
        assertEquals(0.0, doc.get().get("time"))

        doc.update(
            fieldsAndValues = arrayOf(
                "time" to 123.0
            )
        )
        assertEquals(123.0, doc.get().data(FirestoreTest.serializer()).time)

    }

    @Test
    fun testExtendedSetBatch() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTestSetBatch")
            .document("test")
        val batch = Firebase.firestore.batch()
        batch.set(
            documentRef = doc,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1",
                time = 123.0
            ),
            fieldsAndValues = arrayOf(
                "time" to 124.0
            )
        )
        batch.commit()

        assertEquals(124.0, doc.get().get("time"))
        assertEquals("prop1", doc.get().data(FirestoreTest.serializer()).prop1)

    }

    @Test
    fun testExtendedUpdateBatch() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTestSetBatch")
            .document("test").apply {
                set(
                    FirestoreTest(
                        prop1 = "prop1",
                        time = 123.0
                    )
                )
            }
        val batch = Firebase.firestore.batch()
        batch.update(
            documentRef = doc,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1-updated",
                time = 123.0
            ),
            encodeDefaults = false,
            fieldsAndValues = arrayOf(
                "time" to FieldValue.delete
            )
        )
        batch.commit()

        assertEquals(null, doc.get().get("time") as Double?)
        assertEquals("prop1-updated", doc.get().data(FirestoreTest.serializer()).prop1)
    }

    private suspend fun setupFirestoreData() {
        Firebase.firestore.collection("FirebaseFirestoreTest")
            .document("one")
            .set(FirestoreTest.serializer(), FirestoreTest("aaa"))
        Firebase.firestore.collection("FirebaseFirestoreTest")
            .document("two")
            .set(FirestoreTest.serializer(), FirestoreTest("bbb"))
        Firebase.firestore.collection("FirebaseFirestoreTest")
            .document("three")
            .set(FirestoreTest.serializer(), FirestoreTest("ccc"))
    }

    @Test
    fun testDefaultOptions() = runTest {
        assertNull(FirebaseOptions.withContext(1))
    }
}
