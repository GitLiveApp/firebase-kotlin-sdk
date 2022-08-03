/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlin.random.Random
import kotlin.test.*

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend CoroutineScope.() -> Unit)

/** @return a map extracted from the encoded data. */
expect fun encodedAsMap(encoded: Any?): Map<String, Any?>
/** @return pairs as raw encoded data. */
expect fun Map<String, Any?>.asEncoded(): Any

// NOTE: serializer<T>() does not work in a legacy JS so serializers have to be provided explicitly
class FirebaseFirestoreTest {

    @Serializable
    data class FirestoreTest(
        val prop1: String,
        val time: Double = 0.0
    )

    @Serializable
    data class FirestoreTimeTest(
        val prop1: String,
        val time: BaseTimestamp?
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
            .collection("FirebaseFirestoreTest")
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
            FirestoreTimeTest.serializer(),
            FirestoreTimeTest("ServerTimestamp", Timestamp(123, 0)),
        )
        assertEquals(Timestamp(123, 0), doc.get().get("time", TimestampSerializer))

        doc.update(
            fieldsAndValues = arrayOf(
                "time" to Timestamp(321, 0)
            )
        )
        assertEquals(Timestamp(321, 0), doc.get().data(FirestoreTimeTest.serializer()).time)

        assertNotEquals<Any>(Timestamp.ServerTimestamp, doc.get().get("time", TimestampSerializer))
        assertNotEquals<Any?>(Timestamp.ServerTimestamp, doc.get().data(FirestoreTimeTest.serializer()).time)
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
            FirestoreTimeTest.serializer(),
            FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp)
        )

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", TimestampSerializer.nullable, ServerTimestampBehavior.NONE))
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

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get("time", TimestampSerializer, ServerTimestampBehavior.ESTIMATE))
        assertNotEquals(Timestamp(0, 0), pendingWritesSnapshot.data(FirestoreTimeTest.serializer(), ServerTimestampBehavior.ESTIMATE).time)
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

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", TimestampSerializer.nullable, ServerTimestampBehavior.PREVIOUS))
    }

    @Test
    fun testDocumentAutoId() = runTest {
        val doc = Firebase.firestore
            .collection("testDocumentAutoId")
            .document()

        doc.set(FirestoreTest.serializer(), FirestoreTest("AutoId"))

        val resultDoc = Firebase.firestore
            .collection("testDocumentAutoId")
            .document(doc.id)
            .get()

        assertEquals(true, resultDoc.exists)
        assertEquals("AutoId", resultDoc.get("prop1"))
    }

    @Test
    fun testSetBatchDoesNotEncodeEmptyValues() = runTest {
        val doc = Firebase.firestore
            .collection("testServerTestSetBatch")
            .document("test")
        val batch = Firebase.firestore.batch()
        batch.set(
            documentRef = doc,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1-set",
                time = 125.0
            ),
            fieldsAndValues = arrayOf<Pair<String, Any>>()
        )
        batch.commit()

        assertEquals(125.0, doc.get().get("time") as Double?)
        assertEquals("prop1-set", doc.get().data(FirestoreTest.serializer()).prop1)
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

    @Test
    fun testUpdateBatchDoesNotEncodeEmptyValues() = runTest {
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
                prop1 = "prop1-set",
                time = 126.0
            ),
            encodeDefaults = false,
            fieldsAndValues = arrayOf<Pair<String, Any>>()
        )
        batch.commit()

        assertEquals(126.0, doc.get().get("time") as Double?)
        assertEquals("prop1-set", doc.get().data(FirestoreTest.serializer()).prop1)
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

    @Test
    fun testGeoPointSerialization() = runTest {
        @Serializable
        data class DataWithGeoPoint(val geoPoint: GeoPoint)

        fun getDocument() = Firebase.firestore.collection("geoPointSerialization")
            .document("geoPointSerialization")

        val data = DataWithGeoPoint(GeoPoint(12.34, 56.78))
        // store geo point
        getDocument().set(DataWithGeoPoint.serializer(), data)
        // restore data
        val savedData = getDocument().get().data(DataWithGeoPoint.serializer())
        assertEquals(data.geoPoint, savedData.geoPoint)

        // update data
        val updatedData = DataWithGeoPoint(GeoPoint(87.65, 43.21))
        getDocument().update(FieldPath(DataWithGeoPoint::geoPoint.name) to updatedData.geoPoint)
        // verify update
        val updatedSavedData = getDocument().get().data(DataWithGeoPoint.serializer())
        assertEquals(updatedData.geoPoint, updatedSavedData.geoPoint)
    }

    @Test
    fun testDocumentReferenceSerialization() = runTest {
        @Serializable
        data class DataWithDocumentReference(
            val documentReference: DocumentReference
        )

        fun getCollection() = Firebase.firestore.collection("documentReferenceSerialization")
        fun getDocument() = getCollection()
            .document("documentReferenceSerialization")
        val documentRef1 = getCollection().document("refDoc1").apply {
            set(mapOf("value" to 1))
        }
        val documentRef2 = getCollection().document("refDoc2").apply {
            set(mapOf("value" to 2))
        }

        val data = DataWithDocumentReference(documentRef1)
        // store reference
        getDocument().set(DataWithDocumentReference.serializer(), data)
        // restore data
        val savedData = getDocument().get().data(DataWithDocumentReference.serializer())
        assertEquals(data.documentReference.path, savedData.documentReference.path)

        // update data
        val updatedData = DataWithDocumentReference(documentRef2)
        getDocument().update(
            FieldPath(DataWithDocumentReference::documentReference.name) to updatedData.documentReference.withSerializer(DocumentReferenceSerializer)
        )
        // verify update
        val updatedSavedData = getDocument().get().data(DataWithDocumentReference.serializer())
        assertEquals(updatedData.documentReference.path, updatedSavedData.documentReference.path)
    }

    @Serializable
    data class TestDataWithDocumentReference(
        val uid: String,
        val reference: DocumentReference,
        val optionalReference: DocumentReference?
    )

    @Serializable
    data class TestDataWithOptionalDocumentReference(
        val optionalReference: DocumentReference?
    )

    @Test
    fun encodeDocumentReference() = runTest {
        val doc = Firebase.firestore.document("a/b")
        val item = TestDataWithDocumentReference("123", doc, doc)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("123", encoded["uid"])
        assertEquals(doc.nativeValue, encoded["reference"])
        assertEquals(doc.nativeValue, encoded["optionalReference"])
    }

    @Test
    fun encodeNullDocumentReference() = runTest {
        val item = TestDataWithOptionalDocumentReference(null)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertNull(encoded["optionalReference"])
    }

    @Test
    fun decodeDocumentReference() = runTest {
        val doc = Firebase.firestore.document("a/b")
        val obj = mapOf(
            "uid" to "123",
            "reference" to doc.nativeValue,
            "optionalReference" to doc.nativeValue
        ).asEncoded()
        val decoded: TestDataWithDocumentReference = decode(obj)
        assertEquals("123", decoded.uid)
        assertEquals(doc.path, decoded.reference.path)
        assertEquals(doc.path, decoded.optionalReference?.path)
    }

    @Test
    fun decodeNullDocumentReference() = runTest {
        val obj = mapOf("optionalReference" to null).asEncoded()
        val decoded: TestDataWithOptionalDocumentReference = decode(obj)
        assertNull(decoded.optionalReference?.path)
    }

    @Test
    fun testFieldValuesOps() = runTest {
        @Serializable
        data class TestData(val values: List<Int>)
        fun getDocument() = Firebase.firestore.collection("fieldValuesOps")
            .document("fieldValuesOps")

        val data = TestData(listOf(1))
        // store
        getDocument().set(TestData.serializer(), data)
        // append & verify
        getDocument().update(FieldPath(TestData::values.name) to FieldValue.arrayUnion(2))

        var savedData = getDocument().get().data(TestData.serializer())
        assertEquals(listOf(1, 2), savedData.values)

        // remove & verify
        getDocument().update(FieldPath(TestData::values.name) to FieldValue.arrayRemove(1))
        savedData = getDocument().get().data(TestData.serializer())
        assertEquals(listOf(2), savedData.values)

        val list = getDocument().get().get(TestData::values.name, ListSerializer(Int.serializer()).nullable)
        assertEquals(listOf(2), list)
        // delete & verify
        getDocument().update(FieldPath(TestData::values.name) to FieldValue.delete)
        val deletedList = getDocument().get().get(TestData::values.name, ListSerializer(Int.serializer()).nullable)
        assertNull(deletedList)
    }

}
