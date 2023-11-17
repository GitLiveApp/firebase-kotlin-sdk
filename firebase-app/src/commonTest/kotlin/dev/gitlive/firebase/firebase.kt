package dev.gitlive.firebase

import kotlin.test.Test
import kotlin.test.assertEquals

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

class FirebaseAppTest {
    
    @IgnoreForAndroidUnitTest
    @Test
    fun testInitialize() = runTest {
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

        assertEquals(1, Firebase.apps(context).size)

        Firebase.apps(context).forEach {
            it.delete()
        }
    }

}