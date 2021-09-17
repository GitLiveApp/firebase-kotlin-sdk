package dev.gitlive.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import kotlin.test.Test

expect val context: Any
expect fun runTest(test: suspend () -> Unit)

class FirebaseAppTest {
    @Test
    fun testInitialize() {
        Firebase.initialize(
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
    }

}