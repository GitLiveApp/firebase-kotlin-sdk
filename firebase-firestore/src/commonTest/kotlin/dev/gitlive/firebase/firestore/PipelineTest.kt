/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Pipeline
import com.google.firebase.firestore.PipelineResult
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings
import com.google.firebase.firestore.pipeline.AggregateFunction
import com.google.firebase.firestore.pipeline.AggregateStage
import com.google.firebase.firestore.pipeline.Expression
import com.google.firebase.firestore.pipeline.Ordering
import com.google.firebase.serialization.setData
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the `com.google.firebase.firestore.pipeline` layer exactly as Android app code would (sources, the stage
 * functions, expressions and aggregations, the results), on every platform, against the Firestore emulator.
 */
@IgnoreForAndroidUnitTest
class PipelineTest {

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
            "pipelineTest${nextAppId++}",
        )
        firestore = Firebase.firestore(app.compat).apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { setGcSettings(memoryEagerGcSettings { }) })
            }
            useEmulator(emulatorHost, 8080)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
    }

    private suspend fun books() = firestore.collection("compat").document("test").collection("pipelineBooks").also { books ->
        books.document("b1").setData(mapOf("title" to "Dune", "author" to "Herbert", "genre" to "sf", "rating" to 4.6, "year" to 1965, "inPrint" to true, "tags" to listOf("classic", "space"))).await()
        books.document("b2").setData(mapOf("title" to "Neuromancer", "author" to "Gibson", "genre" to "sf", "rating" to 4.1, "year" to 1984, "inPrint" to false, "tags" to listOf("cyberpunk"))).await()
        books.document("b3").setData(mapOf("title" to "Emma", "author" to "Austen", "genre" to "romance", "rating" to 4.0, "year" to 1815, "inPrint" to true, "tags" to listOf("classic"))).await()
        books.document("b4").setData(mapOf("title" to "Persuasion", "author" to "Austen", "genre" to "romance", "rating" to 4.2, "year" to 1817, "inPrint" to true, "tags" to emptyList<String>())).await()
    }

    @Test
    fun testFilterSortLimitAndSelect() = runTest {
        val books = books()
        val topSf: Pipeline = firestore.pipeline().collection(books)
            .where(Expression.field("genre").equal("sf"))
            .sort(Ordering.descending("rating"))
            .limit(1)
        val snapshot: Pipeline.Snapshot = topSf.execute().await()
        assertNotNull(snapshot.executionTime)
        val result: PipelineResult = snapshot.results.single()
        assertEquals("Dune", result.get("title"))
        assertEquals("b1", result.getId())
        assertEquals(books.document("b1").path, result.ref?.path)
        assertNotNull(result.createTime)
        assertNotNull(result.updateTime)
        assertNull(result.get("missing"))
        assertEquals(listOf(result), snapshot.toList())

        // A select keeps only the selected fields; the document key goes unless selected too
        val selected = topSf.select("title", Expression.field("rating").multiply(10).alias("score")).execute().await().results.single()
        assertEquals(setOf("title", "score"), selected.getData().keys)
        assertEquals("Dune", selected.get("title"))
        assertEquals(46.0, (selected.get("score") as Number).toDouble(), 0.001)
    }

    @Test
    fun testBooleanFieldAndFieldNameBuilders() = runTest {
        val books = books()
        // A boolean field is a condition through asBoolean(); the field-name overloads read the field, not the name
        val results = firestore.pipeline().collection(books)
            .where(Expression.field("inPrint").asBoolean())
            .where(Expression.field("genre").equal("sf"))
            .select(
                Expression.stringRepeat("genre", 2).alias("twice"),
                Expression.rtrim("title").alias("trimmed"),
                Expression.arrayIndexOf("tags", "space").alias("space"),
                Expression.arrayLastIndexOf("tags", "space").alias("lastSpace"),
            )
            .execute().await().results
        val dune = results.single()
        assertEquals("sfsf", dune.get("twice"))
        assertEquals("Dune", dune.get("trimmed"))
        assertEquals(1L, (dune.get("space") as Number).toLong())
        assertEquals(1L, (dune.get("lastSpace") as Number).toLong())
    }

    @Test
    fun testAggregation() = runTest {
        val books = books()
        val byGenre = firestore.pipeline().collection(books)
            .aggregate(
                AggregateStage.withAccumulators(AggregateFunction.countAll().alias("count"), AggregateFunction.average("rating").alias("averageRating"))
                    .withGroups("genre"),
            )
            .sort(Ordering.ascending("genre"))
            .execute().await()
        assertEquals(listOf("romance", "sf"), byGenre.results.map { it.get("genre") })
        assertEquals(listOf(2L, 2L), byGenre.results.map { (it.get("count") as Number).toLong() })
        assertEquals(4.1, (byGenre.results[0].get("averageRating") as Number).toDouble(), 0.001)
        assertTrue(byGenre.results.all { it.ref == null && it.getId() == null })

        val total = firestore.pipeline().collection(books)
            .aggregate(AggregateFunction.sum("year").alias("years"), Expression.field("rating").maximum().alias("best"))
            .execute().await()
        assertEquals(1965L + 1984L + 1815L + 1817L, (total.results.single().get("years") as Number).toLong())
        assertEquals(4.6, (total.results.single().get("best") as Number).toDouble(), 0.001)
    }

    @Test
    fun testSourcesAndExpressions() = runTest {
        val books = books()
        val fromQuery = firestore.pipeline().createFrom(books.whereEqualTo("author", "Austen").orderBy("year"))
            .addFields(Expression.field("title").stringConcat(" by ", Expression.field("author")).alias("label"))
            .execute().await()
        assertEquals(listOf("Emma by Austen", "Persuasion by Austen"), fromQuery.results.map { it.get("label") })

        val distinctAuthors = firestore.pipeline().collection(books).distinct("author").sort(Ordering.ascending("author")).execute().await()
        assertEquals(listOf("Austen", "Gibson", "Herbert"), distinctAuthors.results.map { it.get("author") })

        val documents = firestore.pipeline().documents(books.document("b2"), books.document("b3"))
            .sort(Ordering.ascending("year"))
            .select("title")
            .execute().await()
        assertEquals(listOf("Emma", "Neuromancer"), documents.results.map { it.get("title") })

        val classics = firestore.pipeline().collection(books)
            .where(Expression.field("tags").arrayContains("classic"))
            .sort(Ordering.ascending("title"))
            .select(Expression.field("tags").arrayLength().alias("tagCount"), Expression.field("year").add(1).alias("nextYear"))
            .execute().await()
        assertEquals(listOf(2L, 1L), classics.results.map { (it.get("tagCount") as Number).toLong() })
        assertEquals(listOf(1966L, 1816L), classics.results.map { (it.get("nextYear") as Number).toLong() })

        val paged = firestore.pipeline().collection(books).sort(Ordering.ascending("year")).offset(1).limit(2).select("year").execute().await()
        assertEquals(listOf(1817L, 1965L), paged.results.map { (it.get("year") as Number).toLong() })

        val group = firestore.pipeline().collectionGroup("pipelineBooks").where(Expression.field("year").greaterThan(1900)).execute().await()
        assertEquals(2, group.results.size)
    }
}
