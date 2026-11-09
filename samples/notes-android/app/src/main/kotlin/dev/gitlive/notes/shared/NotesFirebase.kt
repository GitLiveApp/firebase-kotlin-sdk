// KMP: this file moves to shared/src/commonMain/kotlin/dev/gitlive/notes/shared/ unchanged.
package dev.gitlive.notes.shared

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

/**
 * Configures Firebase in code rather than through the Google Services plugin, so the sample needs no `google-services.json`.
 *
 * Every member used here keeps its Android SDK signature in common code: `android.content.Context` is provided on every
 * platform (the platforms without one create the placeholder `Context()`), and so are `FirebaseApp`, `FirebaseOptions`
 * and the `useEmulator` functions.
 */
object NotesFirebase {

    /** The host the Firebase emulators are reached on from the Android emulator; null to use the real project. */
    val EMULATOR_HOST: String? = "10.0.2.2"

    fun ensureInitialized(context: Context): FirebaseApp =
        FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setApiKey("AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0")
                .setApplicationId("1:846484016111:android:4c0dd0b6c0d4e1b8")
                .setProjectId("fir-kotlin-sdk")
                .setStorageBucket("fir-kotlin-sdk.appspot.com")
                .build(),
        ).also { if (EMULATOR_HOST != null) useEmulators(EMULATOR_HOST) }

    private fun useEmulators(host: String) {
        FirebaseAuth.getInstance().useEmulator(host, 9099)
        FirebaseFirestore.getInstance().useEmulator(host, 8080)
        FirebaseStorage.getInstance().useEmulator(host, 9199)
        FirebaseFunctions.getInstance().useEmulator(host, 5001)
    }
}
