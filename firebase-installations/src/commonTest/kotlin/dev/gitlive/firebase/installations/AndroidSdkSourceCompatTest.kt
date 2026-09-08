package dev.gitlive.firebase.installations

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import com.google.firebase.installations.installations
import com.google.firebase.installations.internal.FidListener
import com.google.firebase.installations.internal.FidListenerHandle
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Exercises the `com.google.firebase` layer exactly as Android app code would (Task API, static accessors,
 * listeners), on every platform. Initialisation goes through the dev.gitlive API because the Android SDK's
 * `initializeApp(Context)` cannot be called from common code.
 */
@IgnoreForAndroidUnitTest
@IgnoreForJvm
class AndroidSdkSourceCompatTest {

    @BeforeTest
    fun initializeFirebase() {
        if (dev.gitlive.firebase.Firebase.apps(context).isEmpty()) {
            dev.gitlive.firebase.Firebase.initialize(
                context,
                dev.gitlive.firebase.FirebaseOptions(
                    applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                    apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                    databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                    storageBucket = "fir-kotlin-sdk.appspot.com",
                    projectId = "fir-kotlin-sdk",
                    gcmSenderId = "846484016111",
                ),
            )
        }
    }

    @Test
    fun testFirebaseApp() {
        val app = FirebaseApp.getInstance()
        assertEquals(FirebaseApp.DEFAULT_APP_NAME, app.name)
        assertEquals("fir-kotlin-sdk", app.options.projectId)
        assertEquals(app, Firebase.app)
        assertEquals(app, FirebaseApp.getInstance(FirebaseApp.DEFAULT_APP_NAME))
        assertEquals("fir-kotlin-sdk", app.options.projectId)
    }

    @Test
    fun testGetIdWithTask() = runTest {
        val installations: FirebaseInstallations = FirebaseInstallations.getInstance()
        val task: Task<String> = installations.getId()
        val id = task.await()
        assertTrue(id.isNotBlank())
        assertTrue(task.isComplete)
        assertTrue(task.isSuccessful)
        assertEquals(id, task.result)
        assertEquals(id, Firebase.installations.getId().await())
        assertEquals(id, Firebase.installations(FirebaseApp.getInstance()).getId().await())
    }

    @Test
    fun testGetTokenWithListener() = runTest {
        val source = TaskCompletionSource<InstallationTokenResult>()
        FirebaseInstallations.getInstance().getToken(false)
            .addOnSuccessListener { source.setResult(it) }
            .addOnFailureListener { source.setException(it) }
        val result = source.task.await()
        assertTrue(result.token.isNotBlank())
    }

    @Test
    fun testRegisterFidListener() = runTest {
        val changes = mutableListOf<String>()
        val handle: FidListenerHandle = FirebaseInstallations.getInstance().registerFidListener(FidListener { changes += it })
        assertNotNull(handle)
        handle.unregister()
        handle.unregister() // a no-op after the first call, as on Android
        assertTrue(changes.isEmpty())
    }

    @Test
    fun testTaskCompletionSource() = runTest {
        val source = TaskCompletionSource<Int>()
        var completed: Task<Int>? = null
        source.task.addOnCompleteListener(OnCompleteListener { completed = it })
        source.setResult(42)
        assertEquals(42, source.task.await())
        assertNotNull(completed)
        assertEquals(42, completed?.result)

        val failing = TaskCompletionSource<Int>()
        failing.setException(IllegalStateException("boom"))
        assertFailsWith<IllegalStateException> { failing.task.await() }
        assertEquals("boom", failing.task.exception?.message)

        val chained = source.task.continueWith { it.result + 1 }.onSuccessTask { TaskCompletionSource<String>().apply { setResult("$it") }.task }
        assertEquals("43", chained.await())
    }
}
