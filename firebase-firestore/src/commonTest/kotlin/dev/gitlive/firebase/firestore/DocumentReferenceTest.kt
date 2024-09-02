package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@IgnoreForAndroidUnitTest
class DocumentReferenceTest : BaseFirebaseFirestoreTest() {

    @Serializable
    data class FirestoreTimeTest(
        val prop1: String,
        val time: BaseTimestamp?,
    )

    @Serializable
    data class TestDataWithDocumentReference(
        val uid: String,
        val reference: DocumentReference,
        val optionalReference: DocumentReference?,
    )

    @Serializable
    data class TestDataWithOptionalDocumentReference(
        val optionalReference: DocumentReference?,
    )

    @Test
    fun encodeDocumentReference() = runTest {
        val doc = firestore.document("a/b")
        val item = TestDataWithDocumentReference("123", doc, doc)
        val encoded = encodedAsMap(
            encode(item) {
                encodeDefaults = false
            },
        )
        assertEquals("123", encoded["uid"])
        assertEquals(doc.nativeValue, encoded["reference"])
        assertEquals(doc.nativeValue, encoded["optionalReference"])
    }

    @Test
    fun encodeNullDocumentReference() = runTest {
        val item = TestDataWithOptionalDocumentReference(null)
        val encoded = encodedAsMap(
            encode(item) {
                encodeDefaults = false
            },
        )
        assertNull(encoded["optionalReference"])
    }

    @Test
    fun decodeDocumentReference() = runTest {
        val doc = firestore.document("a/b")
        val obj = mapOf(
            "uid" to "123",
            "reference" to doc.nativeValue,
            "optionalReference" to doc.nativeValue,
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
    fun testServerTimestampFieldValue() = runTest {
        val doc = firestore
            .collection("testServerTimestampFieldValue")
            .document("test")
        doc.set(
            FirestoreTimeTest.serializer(),
            FirestoreTimeTest("ServerTimestamp", Timestamp(123, 0)),
        )
        assertEquals(Timestamp(123, 0), doc.get().get("time", TimestampSerializer))

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestamp", Timestamp.ServerTimestamp))

        assertNotEquals(Timestamp.ServerTimestamp, doc.get().get("time", BaseTimestamp.serializer()))
        assertNotEquals(Timestamp.ServerTimestamp, doc.get().data(FirestoreTimeTest.serializer()).time)
    }

    @Test
    fun testServerTimestampBehaviorNone() = runTest {
        val doc = firestore
            .collection("testServerTimestampBehaviorNone")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100.milliseconds) // makes possible to catch pending writes snapshot

        doc.set(
            FirestoreTimeTest.serializer(),
            FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp),
        )

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", BaseTimestamp.serializer().nullable, serverTimestampBehavior = ServerTimestampBehavior.NONE))
    }

