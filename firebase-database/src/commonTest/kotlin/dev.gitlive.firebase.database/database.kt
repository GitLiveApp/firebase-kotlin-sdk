/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.*
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseDatabaseTest {

    @Serializable
    data class FirebaseDatabaseChildTest(val prop1: String? = null, val time: Double = 0.0)

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
                Firebase.database.useEmulator(emulatorHost, 9000)
            }
    }

    @Test
    fun testSetValue() = runTest {
        awaitDatabaseConnection()

        val testValue = "test"
        val testReference = Firebase.database.reference("testPath")

        testReference.setValue(testValue)

        val testReferenceValue = testReference
            .valueEvents
            .first()
            .value<String>()

        assertEquals(testValue, testReferenceValue)
    }

    @Test
    fun testChildCount() = runTest {
        awaitDatabaseConnection()

        setupRealtimeData()
        val dataSnapshot = Firebase.database
            .reference("FirebaseRealtimeDatabaseTest")
            .valueEvents
            .first()

        val firebaseDatabaseChildCount = dataSnapshot.children.count()
        assertEquals(3, firebaseDatabaseChildCount)
    }

    private suspend fun awaitDatabaseConnection() {
        // workaround to avoid "Database not connected" exception with Firebase emulator
        withTimeout(30.seconds) {
            Firebase.database.reference(".info/connected").valueEvents.first { it.value() }
        }
    }

    private suspend fun setupRealtimeData() {
        val firebaseDatabaseTestReference = Firebase.database
            .reference("FirebaseRealtimeDatabaseTest")

        val firebaseDatabaseChildTest1 = FirebaseDatabaseChildTest("aaa")
        val firebaseDatabaseChildTest2 = FirebaseDatabaseChildTest("bbb")
        val firebaseDatabaseChildTest3 = FirebaseDatabaseChildTest("ccc")

        firebaseDatabaseTestReference.child("1").setValue(firebaseDatabaseChildTest1)
        firebaseDatabaseTestReference.child("2").setValue(firebaseDatabaseChildTest2)
        firebaseDatabaseTestReference.child("3").setValue(firebaseDatabaseChildTest3)
    }
}
