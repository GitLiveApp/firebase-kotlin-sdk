/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.TransactionOptions
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Exercises the `com.google.firebase.firestore` layer exactly as Android app code would (the static accessors, the
 * Task-based reference API, snapshots and their typed getters, queries, filters and aggregations, listeners and the Flow
 * extensions, transactions, batches, field values and the error codes), on every platform, against the Firestore emulator.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    companion object {
        private var nextAppId = 0
    }

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
            "firestoreCompatTest${nextAppId++}",
        )
        firestore = Firebase.firestore(app.compat).apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { setGcSettings(memoryEagerGcSettings { }) })
            }
            useEmulator(emulatorHost, 8080)
        }
    }

    // Each test gets its own app, so a fresh client: the JS SDK only accepts settings and the emulator before first use.
    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
    }

    private fun collection(name: String) = firestore.collection("compat").document("test").collection(name)

    @Test
    fun testInstancesAndSettings() = runTest {
        assertEquals(firestore, FirebaseFirestore.getInstance(app.compat))
        assertEquals(app.compat, firestore.app)
        assertIs<MemoryCacheSettings>(firestore.firestoreSettings.cacheSettings)
        assertNull(firestore.persistentCacheIndexManager)
        assertNull(firestore.getNamedQuery("missing").await())

        val reference = firestore.document("compat/test/instances/doc")
        assertEquals("doc", reference.id)
        assertEquals("compat/test/instances/doc", reference.path)
        assertEquals("instances", reference.parent.id)
        assertEquals("compat/test/instances", reference.parent.path)
        assertEquals("test", reference.parent.parent?.id)
        assertEquals(firestore, reference.firestore)
        assertEquals(reference, firestore.collection("compat/test/instances").document("doc"))
        assertNull(firestore.collection("compat").parent)
    }

    @Test
    fun testDocumentReadsAndWrites() = runTest {
        val documents = collection("documents")
        val other = documents.document("other")
        val document = documents.document("doc")
        document.set(
            mapOf(
                "name" to "first",
                "count" to 1L,
                "ratio" to 1.5,
                "flag" to true,
                "list" to listOf(1L, "a"),
                "nested" to mapOf("key" to "value"),
                "time" to Timestamp(1_000_000L, 500_000),
                "geo" to GeoPoint(1.5, 2.5),
                "reference" to other,
                "blob" to Blob.fromBytes(byteArrayOf(1, 2, 3)),
                "missing" to null,
            ),
        ).await()

        val snapshot = document.get().await()
        assertTrue(snapshot.exists())
        assertEquals("doc", snapshot.id)
        assertEquals(document, snapshot.reference)
        assertFalse(snapshot.metadata.hasPendingWrites())
        assertEquals("first", snapshot.getString("name"))
        assertEquals(1L, snapshot.getLong("count"))
        assertEquals(1.0, snapshot.getDouble("count"))
        assertEquals(1.5, snapshot.getDouble("ratio"))
        assertEquals(true, snapshot.getBoolean("flag"))
        assertEquals(listOf(1L, "a"), snapshot.get("list"))
        assertEquals(mapOf("key" to "value"), snapshot.get("nested"))
        assertEquals("value", snapshot.get("nested.key"))
        assertEquals("value", snapshot.get(FieldPath.of("nested", "key")))
        assertEquals(Timestamp(1_000_000L, 500_000), snapshot.getTimestamp("time"))
        assertEquals(GeoPoint(1.5, 2.5), snapshot.getGeoPoint("geo"))
        assertEquals(other, snapshot.getDocumentReference("reference"))
        assertContentEquals(byteArrayOf(1, 2, 3), snapshot.getBlob("blob")?.toBytes())
        assertTrue(snapshot.contains("name"))
        assertTrue(snapshot.contains("missing"))
        assertFalse(snapshot.contains("absent"))
        assertFalse(snapshot.contains(FieldPath.of("absent")))
        assertNull(snapshot.get("missing"))
        assertNull(snapshot.getString("absent"))
        assertEquals("first", snapshot.getField<String>("name"))
        assertEquals("first", snapshot.data?.get("name"))
        assertEquals(1L, snapshot.getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.get("count"))
        assertEquals("first", (snapshot.toObject<Any>() as Map<*, *>)["name"])

        document.update("count", 2L, "nested.key", "updated").await()
        document.update(mapOf("flag" to false)).await()
        document.update(FieldPath.of("name"), "second", FieldPath.of("ratio"), 2.5).await()
        val updated = document.get(Source.SERVER).await()
        assertEquals(2L, updated.getLong("count"))
        assertEquals("updated", updated.get("nested.key"))
        assertEquals(false, updated.getBoolean("flag"))
        assertEquals("second", updated.getString("name"))
        assertEquals(2.5, updated.getDouble("ratio"))

        document.set(mapOf("extra" to "merged"), SetOptions.merge()).await()
        document.set(mapOf("written" to 1L, "skipped" to 2L), SetOptions.mergeFields("written")).await()
        document.set(mapOf("path" to mapOf("written" to 3L)), SetOptions.mergeFieldPaths(listOf(FieldPath.of("path", "written")))).await()
        val merged = document.get().await()
        assertEquals("second", merged.getString("name"))
        assertEquals("merged", merged.getString("extra"))
        assertEquals(1L, merged.getLong("written"))
        assertFalse(merged.contains("skipped"))
        assertEquals(3L, merged.get("path.written"))

        document.set(mapOf("only" to "this")).await()
        val replaced = document.get().await()
        assertFalse(replaced.contains("name"))
        assertEquals("this", replaced.getString("only"))

        document.delete().await()
        assertFalse(document.get().await().exists())
        assertNull(document.get().await().data)

        val added = documents.add(mapOf("added" to true)).await()
        assertEquals(true, added.get().await().getBoolean("added"))
        added.delete().await()
    }

    @Test
    fun testFieldValues() = runTest {
        val document = collection("fieldValues").document("doc")
        document.set(mapOf("count" to 1L, "tags" to listOf("a"), "gone" to "soon")).await()
        document.update(
            mapOf(
                "count" to FieldValue.increment(5L),
                "ratio" to FieldValue.increment(0.5),
                "tags" to FieldValue.arrayUnion("b", "c"),
                "gone" to FieldValue.delete(),
                "time" to FieldValue.serverTimestamp(),
            ),
        ).await()
        document.update("tags", FieldValue.arrayRemove("a")).await()

        val snapshot = document.get().await()
        assertEquals(6L, snapshot.getLong("count"))
        assertEquals(0.5, snapshot.getDouble("ratio"))
        assertEquals(listOf("b", "c"), snapshot.get("tags"))
        assertFalse(snapshot.contains("gone"))
        assertNotNull(snapshot.getTimestamp("time"))
        assertEquals(FieldValue.serverTimestamp(), FieldValue.serverTimestamp())
        document.delete().await()
    }

    @Test
    fun testQueriesAndAggregations() = runTest {
        val scores = collection("queries")
        scores.document("a").set(mapOf("name" to "a", "score" to 1L, "tags" to listOf("x"))).await()
        scores.document("b").set(mapOf("name" to "b", "score" to 2L, "tags" to listOf("x", "y"))).await()
        scores.document("c").set(mapOf("name" to "c", "score" to 3L, "tags" to listOf("y"))).await()

        assertEquals(listOf("c", "b"), scores.whereGreaterThan("score", 1L).orderBy("score", Query.Direction.DESCENDING).get().await().map { it.id })
        assertEquals(listOf("a", "b"), scores.orderBy(FieldPath.of("score")).limit(2).get().await().documents.map { it.id })
        assertEquals(listOf("b", "c"), scores.orderBy("score").limitToLast(2).get().await().map { it.id })
        assertEquals(listOf("b"), scores.whereEqualTo("name", "b").get().await().map { it.id })
        assertEquals(listOf("a", "c"), scores.whereNotEqualTo("name", "b").orderBy("name").get().await().map { it.id })
        assertEquals(listOf("a", "b"), scores.whereLessThanOrEqualTo(FieldPath.of("score"), 2L).get().await().map { it.id })
        assertEquals(listOf("a", "c"), scores.whereIn("name", listOf("a", "c")).get().await().map { it.id })
        assertEquals(listOf("b"), scores.whereNotIn("name", listOf("a", "c")).get().await().map { it.id })
        assertEquals(listOf("a", "b"), scores.whereArrayContains("tags", "x").get().await().map { it.id })
        assertEquals(listOf("a", "b", "c"), scores.whereArrayContainsAny("tags", listOf("x", "y")).get().await().map { it.id })
        assertEquals(listOf("a", "c"), scores.where(Filter.or(Filter.equalTo("name", "a"), Filter.greaterThan(FieldPath.of("score"), 2L))).orderBy("name").get().await().map { it.id })
        assertEquals(listOf("b"), scores.where(Filter.and(Filter.greaterThan("score", 1L), Filter.lessThan("score", 3L))).get().await().map { it.id })
        assertEquals(listOf("b", "c"), scores.orderBy("score").startAfter(1L).get().await().map { it.id })
        assertEquals(listOf("b", "c"), scores.orderBy("score").startAt(2L).endAt(3L).get().await().map { it.id })
        assertEquals(listOf("a"), scores.orderBy("score").endBefore(2L).get().await().map { it.id })
        val first = scores.document("a").get().await()
        assertEquals(listOf("b", "c"), scores.orderBy("score").startAfter(first).get().await().map { it.id })
        assertEquals(listOf("a", "b"), scores.orderBy("score").endAt(scores.document("b").get().await()).get().await().map { it.id })
        assertEquals(listOf("c"), firestore.collectionGroup("queries").whereEqualTo("score", 3L).get().await().map { it.id })

        val snapshot = scores.orderBy("name").get().await()
        assertEquals(3, snapshot.size())
        assertFalse(snapshot.isEmpty)
        assertEquals(scores.orderBy("name"), snapshot.query)
        assertEquals(listOf("a", "b", "c"), snapshot.toObjects<Any>().map { (it as Map<*, *>)["name"] })
        assertEquals(setOf(DocumentChange.Type.ADDED), snapshot.documentChanges.map { it.type }.toSet())
        assertEquals(listOf(0, 1, 2), snapshot.getDocumentChanges(MetadataChanges.EXCLUDE).map { it.newIndex })
        assertTrue(scores.whereEqualTo("name", "none").get().await().isEmpty)

        assertEquals(3L, scores.count().get(AggregateSource.SERVER).await().count)
        val sum = AggregateField.sum("score")
        val average = AggregateField.average(FieldPath.of("score"))
        val count = AggregateField.count()
        val aggregate = scores.aggregate(sum, average, count)
        assertEquals(listOf(sum, average, count), aggregate.aggregateFields)
        assertEquals(scores, aggregate.query)
        val aggregates = aggregate.get(AggregateSource.SERVER).await()
        assertEquals(6L, aggregates.getLong(sum))
        assertEquals(6.0, aggregates.getDouble(sum))
        assertEquals(2.0, aggregates.get(average))
        assertEquals(3L, aggregates.get(count))
        assertEquals(3L, aggregates.count)
        assertEquals(aggregate, aggregates.query)
        assertEquals("sum_score", sum.alias)
        assertEquals("score", average.fieldPath)
        assertEquals("count", count.operator)

        scores.get().await().forEach { it.reference.delete().await() }
    }

    @Test
    fun testListeners() = runTest {
        val document = collection("listeners").document("doc")
        val events = CompletableDeferred<DocumentSnapshot>()
        val registration = document.addSnapshotListener(MetadataChanges.EXCLUDE) { snapshot, error ->
            if (error != null) {
                events.completeExceptionally(error)
            } else if (snapshot != null && snapshot.exists()) {
                events.complete(snapshot)
            }
        }
        val inSync = CompletableDeferred<Unit>()
        val syncRegistration = firestore.addSnapshotsInSyncListener { inSync.complete(Unit) }
        document.set(mapOf("name" to "listened")).await()
        assertEquals("listened", awaitEvent { events.await() }.getString("name"))
        awaitEvent { inSync.await() }
        registration.remove()
        syncRegistration.remove()

        val query = collection("listeners").whereEqualTo("name", "listened")
        val queryEvents = CompletableDeferred<QuerySnapshot>()
        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                queryEvents.completeExceptionally(error)
            } else if (snapshot != null) {
                queryEvents.complete(snapshot)
            }
        }.also {
            assertEquals(listOf("doc"), awaitEvent { queryEvents.await() }.map { it.id })
            it.remove()
        }

        assertEquals("listened", awaitEvent { document.snapshots().first() }.getString("name"))
        assertEquals(listOf("doc"), awaitEvent { query.snapshots(MetadataChanges.INCLUDE).first() }.map { it.id })
        assertEquals("listened", (awaitEvent { document.dataObjects<Any>().first() } as Map<*, *>)["name"])
        assertEquals(listOf("listened"), awaitEvent { query.dataObjects<Any>().first() }.map { (it as Map<*, *>)["name"] })
        document.delete().await()
    }

    /** Waits for a listener event in real time (the test dispatcher's virtual time would expire the timeout immediately). */
    private suspend fun <T> awaitEvent(block: suspend () -> T): T = withContext(Dispatchers.Default) { withTimeout(1.minutes) { block() } }

    @Test
    fun testTransactionsAndBatches() = runTest {
        val documents = collection("transactions")
        val first = documents.document("first")
        val second = documents.document("second")
        first.set(mapOf("count" to 1L)).await()

        val result = firestore.runTransaction { transaction ->
            transaction.update(first, "count", 2L)
            transaction.set(second, mapOf("count" to 10L))
            transaction.set(second, mapOf("merged" to true), SetOptions.merge())
            transaction.update(second, mapOf("count" to 11L))
            transaction.update(second, FieldPath.of("nested", "value"), "x")
            "done"
        }.await()
        assertEquals("done", result)
        assertEquals(2L, first.get().await().getLong("count"))
        val secondSnapshot = second.get().await()
        assertEquals(11L, secondSnapshot.getLong("count"))
        assertEquals(true, secondSnapshot.getBoolean("merged"))
        assertEquals("x", secondSnapshot.get("nested.value"))

        val options = TransactionOptions.Builder().setMaxAttempts(3).build()
        assertEquals(3, options.maxAttempts)
        assertEquals(1, TransactionOptions.Builder(options).setMaxAttempts(1).build().maxAttempts)
        assertEquals(5, TransactionOptions.Builder().build().maxAttempts)
        assertEquals(
            42L,
            firestore.runTransaction(options) { transaction ->
                transaction.delete(second)
                42L
            }.await(),
        )
        assertFalse(second.get().await().exists())

        val failure = assertFailsWith<FirebaseFirestoreException> {
            firestore.runTransaction<Nothing> { throw FirebaseFirestoreException("rolled back", FirebaseFirestoreException.Code.ABORTED) }.await()
        }
        assertEquals("rolled back", failure.message)
        assertEquals(FirebaseFirestoreException.Code.ABORTED, failure.code)

        firestore.runBatch { batch ->
            batch.set(second, mapOf("count" to 1L))
            batch.update(first, "count", 3L)
        }.await()
        assertEquals(1L, second.get().await().getLong("count"))
        assertEquals(3L, first.get().await().getLong("count"))

        firestore.batch()
            .set(second, mapOf("count" to 5L, "extra" to "value"), SetOptions.mergeFields("count"))
            .update(second, FieldPath.of("count"), 6L)
            .update(first, mapOf("count" to 4L))
            .delete(first)
            .commit()
            .await()
        val batched = second.get().await()
        assertEquals(6L, batched.getLong("count"))
        assertFalse(batched.contains("extra"))
        assertFalse(first.get().await().exists())
        second.delete().await()
    }

    @Test
    fun testErrors() = runTest {
        val missing = collection("errors").document("missing")
        val exception = assertFailsWith<FirebaseFirestoreException> { missing.update(mapOf("count" to 1L)).await() }
        assertEquals(FirebaseFirestoreException.Code.NOT_FOUND, exception.code)
        assertEquals(5, FirebaseFirestoreException.Code.NOT_FOUND.value())
        assertEquals(FirebaseFirestoreException.Code.NOT_FOUND, FirebaseFirestoreException.Code.fromValue(5))
        assertEquals(FirebaseFirestoreException.Code.UNKNOWN, FirebaseFirestoreException.Code.fromValue(99))
        val wrapped = FirebaseFirestoreException("not found", FirebaseFirestoreException.Code.NOT_FOUND, exception)
        assertTrue(wrapped.message!!.startsWith("not found"))
        assertEquals(FirebaseFirestoreException.Code.NOT_FOUND, wrapped.code)
    }

    @Test
    fun testNetwork() = runTest {
        val document = collection("network").document("doc")
        document.set(mapOf("name" to "online")).await()
        // A listener keeps the document in the (eagerly collected) memory cache while the network is off.
        val listened = CompletableDeferred<Unit>()
        val registration = document.addSnapshotListener { snapshot, _ -> if (snapshot?.exists() == true) listened.complete(Unit) }
        awaitEvent { listened.await() }
        firestore.disableNetwork().await()
        val cached = document.get(Source.CACHE).await()
        assertEquals("online", cached.getString("name"))
        assertTrue(cached.metadata.isFromCache)
        firestore.enableNetwork().await()
        registration.remove()
        document.update("name", "pending").await()
        firestore.waitForPendingWrites().await()
        assertEquals("pending", document.get(Source.SERVER).await().getString("name"))
        document.delete().await()
        firestore.terminate().await()
    }
}
