package dev.gitlive.firebase

import kotlin.test.Test

expect val context: Any
expect val firebaseOptions: CommonFirebaseOptions
expect fun runTest(test: suspend () -> Unit)

class FirebaseAppTest {
    @Test
    fun testInitialize() {
        Firebase.initialize(
            context,
            firebaseOptions
        )
    }
}