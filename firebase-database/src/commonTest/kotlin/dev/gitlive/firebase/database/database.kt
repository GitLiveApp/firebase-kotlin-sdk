package dev.gitlive.firebase.database

import dev.gitlive.firebase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes

expect val emulatorHost: String
expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseDatabaseTest {

    lateinit var database: FirebaseDatabase

    @Serializable
    data class FirebaseDatabaseChildTest(val prop1: String? = null, val time: Double = 0.0)

    @Serializable
    data class DatabaseTest(val title: String, val likes: Int = 0)

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

        database = Firebase.database(app).apply {
            useEmulator(emulatorHost, 9000)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testSetValue() = runTest {
        ensureDatabaseConnected()
        val testValue = "test"
        val testReference = database.reference("testPath")

        testReference.setValue(testValue)

        val testReferenceValue = testReference
            .valueEvents
            .first()
            .value<String>()

        assertEquals(testValue, testReferenceValue)
    }

    @Test
    fun testChildCount() = runTest {
        setupRealtimeData()
        val dataSnapshot = database
            .reference("FirebaseRealtimeDatabaseTest")
            .valueEvents
            .first()

        val firebaseDatabaseChildCount = dataSnapshot.children.count()
        assertEquals(3, firebaseDatabaseChildCount)
    }

//    @Test
//    fun testBasicIncrementTransaction() = runTest {
//        val data = DatabaseTest("PostOne", 2)
//        val userRef = Firebase.database.reference("users/user_1/post_id_1")
//        setupDatabase(userRef, data, DatabaseTest.serializer())
//
//        // Check database before transaction
//        val userDocBefore = userRef.valueEvents.first().value(DatabaseTest.serializer())
//        assertEquals(data.title, userDocBefore.title)
//        assertEquals(data.likes, userDocBefore.likes)
//
//        // Run transaction
//        val transactionSnapshot = userRef.runTransaction(DatabaseTest.serializer()) { DatabaseTest(data.title, it.likes + 1) }
//        val userDocAfter = transactionSnapshot.value(DatabaseTest.serializer())
//
//        // Check the database after transaction
//        assertEquals(data.title, userDocAfter.title)
//        assertEquals(data.likes + 1, userDocAfter.likes)
//
//        // cleanUp Firebase
//        cleanUp()
//    }
//
//    @Test
//    fun testBasicDecrementTransaction() = runTest {
//        val data = DatabaseTest("PostTwo", 2)
//        val userRef = database.reference("users/user_1/post_id_2")
//        setupDatabase(userRef, data, DatabaseTest.serializer())
//
//        // Check database before transaction
//        val userDocBefore = userRef.valueEvents.first().value(DatabaseTest.serializer())
//        assertEquals(data.title, userDocBefore.title)
//        assertEquals(data.likes, userDocBefore.likes)
//
//        // Run transaction
//        val transactionSnapshot = userRef.runTransaction(DatabaseTest.serializer()) { DatabaseTest(data.title, it.likes - 1) }
//        val userDocAfter = transactionSnapshot.value(DatabaseTest.serializer())
//
//        // Check the database after transaction
//        assertEquals(data.title, userDocAfter.title)
//        assertEquals(data.likes - 1, userDocAfter.likes)
//
//        // cleanUp Firebase
//        cleanUp()
//    }

    @Test
    fun testSetServerTimestamp() = runTest {
        ensureDatabaseConnected()
        val testReference = database.reference("testSetServerTimestamp")

        testReference.setValue(ServerValue.TIMESTAMP)

        val timestamp = testReference
            .valueEvents
            .first()
            .value<Long>()

        assertTrue(timestamp > 0)
    }

    @Test
    fun testIncrement() = runTest {
        ensureDatabaseConnected()
        val testReference = database.reference("testIncrement")

        testReference.setValue(2.0)

        val value = testReference
            .valueEvents
            .first()
            .value<Double>()

        assertEquals(2.0, value)

        testReference.setValue(ServerValue.increment(5.0))
        val updatedValue = testReference
            .valueEvents
            .first()
            .value<Double>()

        assertEquals(7.0, updatedValue)
    }

    private suspend fun setupRealtimeData() {
        ensureDatabaseConnected()
        val firebaseDatabaseTestReference = database
            .reference("FirebaseRealtimeDatabaseTest")

        val firebaseDatabaseChildTest1 = FirebaseDatabaseChildTest("aaa")
        val firebaseDatabaseChildTest2 = FirebaseDatabaseChildTest("bbb")
        val firebaseDatabaseChildTest3 = FirebaseDatabaseChildTest("ccc")

        firebaseDatabaseTestReference.child("1").setValue(firebaseDatabaseChildTest1)
        firebaseDatabaseTestReference.child("2").setValue(firebaseDatabaseChildTest2)
        firebaseDatabaseTestReference.child("3").setValue(firebaseDatabaseChildTest3)
    }

    private suspend fun <T> setupDatabase(ref: DatabaseReference, data: T, strategy: SerializationStrategy<T>) {
        try {
            ref.setValue(strategy, data)
        } catch (err: DatabaseException) {
            println(err)
            throw err
        }
    }

    private suspend fun ensureDatabaseConnected() = withContext(Dispatchers.Default.limitedParallelism(1)) {
        withTimeout(2.minutes) {
            database.reference(".info/connected").valueEvents.first { it.value() }
        }
    }
}
