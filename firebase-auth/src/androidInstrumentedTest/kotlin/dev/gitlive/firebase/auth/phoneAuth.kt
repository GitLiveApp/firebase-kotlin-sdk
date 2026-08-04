/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Phone auth cannot be covered from commonTest like the rest of the auth suite: [PhoneAuthProvider]
 * requires an [Activity] to attach app verification to, and [PhoneVerificationProvider] exposes
 * different members on every platform, so there is no shared surface to test against.
 *
 * No sms is sent - the auth emulator records the code it would have sent and serves it back over its
 * rest api, which is what [TestPhoneVerificationProvider] reads instead of prompting a user.
 */
class PhoneAuthTest {

    private companion object {
        const val PROJECT_ID = "fir-kotlin-sdk"
        const val AUTH_EMULATOR_PORT = 9099

        /**
         * Deliberately long, so that a regression which only asks for the code once auto retrieval
         * has timed out is clearly distinguishable from one which asks as soon as it is sent.
         */
        const val AUTO_RETRIEVAL_TIMEOUT_SECONDS = 120L
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var scenario: ActivityScenario<Activity>
    private lateinit var activity: Activity

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = PROJECT_ID,
                gcmSenderId = "846484016111",
            ),
        )

        auth = Firebase.auth(app).apply {
            useEmulator(emulatorHost, AUTH_EMULATOR_PORT)
            // there is no play services attestation on a test device, so skip app verification
            android.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }

        scenario = ActivityScenario.launch(Activity::class.java)
        scenario.onActivity { activity = it }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        scenario.close()
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testVerificationCodeIsRequestedAsSoonAsTheCodeIsSent() = runTest {
        val phoneNumber = randomPhoneNumber()
        val verificationProvider = TestPhoneVerificationProvider(phoneNumber)

        val startedAt = TimeSource.Monotonic.markNow()
        val credential = PhoneAuthProvider(auth).verifyPhoneNumber(phoneNumber, verificationProvider)
        val elapsed = startedAt.elapsedNow()

        assertNotNull(credential)
        assertEquals(1, verificationProvider.codesSent)
        // the code used to only be requested from onCodeAutoRetrievalTimeOut, which blocked the
        // caller for the full auto retrieval timeout before the user could enter anything
        assertTrue(
            elapsed < (AUTO_RETRIEVAL_TIMEOUT_SECONDS / 4).seconds,
            "expected the code to be requested as soon as it was sent, but verification took $elapsed",
        )
    }

    @Test
    fun testCodeIsSubmittedAgainstTheVerificationIdOfTheMostRecentResend() = runTest {
        val phoneNumber = randomPhoneNumber()
        val verificationProvider = TestPhoneVerificationProvider(phoneNumber, resendOnFirstCode = true)

        val credential = PhoneAuthProvider(auth).verifyPhoneNumber(phoneNumber, verificationProvider)

        assertEquals(2, verificationProvider.codesSent)
        // resending issues a new verification id and invalidates the previous one, so signing in
        // only succeeds if the code was paired with the newer of the two
        val result = auth.signInWithCredential(credential)
        try {
            assertNotNull(result.user)
        } finally {
            result.user?.delete()
        }
    }

    private fun randomPhoneNumber() = "+1555555${Random.nextInt(1000, 10000)}"

    private inner class TestPhoneVerificationProvider(
        private val phoneNumber: String,
        private val resendOnFirstCode: Boolean = false,
    ) : PhoneVerificationProvider {

        override val activity: Activity get() = this@PhoneAuthTest.activity
        override val timeout: Long = AUTO_RETRIEVAL_TIMEOUT_SECONDS
        override val unit: TimeUnit = TimeUnit.SECONDS

        var codesSent = 0
            private set

        /** completed once no further verification ids are expected */
        private val settled = CompletableDeferred<Unit>()

        override fun codeSent(triggerResend: (Unit) -> Unit) {
            codesSent++
            if (resendOnFirstCode && codesSent == 1) {
                triggerResend(Unit)
            } else {
                settled.complete(Unit)
            }
        }

        override suspend fun getVerificationCode(): String {
            // stand in for the user taking long enough to type that the resend has landed
            settled.await()
            return latestVerificationCode()
        }

        private suspend fun latestVerificationCode(): String = withContext(Dispatchers.IO) {
            val url = "http://$emulatorHost:$AUTH_EMULATOR_PORT/emulator/v1/projects/$PROJECT_ID/verificationCodes"
            repeat(10) {
                val codes = JSONObject(URL(url).readText()).getJSONArray("verificationCodes")
                for (index in codes.length() - 1 downTo 0) {
                    val code = codes.getJSONObject(index)
                    if (code.getString("phoneNumber") == phoneNumber) {
                        return@withContext code.getString("code")
                    }
                }
                delay(500)
            }
            error("the auth emulator recorded no verification code for $phoneNumber")
        }
    }
}
