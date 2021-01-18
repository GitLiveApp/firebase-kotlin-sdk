@file:JvmName("AndroidConfig")

package dev.gitlive.firebase.config

import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await

actual val Firebase.config by lazy {
    FirebaseRemoteConfig(com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance())
}

actual fun Firebase.config(app: FirebaseApp) =
    FirebaseRemoteConfig(
        com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance(app.android)
    )

actual class FirebaseRemoteConfig internal constructor (
    private val android: com.google.firebase.remoteconfig.FirebaseRemoteConfig
) {
    actual val configSettings: FirebaseRemoteConfigSettings
        get() = android.info.configSettings.run {
            FirebaseRemoteConfigSettings(
                fetchTimeoutInSeconds,
                minimumFetchIntervalInSeconds
            )
        }

    actual suspend fun fetch() {
        android.fetch().await()
    }

    actual suspend  fun fetch(expiration: Long) {
        android.fetch(expiration).await()
    }

    actual suspend  fun fetchAndActivate(): Boolean =
        android.fetchAndActivate().await()

    actual suspend  fun activate(): Boolean =
        android.activate().await()

    actual suspend  fun ensureInitialized(): FirebaseRemoteConfigInfo =
        android.ensureInitialized().await().run {
            FirebaseRemoteConfigInfo(
                this@FirebaseRemoteConfig.configSettings,
                fetchTimeMillis,
                FirebaseRemoteConfigFetchStatus.values()
                    .first { it.value == lastFetchStatus.toByte() }
            )
        }

    actual suspend  fun setDefaults(defaults: Config) {
        android.setDefaultsAsync(defaults as Map<String, Any>).await()
    }

    actual fun getValue(key: String): FirebaseRemoteConfigValue =
        android.getValue(key).run {
            FirebaseRemoteConfigValue(
                key,
                FirebaseRemoteConfigSource.values().associateBy(FirebaseRemoteConfigSource::value)
                    .getValue(source),
                ::asBoolean,
                ::asByteArray,
                ::asDouble,
                ::asLong,
                ::asString
            )
        }

    actual suspend fun getAll(source: FirebaseRemoteConfigSource): List<FirebaseRemoteConfigValue> =
        android.all.filter { it.value.source == source.value }
            .map { getValue(it.key) }

    actual fun getKeysByPrefix(prefix: String?): Set<Any?> =
        android.getKeysByPrefix(prefix ?: "")

    actual suspend fun setConfigs(config: FirebaseRemoteConfigSettings) {
        android.setConfigSettingsAsync(
            remoteConfigSettings {
                fetchTimeoutInSeconds = config.fetchTimeout ?: fetchTimeoutInSeconds
                minimumFetchIntervalInSeconds = config.minimumFetchInterval
                    ?: minimumFetchIntervalInSeconds
            }
        ).await()
    }

}
