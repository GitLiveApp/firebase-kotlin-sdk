/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend () -> Unit)

private const val PROJECT_ID = "fir-kotlin-sdk"

class FirebaseAuthTest {

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
                        projectId = PROJECT_ID,
                        gcmSenderId = "846484016111"
                    )
                )
                Firebase.auth.useEmulator(emulatorHost, 9099)
            }
    }

    @Test
    fun testSignInWithUsernameAndPassword() = runTest {
        val email = getRandomEmail()
        val uid = getTestUid(email, "test123")
        val result = Firebase.auth.signInWithEmailAndPassword(email, "test123")
        assertEquals(uid, result.user!!.uid)
    }

    @Test
    fun testCreateUserWithEmailAndPassword() = runTest {
        val email = getRandomEmail()
        val createResult = Firebase.auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)
        assertEquals(null, createResult.user?.displayName)
        assertEquals(null, createResult.user?.phoneNumber)
        assertEquals(email, createResult.user?.email)

        val signInResult = Firebase.auth.signInWithEmailAndPassword(email, "test123")
        assertEquals(createResult.user?.uid, signInResult.user?.uid)

        signInResult.user!!.delete()
    }

    @Test
    fun testFetchSignInMethods() = runTest {
        val email = getRandomEmail()
        var signInMethodResult = Firebase.auth.fetchSignInMethodsForEmail(email)
        assertEquals(emptyList(), signInMethodResult)
        Firebase.auth.createUserWithEmailAndPassword(email, "test123")
        signInMethodResult = Firebase.auth.fetchSignInMethodsForEmail(email)
        assertEquals(listOf("password"), signInMethodResult)

        Firebase.auth.signInWithEmailAndPassword(email, "test123").user!!.delete()
    }

    @Test
    fun testSendEmailVerification() = runTest {
        val email = getRandomEmail()
        val createResult = Firebase.auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)
        createResult.user!!.sendEmailVerification()

        val oobCodes = fetchOobCodes(PROJECT_ID)
        assertTrue(oobCodes.any { it.email == email && it.requestType == "VERIFY_EMAIL" })

        createResult.user!!.delete()
    }

    @Test
    fun testSendPasswordResetEmail() = runTest {
        val email = getRandomEmail()
        val createResult = Firebase.auth.createUserWithEmailAndPassword(email, "test123")
        assertNotEquals(null, createResult.user?.uid)

        Firebase.auth.sendPasswordResetEmail(email)

        val oobCodes = fetchOobCodes(PROJECT_ID)
        assertTrue(oobCodes.any { it.email == email && it.requestType == "PASSWORD_RESET" })

        createResult.user!!.delete()
    }

    @Test
    fun testSignInWithCredential() = runTest {
        val email = getRandomEmail()
        val uid = getTestUid(email, "test123")
        val credential = EmailAuthProvider.credential(email, "test123")
        val result = Firebase.auth.signInWithCredential(credential)
        assertEquals(uid, result.user!!.uid)
    }

    @Test
    fun testSendSignInEmailLink() = runTest {
        val email = getRandomEmail()
        sendSgnInLink(email)
        val oobCodes = fetchOobCodes(PROJECT_ID)
        assertTrue(oobCodes.any { it.email == email && it.requestType == "EMAIL_SIGNIN" })
    }

    private suspend fun sendSgnInLink(email: String) {
        val actionCodeSettings = ActionCodeSettings(
            url = "https://example.com/signin",
            canHandleCodeInApp = true,
        )
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
    }

    @Test
    fun testIsSignInWithEmailLink() {
        val validLink = "http://localhost:9099/emulator/action?mode=signIn&lang=en&oobCode=_vr0QcFcxcVeLZbrcU-GpTaZiuxlHquqdC8MSy0YM_vzWCTAQgV9Jq&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        val invalidLink = "http://localhost:9099/emulator/action?mode=signIn&lang=en&&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        assertTrue(Firebase.auth.isSignInWithEmailLink(validLink))
        assertFalse(Firebase.auth.isSignInWithEmailLink(invalidLink))
    }

    @Test
    fun testSignInWithEmailLink() = runTest {
        val email = getRandomEmail()
        sendSgnInLink(email)
        val oobCodes = fetchOobCodes(PROJECT_ID)
        val oobCode = oobCodes.first { it.email == email && it.requestType == "EMAIL_SIGNIN" }
        val authResult = Firebase.auth.signInWithEmailLink(oobCode.email, oobCode.oobLink)
        assertNotNull(authResult.user)
    }

    private suspend fun getTestUid(email: String, password: String): String {
        val uid = Firebase.auth.let {
            val user = try {
                it.createUserWithEmailAndPassword(email, password).user
            } catch (e: FirebaseAuthUserCollisionException) {
                // the user already exists, just sign in for getting user's ID
                it.signInWithEmailAndPassword(email, password).user
            }
            user!!.uid
        }

        check(Firebase.auth.currentUser != null)
        Firebase.auth.signOut()
        check(Firebase.auth.currentUser == null)

        return uid
    }

    private fun getRandomEmail(): String = "test+${Random.nextInt(100000)}@test.com"
}
