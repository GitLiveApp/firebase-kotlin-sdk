package dev.gitlive.firebase.config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

/** Returns the [FirebaseRemoteConfig] instance of the default [FirebaseApp]. */
actual val Firebase.config: FirebaseRemoteConfig
    get() = TODO("Not yet implemented")

/** Returns the [FirebaseRemoteConfig] instance of the given [FirebaseApp]. */
actual fun Firebase.config(app: FirebaseApp): FirebaseRemoteConfig {
    TODO("Not yet implemented")
}

actual class FirebaseRemoteConfig {
    actual val configSettings: FirebaseRemoteConfigSettings
        get() = TODO("Not yet implemented")

    actual suspend fun fetch() {
    }

    actual suspend fun fetch(expiration: Double) {
    }

    actual suspend fun fetchAndActivate(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun activate(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun ensureInitialized(): FirebaseRemoteConfigInfo {
        TODO("Not yet implemented")
    }

    actual suspend fun setDefaults(defaults: Config) {}

    actual fun getValue(key: String): FirebaseRemoteConfigValue {
        TODO("Not yet implemented")
    }
    actual suspend fun getAll(source: FirebaseRemoteConfigSource): List<FirebaseRemoteConfigValue> =
        TODO("Not yet implemented")

    actual fun getKeysByPrefix(prefix: String?): Set<Any?> {
        TODO("Not yet implemented")
    }

    actual suspend fun setConfigs(config: FirebaseRemoteConfigSettings) {
        TODO("Not yet implemented")
    }
}
