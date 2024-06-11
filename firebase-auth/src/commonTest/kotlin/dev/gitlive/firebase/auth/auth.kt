/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlin.random.Random
import kotlin.test.*

expect val emulatorHost: String
expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseAuthTest {

    lateinit var auth: FirebaseAuth

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
                gcmSenderId = "846484016111",
            ),
        )

        auth = Firebase.auth(app).apply {
            useEmulator(emulatorHost, 9099)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testSignInWithUsernameAndPassword() = runTest {
        val uid = getTestUid("test@test.com", "test123")
        val result = auth.signInWithEmailAndPassword("test@test.com", "test123")
        assertEquals(uid, result.user!!.uid)
    }

    @Test
    fun testCreateUserWithEmailAndPassword() = runTest {
        val email = "test+${Random.nextInt(100000)}@test.com"
        val createResult = auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)
        assertEquals(null, createResult.user?.displayName)
        assertEquals(null, createResult.user?.phoneNumber)
        assertEquals(email, createResult.user?.email)

        val signInResult = auth.signInWithEmailAndPassword(email, "test123")
        assertEquals(createResult.user?.uid, signInResult.user?.uid)

        signInResult.user!!.delete()
    }

    @Test
    fun testFetchSignInMethods() = runTest {
        val email = "test+${Random.nextInt(100000)}@test.com"
        var signInMethodResult = auth.fetchSignInMethodsForEmail(email)
        assertEquals(emptyList(), signInMethodResult)
        auth.createUserWithEmailAndPassword(email, "test123")
        signInMethodResult = auth.fetchSignInMethodsForEmail(email)
        assertEquals(listOf("password"), signInMethodResult)

        auth.signInWithEmailAndPassword(email, "test123").user!!.delete()
    }

    @Test
    fun testSendEmailVerification() = runTest {
        val email = "test+${Random.nextInt(100000)}@test.com"
        val createResult = auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)
        createResult.user!!.sendEmailVerification()

        createResult.user!!.delete()
    }

    @Test
    fun sendPasswordResetEmail() = runTest {
        val email = "test+${Random.nextInt(100000)}@test.com"
        val createResult = auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)

        auth.sendPasswordResetEmail(email)

        createResult.user!!.delete()
    }

    @Test
    fun testSignInWithCredential() = runTest {
        val uid = getTestUid("test@test.com", "test123")
        val credential = EmailAuthProvider.credential("test@test.com", "test123")
        val result = auth.signInWithCredential(credential)
        assertEquals(uid, result.user!!.uid)
    }

    @Test
    fun testIsSignInWithEmailLink() {
        val validLink = "http://localhost:9099/emulator/action?mode=signIn&lang=en&oobCode=_vr0QcFcxcVeLZbrcU-GpTaZiuxlHquqdC8MSy0YM_vzWCTAQgV9Jq&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        val invalidLink = "http://localhost:9099/emulator/action?mode=signIn&lang=en&&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        assertTrue(auth.isSignInWithEmailLink(validLink))
        assertFalse(auth.isSignInWithEmailLink(invalidLink))
    }

    @Test
    fun testCredentialWithLink() {
        val link = "http://localhost:9099/emulator/action?mode=signIn&lang=en&oobCode=_vr0QcFcxcVeLZbrcU-GpTaZiuxlHquqdC8MSy0YM_vzWCTAQgV9Jq&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        val email = "test@test.com"
        val credential = EmailAuthProvider.credentialWithLink(email, link)
        assertEquals("password", credential.providerId)
    }

    private suspend fun getTestUid(email: String, password: String): String {
        val uid = auth.let {
            val user = try {
                it.createUserWithEmailAndPassword(email, password).user
            } catch (e: FirebaseAuthUserCollisionException) {
                // the user already exists, just sign in for getting user's ID
                it.signInWithEmailAndPassword(email, password).user
            }
            user!!.uid
        }

        check(auth.currentUser != null)
        auth.signOut()
        check(auth.currentUser == null)

        return uid
    }
}
