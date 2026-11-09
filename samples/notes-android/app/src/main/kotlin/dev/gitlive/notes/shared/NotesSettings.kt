// KMP: this file moves to commonMain; only the XML defaults change.
package dev.gitlive.notes.shared

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dev.gitlive.notes.R // KMP: remove; Android resources do not exist in common code
import kotlinx.coroutines.tasks.await

/** Remote Config values, with the defaults from `res/xml/remote_config_defaults.xml`. */
class NotesSettings(private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()) {

    suspend fun load(): NotesSettings {
        remoteConfig.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()).await()
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await() // KMP: setDefaultsAsync(mapOf("max_note_length" to 280L, "backend_greeting" to "Hello from the defaults"))
        remoteConfig.fetchAndActivate().await()
        return this
    }

    val maxNoteLength: Long get() = remoteConfig.getLong("max_note_length")

    val greeting: String get() = remoteConfig.getString("backend_greeting")
}
