package dev.gitlive.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import kotlin.test.Test

expect val context: Any
expect val firebaseOptions: FirebaseOptions
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