/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

expect val emulatorHost: String
expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseFirestoreTest {

    @Serializable
    data class FirestoreTest(
        val prop1: String,
        val time: Double = 0.0,
        val count: Int = 0,
        val list: List<String> = emptyList(),
        val optional: String? = null,
    )

    @Serializable
    data class FirestoreTimeTest(
        val prop1: String,
        val time: BaseTimestamp?
    )

    companion object {
        val testOne = FirestoreTest(
            "aaa",
            0.0,
            1,
            listOf("a", "aa", "aaa"),
            "notNull",
        )
        val testTwo = FirestoreTest(
            "bbb",
            0.0,
            2,
            listOf("b", "bb", "ccc")
        )
        val testThree = FirestoreTest(
            "ccc",
            1.0,
            3,
            listOf("c", "cc", "ccc"),
            "notNull",
        )
    }

    lateinit var firestore: FirebaseFirestore

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
                gcmSenderId = "846484016111"
            )
        )

        firestore = Firebase.firestore(app).apply {
            useEmulator(emulatorHost, 8080)
            setSettings(FirebaseFirestore.Settings.create(cacheSettings = LocalCacheSettings.Memory(LocalCacheSettings.Memory.GarbageCollectorSettings.Eager)))
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testStringOrderBy() = runTest {
        setupFirestoreData()
        val resultDocs = firestore
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

        val resultDocs = firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1")).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testStringOrderByAscending() = runTest {
        setupFirestoreData()

        val resultDocs = firestore.collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testFieldOrderByAscending() = runTest {
        setupFirestoreData()

        val resultDocs = firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1"), Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("ccc", resultDocs[2].get("prop1"))
    }

    @Test
    fun testStringOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = firestore.collection("testFirestoreQuerying")
            .orderBy("prop1", Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("aaa", resultDocs[2].get("prop1"))
    }

    @Test
    fun testFieldOrderByDescending() = runTest {
        setupFirestoreData()

        val resultDocs = firestore.collection("testFirestoreQuerying")
            .orderBy(FieldPath("prop1"), Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get("prop1"))
        assertEquals("bbb", resultDocs[1].get("prop1"))
        assertEquals("aaa", resultDocs[2].get("prop1"))
    }

    @Test
    fun testServerTimestampFieldValue() = runTest {
        val doc = firestore
            .collection("testServerTimestampFieldValue")
            .document("test")

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
        nonSkippedDelay(100) // makes possible to catch pending writes snapshot

        doc.set(
            FirestoreTimeTest.serializer(),
            FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp)
        )

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", BaseTimestamp.serializer().nullable, ServerTimestampBehavior.NONE))
    }

    @Test
    fun testServerTimestampBehaviorEstimate() = runTest {
        val doc = firestore
            .collection("testServerTimestampBehaviorEstimate")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get("time", BaseTimestamp.serializer().nullable, ServerTimestampBehavior.ESTIMATE))
        assertNotEquals(Timestamp.ServerTimestamp, pendingWritesSnapshot.data(FirestoreTimeTest.serializer(), ServerTimestampBehavior.ESTIMATE).time)
    }

    @Test
    fun testServerTimestampBehaviorPrevious() = runTest {
        val doc = firestore
            .collection("testServerTimestampBehaviorPrevious")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100) // makes possible to catch pending writes snapshot

        doc.set(FirestoreTimeTest.serializer(), FirestoreTimeTest("ServerTimestampBehavior", Timestamp.ServerTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNull(pendingWritesSnapshot.get("time", BaseTimestamp.serializer().nullable, ServerTimestampBehavior.PREVIOUS))
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
    fun testStartAfterDocumentSnapshot() = runTest {
        setupFirestoreData()
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
        val query = firestore
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
            val time: Double?
        )

        val doc = firestore
            .collection("testLegacyDoubleTimestamp")
            .document("test${Random.nextInt()}")

        val deferredPendingWritesSnapshot = async {
            doc.snapshots.filter { it.exists }.first()
        }
        nonSkippedDelay(100) // makes possible to catch pending writes snapshot

        doc.set(DoubleTimestamp.serializer(), DoubleTimestamp(DoubleAsTimestampSerializer.serverTimestamp))

        val pendingWritesSnapshot = deferredPendingWritesSnapshot.await()
        assertTrue(pendingWritesSnapshot.metadata.hasPendingWrites)
        assertNotNull(pendingWritesSnapshot.get("time", DoubleAsTimestampSerializer, ServerTimestampBehavior.ESTIMATE ))
        assertNotEquals(DoubleAsTimestampSerializer.serverTimestamp, pendingWritesSnapshot.data(DoubleTimestamp.serializer(), ServerTimestampBehavior.ESTIMATE).time)
    }

    @Test
    fun testLegacyDoubleTimestampWriteNewFormatRead() = runTest {
        @Serializable
        data class LegacyDocument(
            @Serializable(with = DoubleAsTimestampSerializer::class)
            val time: Double
        )

        @Serializable
        data class NewDocument(
            val time: Timestamp
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
    fun testQueryByTimestamp() = runTest {
        @Serializable
        data class DocumentWithTimestamp(
            val time: Timestamp
        )

        val collection = firestore
            .collection("testQueryByTimestamp")

        val timestamp = Timestamp.fromMilliseconds(1693262549000.0)

        val pastTimestamp = Timestamp(timestamp.seconds - 60, 12345000) // note: iOS truncates 3 last digits of nanoseconds due to internal conversions
        val futureTimestamp = Timestamp(timestamp.seconds + 60, 78910000)

        collection.add(DocumentWithTimestamp.serializer(), DocumentWithTimestamp(pastTimestamp))
        collection.add(DocumentWithTimestamp.serializer(), DocumentWithTimestamp(futureTimestamp))

        val equalityQueryResult = collection.where {
            FieldPath(DocumentWithTimestamp::time.name) equalTo pastTimestamp
        }.get().documents.map { it.data(DocumentWithTimestamp.serializer()) }.toSet()

        assertEquals(setOf(DocumentWithTimestamp(pastTimestamp)), equalityQueryResult)

        val gtQueryResult = collection.where {
            FieldPath(DocumentWithTimestamp::time.name) greaterThan timestamp
        }.get().documents.map { it.data(DocumentWithTimestamp.serializer()) }.toSet()

        assertEquals(setOf(DocumentWithTimestamp(futureTimestamp)), gtQueryResult)
    }

    @Test
    fun testQueryEqualTo() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "prop1" equalTo testOne.prop1 }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::prop1.name) equalTo testTwo.prop1 }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testTwo)

        val nullableQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::optional.name) equalTo null }

        nullableQuery.assertDocuments(FirestoreTest.serializer(), testTwo)
    }

    @Test
    fun testQueryNotEqualTo() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "prop1" notEqualTo  testOne.prop1 }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::prop1.name) notEqualTo  testTwo.prop1 }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)

        val nullableQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::optional.name) notEqualTo  null }

        nullableQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)
    }

    @Test
    fun testQueryLessThan() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "count" lessThan testThree.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::count.name) lessThan testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testQueryGreaterThan() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "count" greaterThan testOne.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::count.name) greaterThan testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testQueryLessThanOrEqualTo() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "count" lessThanOrEqualTo testOne.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::count.name) lessThanOrEqualTo testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)
    }

    @Test
    fun testQueryGreaterThanOrEqualTo() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "count" greaterThanOrEqualTo testThree.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::count.name) greaterThanOrEqualTo  testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)
    }

    @Test
    fun testQueryArrayContains() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "list" contains "a" }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::list.name) contains "ccc" }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree, testTwo)
    }

    @Test
    fun testQueryArrayContainsAny() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "list" containsAny listOf("a", "b") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::list.name) containsAny listOf("c", "d") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testQueryInArray() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "prop1" inArray listOf("aaa", "bbb") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::prop1.name) inArray listOf("ccc", "ddd") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testQueryNotInArray() = runTest {
        setupFirestoreData()

        val fieldQuery = firestore
            .collection("testFirestoreQuerying")
            .where { "prop1" notInArray listOf("aaa", "bbb") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val pathQuery = firestore
            .collection("testFirestoreQuerying")
            .where { FieldPath(FirestoreTest::prop1.name) notInArray listOf("ccc", "ddd") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)
    }

    @Test
    fun testCompoundQuery() = runTest {
        setupFirestoreData()

        val andQuery = firestore
            .collection("testFirestoreQuerying")
            .where {
                FieldPath(FirestoreTest::prop1.name) inArray listOf("aaa", "bbb") and (FieldPath(FirestoreTest::count.name) equalTo 1)
            }
        andQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val orQuery = firestore
            .collection("testFirestoreQuerying")
            .where {
                FieldPath(FirestoreTest::prop1.name) equalTo "aaa" or (FieldPath(FirestoreTest::count.name) equalTo 2)
            }
        orQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val andOrQuery = firestore
            .collection("testFirestoreQuerying")
            .where {
                all(
                    any(
                        FieldPath(FirestoreTest::prop1.name) equalTo "aaa",
                        FieldPath(FirestoreTest::count.name) equalTo 2,
                    )!!,
                    FieldPath(FirestoreTest::list.name) contains "a"
                )
            }
        andOrQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    private suspend fun setupFirestoreData(
        documentOne: FirestoreTest = testOne,
        documentTwo: FirestoreTest = testTwo,
        documentThree: FirestoreTest = testThree
    ) {
        firestore.collection("testFirestoreQuerying")
            .document("one")
            .set(FirestoreTest.serializer(), documentOne)
        firestore.collection("testFirestoreQuerying")
            .document("two")
            .set(FirestoreTest.serializer(), documentTwo)
        firestore.collection("testFirestoreQuerying")
            .document("three")
            .set(FirestoreTest.serializer(), documentThree)
    }

    private suspend fun <T> Query.assertDocuments(serializer: KSerializer<T>, vararg expected: T) {
        val documents = get().documents
        assertEquals(expected.size, documents.size)
        documents.forEachIndexed { index, documentSnapshot ->
            assertEquals(expected[index], documentSnapshot.data(serializer))
        }
    }

    private suspend fun nonSkippedDelay(timeout: Long) = withContext(Dispatchers.Default) {
        delay(timeout)
    }
}
