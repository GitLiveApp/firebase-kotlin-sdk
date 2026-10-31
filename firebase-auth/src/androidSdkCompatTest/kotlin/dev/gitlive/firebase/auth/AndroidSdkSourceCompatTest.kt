/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeResult
import com.google.firebase.auth.ActionCodeUrl
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.auth.oAuthCredential
import com.google.firebase.auth.oAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.app
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Exercises the `com.google.firebase.auth` layer exactly as Android app code would (the static accessors, the Task-based
 * sign-in API, users and their tokens, credentials and providers, the builders and their Kotlin extensions, listeners,
 * action codes and the error codes), on every platform, against the Authentication emulator.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    private lateinit var auth: FirebaseAuth

    // Shares the app with FirebaseAuthTest (see there for why it is never deleted); the emulator is applied on creation.
    @BeforeTest
    fun initializeFirebase() {
        val app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        ).also {
            Firebase.auth(it.compat).useEmulator(emulatorHost, 9099)
        }
        auth = Firebase.auth(app.compat)
    }

    @AfterTest
    fun signOut() = runBlockingTest {
        dev.gitlive.firebase.Firebase.auth.signOut()
    }

    private fun randomEmail() = "compat+${Random.nextInt(100000)}@test.com"

    private suspend fun <T> awaitEvent(deferred: CompletableDeferred<T>): T = withContext(Dispatchers.Default) { withTimeout(1.minutes) { deferred.await() } }

    @Test
    fun testInstancesAndSettings() {
        assertEquals(auth, FirebaseAuth.getInstance(auth.app))
        assertEquals(auth, Firebase.auth)
        assertEquals(auth, dev.gitlive.firebase.Firebase.auth.compat)
        assertEquals(dev.gitlive.firebase.Firebase.app.compat, auth.app)
        assertNotNull(auth.firebaseAuthSettings)
        auth.setLanguageCode("fr")
        assertEquals("fr", auth.languageCode)
        auth.useAppLanguage()
        assertNull(auth.tenantId)
    }

    @Test
    fun testCreateSignInAndDelete() = runTest {
        val email = randomEmail()
        val created = auth.createUserWithEmailAndPassword(email, "test123").await()
        val user: FirebaseUser = assertNotNull(created.user)
        try {
            assertEquals(email, user.email)
            assertFalse(user.isAnonymous)
            assertFalse(user.isEmailVerified)
            assertNull(user.displayName)
            assertEquals("firebase", user.providerId)
            assertEquals(true, created.additionalUserInfo?.isNewUser)
            assertEquals(user, auth.currentUser)
            assertTrue(user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID && it.email == email })
            assertNotNull(user.metadata)

            auth.signOut()
            assertNull(auth.currentUser)

            val signedIn = auth.signInWithEmailAndPassword(email, "test123").await()
            assertEquals(user.uid, signedIn.user?.uid)
            assertEquals(false, signedIn.additionalUserInfo?.isNewUser)
            val token: GetTokenResult = assertNotNull(signedIn.user).getIdToken(false).await()
            assertNotNull(token.token)
            assertEquals("password", token.signInProvider)
            assertEquals(email, token.claims["email"])
            assertTrue(token.expirationTimestamp > 0)
            assertEquals(dev.gitlive.firebase.Firebase.auth.currentUser?.compat, auth.currentUser)
        } finally {
            (auth.currentUser ?: user).delete().await()
        }
        assertNull(auth.currentUser)
    }

    @Test
    fun testListeners() = runTest {
        val signedIn = CompletableDeferred<FirebaseUser>()
        val listener = FirebaseAuth.AuthStateListener { auth -> auth.currentUser?.let { signedIn.complete(it) } }
        auth.addAuthStateListener(listener)
        val tokenChanged = CompletableDeferred<Unit>()
        val idTokenListener = FirebaseAuth.IdTokenListener { auth -> if (auth.currentUser != null) tokenChanged.complete(Unit) }
        auth.addIdTokenListener(idTokenListener)
        try {
            val result = auth.signInAnonymously().await()
            assertTrue(assertNotNull(result.user).isAnonymous)
            assertEquals(result.user, awaitEvent(signedIn))
            awaitEvent(tokenChanged)
        } finally {
            auth.removeAuthStateListener(listener)
            auth.removeIdTokenListener(idTokenListener)
            auth.currentUser?.delete()?.await()
        }
    }

    @Test
    fun testCredentialsAndProviders() = runTest {
        val email = randomEmail()
        val credential: AuthCredential = EmailAuthProvider.getCredential(email, "test123")
        assertEquals(EmailAuthProvider.PROVIDER_ID, credential.provider)
        assertEquals(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD, credential.signInMethod)
        assertEquals("password", EmailAuthProvider.PROVIDER_ID)
        assertEquals(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD, EmailAuthProvider.getCredentialWithLink(email, VALID_LINK).signInMethod)
        assertEquals("google.com", GoogleAuthProvider.PROVIDER_ID)
        assertEquals("google.com", GoogleAuthProvider.getCredential("id-token", "access-token").provider)
        assertEquals("facebook.com", FacebookAuthProvider.getCredential("token").provider)
        assertEquals("twitter.com", TwitterAuthProvider.getCredential("token", "secret").signInMethod)
        assertEquals("phone", PhoneAuthProvider.PROVIDER_ID)

        val oAuth: AuthCredential = oAuthCredential("microsoft.com") {
            setIdToken("id-token")
            setAccessToken("access-token")
        }
        assertEquals("microsoft.com", oAuth.provider)
        assertIs<OAuthCredential>(oAuth)
        assertEquals("id-token", oAuth.idToken)
        assertEquals("access-token", oAuth.accessToken)
        assertNull(oAuth.secret)
        val provider = oAuthProvider("microsoft.com", auth) {
            setScopes(listOf("mail.read"))
            addCustomParameter("tenant", "common")
        }
        assertEquals("microsoft.com", provider.providerId)

        auth.createUserWithEmailAndPassword(email, "test123").await()
        auth.signOut()
        val result = auth.signInWithCredential(credential).await()
        try {
            assertEquals(email, result.user?.email)
        } finally {
            result.user?.delete()?.await()
        }
    }

    @Test
    fun testErrors() = runTest {
        val email = randomEmail()
        val weak = assertFailsWith<FirebaseAuthWeakPasswordException> { auth.createUserWithEmailAndPassword(email, "1").await() }
        assertEquals("ERROR_WEAK_PASSWORD", weak.errorCode)

        val user = assertNotNull(auth.createUserWithEmailAndPassword(email, "test123").await().user)
        try {
            val collision = assertFailsWith<FirebaseAuthUserCollisionException> { auth.createUserWithEmailAndPassword(email, "test123").await() }
            assertEquals("ERROR_EMAIL_ALREADY_IN_USE", collision.errorCode)
            auth.signOut()
            val invalid = assertFailsWith<FirebaseAuthInvalidCredentialsException> { auth.signInWithEmailAndPassword(email, "wrong").await() }
            assertTrue(invalid.errorCode.startsWith("ERROR_"), invalid.errorCode)
            val actionCode = assertFailsWith<FirebaseAuthActionCodeException> { auth.checkActionCode("not-a-code").await() }
            assertEquals("ERROR_INVALID_ACTION_CODE", actionCode.errorCode)
            assertFailsWith<FirebaseAuthActionCodeException> { auth.applyActionCode("not-a-code").await() }
        } finally {
            auth.signInWithEmailAndPassword(email, "test123").await()
            user.delete().await()
        }
    }

    @Test
    fun testProfileAndActionCodes() = runTest {
        val email = randomEmail()
        val user = assertNotNull(auth.createUserWithEmailAndPassword(email, "test123").await().user)
        try {
            user.updateProfile(userProfileChangeRequest { setDisplayName("Compat") }).await()
            user.reload().await()
            assertEquals("Compat", user.displayName)
            assertEquals("Compat", auth.currentUser?.displayName)
            user.sendEmailVerification().await()

            val settings = actionCodeSettings {
                setUrl("https://example.com/finish")
                setHandleCodeInApp(true)
                setIOSBundleId("dev.gitlive.compat")
                setAndroidPackageName("dev.gitlive.compat", true, "21")
            }
            assertEquals("https://example.com/finish", settings.url)
            assertTrue(settings.canHandleCodeInApp())
            assertEquals("dev.gitlive.compat", settings.iosBundle)
            assertEquals("dev.gitlive.compat", settings.androidPackageName)
            assertTrue(settings.androidInstallApp)
            assertEquals("21", settings.androidMinimumVersion)
            auth.sendPasswordResetEmail(email, settings).await()
            auth.sendPasswordResetEmail(email).await()
            auth.sendSignInLinkToEmail(email, settings).await()

            assertTrue(auth.isSignInWithEmailLink(VALID_LINK))
            assertFalse(auth.isSignInWithEmailLink(INVALID_LINK))
            val url = assertNotNull(ActionCodeUrl.parseLink(VALID_LINK))
            assertEquals("fake-api-key", url.apiKey)
            assertEquals("_vr0QcFcxcVeLZbrcU-GpTaZiuxlHquqdC8MSy0YM_vzWCTAQgV9Jq", url.code)
            assertEquals("https://example.com/signin", url.continueUrl)
            assertEquals(ActionCodeResult.SIGN_IN_WITH_EMAIL_LINK, url.operation)
            assertNull(ActionCodeUrl.parseLink("https://example.com/not-an-action"))
        } finally {
            user.delete().await()
        }
    }

    private companion object {
        const val VALID_LINK = "http://localhost:9099/emulator/action?mode=signIn&lang=en&oobCode=_vr0QcFcxcVeLZbrcU-GpTaZiuxlHquqdC8MSy0YM_vzWCTAQgV9Jq&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
        const val INVALID_LINK = "http://localhost:9099/emulator/action?mode=signIn&lang=en&&apiKey=fake-api-key&continueUrl=https%3A%2F%2Fexample.com%2Fsignin"
    }
}
