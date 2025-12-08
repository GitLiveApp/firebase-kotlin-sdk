package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.runTest
import kotlinx.serialization.KSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

@IgnoreForAndroidUnitTest
open class AggregateQueryTest : BaseFirebaseFirestoreTest() {

    companion object {
        const val COLLECTION = "testFirestoreQuerying"
        val testOne = FirestoreTest(
            "aaa",
            0.0,
            1,
            listOf("a", "aa", "aaa"),
            "notNull",
            NestedObject("ddd"),
            listOf(NestedObject("l1"), NestedObject("l2"), NestedObject("l3")),
            100.milliseconds,
        )
        val testTwo = FirestoreTest(
            "bbb",
            0.0,
            2,
            listOf("b", "bb", "ccc"),
            null,
            NestedObject("eee"),
            listOf(NestedObject("l2"), NestedObject("l4"), NestedObject("l5")),
            200.milliseconds,
        )
        val testThree = FirestoreTest(
            "ccc",
            1.0,
            3,
            listOf("c", "cc", "ccc"),
            "notNull",
            NestedObject("fff"),
            listOf(NestedObject("l3"), NestedObject("l6"), NestedObject("l7")),
            300.milliseconds,
        )
    }

    private val collection get() = firestore.collection(COLLECTION)

    @Test
    fun testCount() = runTestWithFirestoreData {
        assertEquals(
            3L,
            collection
                .count().get().count,
        )

        assertEquals(
            3L,
            collection.aggregate(AggregateField.count()).get()[AggregateField.count()],
        )
    }

    @Test
    fun testAverage() = runTestWithFirestoreData {
        val averageTime = AggregateField.average(FirestoreTest::time.name)
        val averageCount = AggregateField.average(FirestoreTest::count.name)
        val averageProp = AggregateField.average(FirestoreTest::prop1.name)
        val aggregate = collection.aggregate(averageTime, averageCount, averageProp).get()
        assertEquals(
            2.0,
            aggregate[averageCount],
        )
        assertEquals(
            1.0 / 3.0,
            aggregate[averageTime],
        )
        assertNull(aggregate[averageProp])
    }

    @Test
    fun testSum() = runTestWithFirestoreData {
        val sumTime = AggregateField.sum(FirestoreTest::time.name)
        val sumCount = AggregateField.sum(FirestoreTest::count.name)
        val sumProp = AggregateField.sum(FirestoreTest::prop1.name)
        val aggregate = collection.aggregate(sumTime, sumCount, sumProp).get()
        assertEquals(
            6,
            aggregate[sumCount]?.toInt(),
        )
        assertEquals(
            1,
            aggregate[sumTime]?.toInt(),
        )
        assertEquals(0, aggregate[sumProp]?.toInt())
    }

    private fun runTestWithFirestoreData(
        documentOne: FirestoreTest = testOne,
        documentTwo: FirestoreTest = testTwo,
        documentThree: FirestoreTest = testThree,
        block: suspend () -> Unit,
    ) = runTest {
        try {
            setupFirestoreData(documentOne, documentTwo, documentThree)
            block()
        } finally {
            cleanFirestoreData()
        }
    }

    private suspend fun setupFirestoreData(
        documentOne: FirestoreTest = testOne,
        documentTwo: FirestoreTest = testTwo,
        documentThree: FirestoreTest = testThree,
    ) {
        firestore.collection(COLLECTION)
            .document("one")
            .set(FirestoreTest.serializer(), documentOne)
        firestore.collection(COLLECTION)
            .document("two")
            .set(FirestoreTest.serializer(), documentTwo)
        firestore.collection(COLLECTION)
            .document("three")
            .set(FirestoreTest.serializer(), documentThree)
    }

    private suspend fun cleanFirestoreData() {
        firestore.collection(COLLECTION).document("one").delete()
        firestore.collection(COLLECTION).document("two").delete()
        firestore.collection(COLLECTION).document("three").delete()
    }

    private suspend fun <T> Query.assertDocuments(serializer: KSerializer<T>, vararg expected: T) {
        val documents = get().documents
        assertEquals(expected.size, documents.size)
        documents.forEachIndexed { index, documentSnapshot ->
            assertEquals(expected[index], documentSnapshot.data(serializer))
        }
    }
}
