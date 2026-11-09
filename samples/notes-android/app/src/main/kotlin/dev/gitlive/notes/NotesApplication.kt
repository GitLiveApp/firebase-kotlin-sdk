// The Android app; stays in the app module after the port.
package dev.gitlive.notes

import android.app.Application
import dev.gitlive.notes.shared.NotesFirebase
import dev.gitlive.notes.shared.NotesMessaging

class NotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // KMP: unchanged; the iOS, JS and wasmJs apps call NotesFirebase.ensureInitialized(Context()) from their entry points.
        NotesFirebase.ensureInitialized(this)
        NotesMessaging().enable()
    }
}