    @Test
    fun testServerTimestampBehaviorEstimate() = runTest {
        val doc = firestore
            .collection("testServerTimestampBehaviorEstimate")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100.milliseconds) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get<BaseTimestamp?>("time", ServerTimestampBehavior.ESTIMATE))
        assertNotEquals(Timestamp.ServerTimestamp, pendingWritesSnapshot.data(FirestoreTimeTest.serializer(), serverTimestampBehavior = ServerTimestampBehavior.ESTIMATE).time)
    }

    @Test
    fun testServerTimestampBehaviorPrevious() = runTest {
        val doc = firestore
            .collection("testServerTimestampBehaviorPrevious")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100.milliseconds) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", BaseTimestamp.serializer().nullable, serverTimestampBehavior = ServerTimestampBehavior.PREVIOUS))
    }

    @Test
    fun testDocumentAutoId() = runTest {
        val doc = firestore
            .collection("testDocumentAutoId")
            .document

        doc.set(FirestoreTest.serializer(), FirestoreTest("AutoId"))

        val resultDoc = firestore
            .collection("testDocumentAutoId")
            .document(doc.id)
            .get()

        assertEquals(true, resultDoc.exists)
        assertEquals("AutoId", resultDoc.get("prop1"))
    }

    @Test
    fun testUpdateValues() = runTest {
        val doc = firestore
            .collection("testFirestoreUpdateMultipleValues")
            .document("test1")

        doc.set(FirestoreTest.serializer(), FirestoreTest("property", count = 0, nested = NestedObject("nested"), duration = 600.milliseconds))
        val dataBefore = doc.get().data(FirestoreTest.serializer())
        assertEquals(0, dataBefore.count)
        assertNull(dataBefore.optional)
        assertEquals(NestedObject("nested"), dataBefore.nested)
        assertEquals(600.milliseconds, dataBefore.duration)

        doc.update {
            FirestoreTest::count.name to 5
            FieldPath(FirestoreTest::optional.name) to "notNull"
            FirestoreTest::nested.name.to(NestedObject.serializer(), NestedObject("newProperty"))
            FieldPath(FirestoreTest::duration.name).to(DurationAsLongSerializer(), 700.milliseconds)
        }
        val dataAfter = doc.get().data(FirestoreTest.serializer())
        assertEquals(5, dataAfter.count)
        assertEquals("notNull", dataAfter.optional)
        assertEquals(NestedObject("newProperty"), dataAfter.nested)
        assertEquals(700.milliseconds, dataAfter.duration)
    }

    @Test
    fun testIncrementFieldValue() = runTest {
        val doc = firestore
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
        val doc = firestore
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
        val doc = firestore
            .collection("testFirestoreArrayRemove")
            .document("test1")

        doc.set(FirestoreTest.serializer(), FirestoreTest("increment1", list = listOf("first", "second")))
        val dataBefore = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first", "second"), dataBefore.list)

        doc.update("list" to FieldValue.arrayRemove("second"))
        val dataAfter = doc.get().data(FirestoreTest.serializer())
        assertEquals(listOf("first"), dataAfter.list)
    }

    @Test
    fun testLegacyDoubleTimestamp() = runTest {
        @Serializable
        data class DoubleTimestamp(
            @Serializable(with = DoubleAsTimestampSerializer::class)
            val time: Double?,
        )

        val doc = firestore
            .collection("testLegacyDoubleTimestamp")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100.milliseconds) // makes possible to catch pending writes snapshot

        doc.set(DoubleTimestamp.serializer(), DoubleTimestamp(DoubleAsTimestampSerializer.SERVER_TIMESTAMP))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get("time", DoubleAsTimestampSerializer, serverTimestampBehavior = ServerTimestampBehavior.ESTIMATE))
        assertNotEquals(DoubleAsTimestampSerializer.SERVER_TIMESTAMP, pendingWritesSnapshot.data(DoubleTimestamp.serializer(), serverTimestampBehavior = ServerTimestampBehavior.ESTIMATE).time)
    }

    @Test
    fun testLegacyDoubleTimestampWriteNewFormatRead() = runTest {
        @Serializable
        data class LegacyDocument(
            @Serializable(with = DoubleAsTimestampSerializer::class)
            val time: Double,
        )

        @Serializable
        data class NewDocument(
            val time: Timestamp,
        )

        val doc = firestore
            .collection("testLegacyDoubleTimestampEncodeDecode")
            .document("testLegacy")

        val ms = 12345678.0

        doc.set(LegacyDocument.serializer(), LegacyDocument(time = ms))

        val fetched: NewDocument = doc.get().data(NewDocument.serializer())
        assertEquals(ms, fetched.time.toMilliseconds())
    }

    @Test
    fun testGeoPointSerialization() = runTest {
        @Serializable
        data class DataWithGeoPoint(val geoPoint: GeoPoint)

        fun getDocument() = firestore.collection("geoPointSerialization")
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
            val documentReference: DocumentReference,
        )

        fun getCollection() = firestore.collection("documentReferenceSerialization")
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
        getDocument().update {
            FieldPath(DataWithDocumentReference::documentReference.name).to(
                DocumentReferenceSerializer,
                updatedData.documentReference,
            )
        }
        // verify update
        val updatedSavedData = getDocument().get().data(DataWithDocumentReference.serializer())
        assertEquals(updatedData.documentReference.path, updatedSavedData.documentReference.path)
    }

    @Test
    fun testFieldValuesOps() = runTest {
        @Serializable
        data class TestData(val values: List<Int>)
        fun getDocument() = firestore.collection("fieldValuesOps")
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

    private suspend fun nonSkippedDelay(timeout: Duration) = withContext(Dispatchers.Default) {
        delay(timeout)
    }
}
