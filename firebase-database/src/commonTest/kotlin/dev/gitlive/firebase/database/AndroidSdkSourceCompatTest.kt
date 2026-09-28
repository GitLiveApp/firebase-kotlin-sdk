/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import com.google.firebase.Firebase
import com.google.firebase.database.ChildEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Logger
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.childEvents
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.database.snapshots
import com.google.firebase.database.values
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Exercises the `com.google.firebase.database` layer exactly as Android app code would (the static accessors, the
 * Task-based reference API, listeners and the Flow extensions, queries, transactions with MutableData, disconnect
 * operations, server values, snapshots and the error codes), on every platform, against the Realtime Database emulator.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    private lateinit var app: dev.gitlive.firebase.FirebaseApp
    private lateinit var database: FirebaseDatabase

    @BeforeTest
    fun initializeFirebase() {
        app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk-default-rtdb.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk-default-rtdb",
                gcmSenderId = "846484016111",
            ),
        )
        database = Firebase.database(app.compat).apply {
            useEmulator(emulatorHost, 9000)
            setLogLevel(Logger.Level.NONE)
        }
    }

    // The JS SDK only accepts useEmulator before the database is used, so every test starts from a fresh app.
    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        dev.gitlive.firebase.Firebase.apps(context).forEach { it.delete() }
    }

    @Test
    fun testInstances() = runTest {
        ensureConnected()
        // firebase-java-sdk creates a new FirebaseDatabase per call, so the instances are compared by app.
        assertEquals(database.app, FirebaseDatabase.getInstance(app.compat).app)
        assertEquals(app.compat, database.app)
        assertEquals(database.app, dev.gitlive.firebase.Firebase.database(app).compat.app)
        assertTrue(FirebaseDatabase.getSdkVersion().isNotEmpty())
        val reference: DatabaseReference = database.getReference("test/compat/child")
        assertEquals("child", reference.key)
        assertEquals("compat", reference.parent?.key)
        assertNull(reference.root.key)
        assertEquals(database, reference.database)
        assertEquals(reference, database.reference.child("test").child("compat/child"))
        assertEquals(reference, database.getReference("test/compat").child("child").ref)
        assertNotNull(reference.push().key)
    }

    @Test
    fun testWritesReadsAndSnapshots() = runTest {
        ensureConnected()
        val reference = database.getReference("test/compatWrites")
        reference.setValue(mapOf("name" to "compat", "count" to 5, "nested" to mapOf("flag" to true))).await()

        val snapshot: DataSnapshot = reference.get().await()
        assertTrue(snapshot.exists(), "snapshot.exists()")
        assertEquals("compatWrites", snapshot.key)
        assertEquals(reference, snapshot.ref)
        assertEquals(3, snapshot.childrenCount)
        assertTrue(snapshot.hasChildren(), "snapshot.hasChildren()")
        assertTrue(snapshot.hasChild("nested/flag"), "snapshot.hasChild('nested/flag')")
        assertFalse(snapshot.hasChild("missing"), "snapshot.hasChild('missing')")
        assertEquals("compat", snapshot.child("name").value)
        assertEquals(5L, (snapshot.child("count").value as Number).toLong())
        assertEquals(true, snapshot.child("nested/flag").getValue<Boolean>())
        assertEquals("compat", snapshot.child("name").getValue(object : GenericTypeIndicator<String>() {}))
        assertEquals(setOf("count", "name", "nested"), snapshot.children.map { it.key }.toSet())
        val value = snapshot.getValue<Map<String, Any?>>()
        assertEquals("compat", value?.get("name"))
        assertNull(snapshot.priority)

        reference.updateChildren(mapOf("count" to 6, "name" to null)).await()
        val updated = reference.snapshots.first()
        assertFalse(updated.hasChild("name"), "updated.hasChild('name')")
        assertEquals(6L, (updated.child("count").value as Number).toLong())

        val completed = CompletableDeferred<DatabaseError?>()
        reference.child("count").setValue(7, DatabaseReference.CompletionListener { error, ref -> completed.complete(error.also { assertEquals(reference.child("count"), ref) }) })
        assertNull(completed.await())
        assertEquals(7L, reference.child("count").values<Long>().first()?.let { (it as Number).toLong() })

        reference.removeValue().await()
        assertFalse(reference.get().await().exists(), "reference.get().await().exists()")
        assertNull(reference.get().await().value)
    }

    @Test
    fun testListenersAndFlows() = runTest {
        ensureConnected()
        val reference = database.getReference("test/compatListeners")
        reference.removeValue().await()

        val first = CompletableDeferred<DataSnapshot>()
        val listener = reference.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    first.complete(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    first.completeExceptionally(error.toException())
                }
            },
        )
        assertFalse(first.await().exists())
        reference.removeEventListener(listener)

        val single = CompletableDeferred<DataSnapshot>()
        reference.child("a").setValue(1).await()
        reference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    single.complete(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    single.completeExceptionally(error.toException())
                }
            },
        )
        assertEquals(1L, (single.await().child("a").value as Number).toLong())

        val events = reference.childEvents.take(1).toList()
        val added = events.single()
        assertTrue(added is ChildEvent.Added)
        assertEquals("a", added.snapshot.key)
        assertNull(added.previousChildName)
    }

    @Test
    fun testQueries() = runTest {
        ensureConnected()
        val reference = database.getReference("test/compatQueries")
        reference.setValue(
            mapOf(
                "a" to mapOf("score" to 1),
                "b" to mapOf("score" to 2),
                "c" to mapOf("score" to 3),
                "d" to mapOf("score" to 4),
            ),
        ).await()
        assertEquals(listOf("b", "c"), reference.orderByChild("score").startAt(2.0).endAt(3.0).get().await().children.map { it.key })
        assertEquals(listOf("c", "d"), reference.orderByChild("score").startAfter(2.0, "b").get().await().children.map { it.key })
        assertEquals(listOf("a"), reference.orderByChild("score").endBefore(2.0, "b").get().await().children.map { it.key })
        assertEquals(listOf("c"), reference.orderByChild("score").equalTo(3.0).get().await().children.map { it.key })
        assertEquals(listOf("a", "b"), reference.orderByKey().limitToFirst(2).get().await().children.map { it.key })
        assertEquals(listOf("d"), reference.orderByKey().limitToLast(1).get().await().children.map { it.key })
        assertEquals(listOf("c", "d"), reference.orderByKey().startAt("c").get().await().children.map { it.key })
        assertEquals(listOf("a", "b"), reference.orderByKey().endAt("b").get().await().children.map { it.key })
        assertEquals(listOf("b"), reference.orderByKey().equalTo("b").get().await().children.map { it.key })
        assertEquals(listOf("a", "b", "c", "d"), reference.orderByValue().get().await().children.map { it.key })
        assertEquals(listOf("a", "b", "c", "d"), reference.orderByPriority().get().await().children.map { it.key })
        assertEquals(reference, reference.orderByKey().limitToFirst(1).ref)
    }

    @Test
    fun testTransaction() = runTest {
        ensureConnected()
        val reference = database.getReference("test/compatTransaction")
        reference.setValue(mapOf("likes" to 2)).await()
        reference.get().await()

        val completed = CompletableDeferred<DataSnapshot?>()
        reference.runTransaction(
            object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val likes = currentData.child("likes")
                    val current = (likes.value as? Number)?.toLong() ?: return Transaction.success(currentData)
                    assertEquals("likes", likes.key)
                    assertTrue(currentData.hasChild("likes"))
                    assertEquals(1, currentData.childrenCount)
                    assertEquals(listOf("likes"), currentData.children.map { it.key })
                    likes.value = current + 1
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) completed.completeExceptionally(error.toException()) else completed.complete(currentData.takeIf { committed })
                }
            },
        )
        assertEquals(3L, (completed.await()?.child("likes")?.value as Number).toLong())

        val aborted = CompletableDeferred<Boolean>()
        reference.runTransaction(
            object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result = Transaction.abort()

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    aborted.complete(committed)
                }
            },
            false,
        )
        assertFalse(aborted.await())
        assertEquals(3L, (reference.get().await().child("likes").value as Number).toLong())
    }

    @Test
    fun testServerValuesAndDisconnect() = runTest {
        ensureConnected()
        val reference = database.getReference("test/compatServerValues")
        reference.child("stamp").setValue(ServerValue.TIMESTAMP).await()
        assertTrue((reference.child("stamp").get().await().value as Number).toLong() > 0)
        reference.child("counter").setValue(2).await()
        reference.child("counter").setValue(ServerValue.increment(5)).await()
        assertEquals(7L, (reference.child("counter").get().await().value as Number).toLong())
        reference.child("counter").setValue(ServerValue.increment(0.5)).await()
        assertEquals(7.5, (reference.child("counter").get().await().value as Number).toDouble())

        val onDisconnect = reference.child("presence").onDisconnect()
        onDisconnect.setValue("offline").await()
        onDisconnect.updateChildren(mapOf("last" to 1)).await()
        onDisconnect.removeValue().await()
        onDisconnect.cancel().await()
        val cancelled = CompletableDeferred<DatabaseError?>()
        onDisconnect.cancel { error, _ -> cancelled.complete(error) }
        assertNull(cancelled.await())
    }

    @Test
    fun testErrors() = runTest {
        ensureConnected()
        assertEquals(-3, DatabaseError.PERMISSION_DENIED)
        assertEquals(-999, DatabaseError.UNKNOWN_ERROR)
        val error = DatabaseError.fromException(IllegalStateException("boom"))
        assertEquals(DatabaseError.USER_CODE_EXCEPTION, error.code)
        assertTrue((error.message + error.details).contains("boom"))
        assertTrue(error.toException() is DatabaseException)
        assertTrue(error.toException().message!!.contains(error.message))

        val invalid = database.getReference("FirebaseRealtimeDatabaseTest/lastActivity")
        val exception = assertFailsWith<DatabaseException> { invalid.setValue("stringNotAllowed").await() }
        assertNotNull(exception.message)
        val failed = CompletableDeferred<DatabaseError?>()
        invalid.setValue("stringNotAllowed") { failedError, _ -> failed.complete(failedError) }
        assertEquals(DatabaseError.PERMISSION_DENIED, failed.await()?.code)
    }

    private suspend fun ensureConnected() = withContext(Dispatchers.Default.limitedParallelism(1)) {
        withTimeout(2.minutes) {
            database.getReference(".info/connected").snapshots.first { it.getValue<Boolean>() == true }
        }
    }
}
