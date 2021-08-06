package dev.gitlive.firebase.database

import dev.gitlive.firebase.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.*
import kotlin.test.*

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseDatabaseTest {

    @Serializable
    data class DatabaseTest(val prop: String, val likes: Int = 0)

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
                        databaseUrl = "http://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
                Firebase.database.useEmulator(emulatorHost, 9000)
            }
    }

    @AfterTest
    fun tearDown() {
        Firebase
            .takeIf { Firebase.apps(context).isNotEmpty() }
            ?.apply { app.delete() }
    }

    @Test
    fun testBasicIncrementTransaction() = runTest {
        val data = DatabaseTest("post1", 2)
        val userRef = Firebase.database.reference("users/user_1/post_id_1")
        setupDatabase(userRef, data, DatabaseTest.serializer())

        // Check database before transaction
        val userDocBefore = userRef.valueEvents.first().value(DatabaseTest.serializer())
        assertEquals(data.prop, userDocBefore.prop)
        assertEquals(data.likes, userDocBefore.likes)

        // Run transaction
        userRef.runTransaction(DatabaseTest.serializer()) { DatabaseTest(data.prop, it.likes + 1) }

        // Check the database after transaction
        val userDocAfter = userRef.valueEvents.first().value(DatabaseTest.serializer())
        assertEquals(data.prop, userDocAfter.prop)
        assertEquals(data.likes, userDocAfter.likes + 1)
    }

    @Test
    fun testBasicDecrementTransaction() = runTest {
        val data = DatabaseTest("post2", 2)
        val userRef = Firebase.database.reference("users/user_1/post_id_2")
        setupDatabase(userRef, data, DatabaseTest.serializer())

        // Check database before transaction
        val userDocBefore = userRef.valueEvents.first().value(DatabaseTest.serializer())
        assertEquals(data.prop, userDocBefore.prop)
        assertEquals(data.likes, userDocBefore.likes)

        // Run transaction
        userRef.runTransaction(DatabaseTest.serializer()) { DatabaseTest(data.prop, it.likes - 1) }

        // Check the database after transaction
        val userDocAfter = userRef.valueEvents.first().value(DatabaseTest.serializer())
        assertEquals(data.prop, userDocAfter.prop)
        assertEquals(data.likes, userDocAfter.likes - 1)
    }

    private suspend fun <T> setupDatabase(ref: DatabaseReference, data: T, strategy: SerializationStrategy<T>) {
        try {
            ref.setValue(strategy, data)
        } catch (err: DatabaseException) {
            println(err)
        }
    }

}