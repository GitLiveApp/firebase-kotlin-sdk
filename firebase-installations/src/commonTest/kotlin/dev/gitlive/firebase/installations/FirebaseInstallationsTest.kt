package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

/** firebase-java-sdk 0.6.3 does not support Installations (its Context stub lacks getFilesDir), so the tests are skipped on the JVM. */
expect annotation class IgnoreForJvm()

@IgnoreForAndroidUnitTest
@IgnoreForJvm
class FirebaseInstallationsTest {

    @BeforeTest
    fun initializeFirebase() {
        Firebase.apps(context).ifEmpty {
            Firebase.initialize(
                context,
                FirebaseOptions(
                    applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                    apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                    databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                    storageBucket = "fir-kotlin-sdk.appspot.com",
                    projectId = "fir-kotlin-sdk",
                    gcmSenderId = "846484016111",
                ),
            )
            listOf(Firebase.app)
        }
    }

    @Test
    fun testGetId() = runTest {
        val id = Firebase.installations.getId()
        assertTrue(id.isNotBlank(), "Installation id should not be blank")
        assertEquals(id, Firebase.installations(Firebase.app).getId())
    }

    @Test
    fun testGetToken() = runTest {
        val token = Firebase.installations.getToken(false)
        assertTrue(token.isNotBlank(), "Installation token should not be blank")
    }

    @Test
    fun testDelete() = runTest {
        val id = Firebase.installations.getId()
        // getId() returns before the installation is registered and the JS SDK refuses to delete a pending
        // registration, so wait for a token (which completes the registration) before deleting.
        Firebase.installations.getToken(false)
        Firebase.installations.delete()
        assertTrue(id.isNotBlank())
    }
}
