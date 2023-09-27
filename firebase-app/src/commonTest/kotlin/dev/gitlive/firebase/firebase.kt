package dev.gitlive.firebase

import kotlin.test.Test
import kotlin.test.assertEquals

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseAppTest {
    @Test
    fun testInitialize() = runTest {
        Firebase.initialize(
            context,
            firebaseOptions
        )

        assertEquals(1, Firebase.apps(context).size)

        Firebase.apps(context).forEach {
            it.delete()
        }
    }

}