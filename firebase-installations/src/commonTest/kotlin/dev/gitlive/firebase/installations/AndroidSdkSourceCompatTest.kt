package dev.gitlive.firebase.installations

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.app
import com.google.firebase.initialize
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import com.google.firebase.installations.installations
import dev.gitlive.firebase.runTest
import dev.gitlive.firebase.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Exercises the `com.google.firebase` layer exactly as Android app code would (Task API, static accessors,
 * listeners), on every platform.
 */
@IgnoreForAndroidUnitTest
@IgnoreForJvm
class AndroidSdkSourceCompatTest {

    @BeforeTest
    fun initializeFirebase() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Firebase.initialize(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:846484016111:ios:dd1f6688bad7af768c841a")
                    .setApiKey("AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0")
                    .setDatabaseUrl("https://fir-kotlin-sdk.firebaseio.com")
                    .setStorageBucket("fir-kotlin-sdk.appspot.com")
                    .setProjectId("fir-kotlin-sdk")
                    .setGcmSenderId("846484016111")
                    .build(),
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
        assertTrue(FirebaseApp.getApps(context).contains(app))
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
