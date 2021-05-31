package dev.gitlive.firebase.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.app
import kotlinx.coroutines.tasks.await
import com.google.firebase.ktx.Firebase as AndroidFirebase

actual val Firebase.remoteConfig: RemoteConfig by lazy { RemoteConfig() }

actual fun Firebase.remoteConfig(app: FirebaseApp): RemoteConfig = RemoteConfig(Firebase.app)

actual class RemoteConfig internal constructor(private val app: FirebaseApp? = null) {
    val android: FirebaseRemoteConfig
        get() = app?.let { AndroidFirebase.remoteConfig(app.android) }
            ?: AndroidFirebase.remoteConfig

    actual suspend fun activate(): Boolean = android.activate().await()

    actual suspend fun ensureInitialized(): RemoteConfigInfo {
        val info = android.ensureInitialized().await()
        return info.asCommon()
    }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long?) {
        minimumFetchIntervalInSeconds
            ?.also { android.fetch(it) }
            ?: run { android.fetch() }
                .await()
    }

    actual suspend fun fetchAndActivate(): Boolean = android.fetchAndActivate().await()

    actual fun getAll(): Map<String, RemoteConfigValue> {
        Log.d("RemoteConfig", "${android.all.keys}")
        return android.all.mapValues { RemoteConfigValue(it.value) }
    }

    actual fun getBoolean(key: String): Boolean = android.getBoolean(key)
    actual fun getDouble(key: String): Double = android.getDouble(key)
    actual fun getInfo(): RemoteConfigInfo = android.info.asCommon()
    actual fun getKeysByPrefix(prefix: String): Set<String> = android.getKeysByPrefix(prefix)
    actual fun getLong(key: String): Long = android.getLong(key)
    actual fun getString(key: String): String = android.getString(key)
    actual fun getValue(key: String): RemoteConfigValue = RemoteConfigValue(android.getValue(key))

    actual suspend fun reset() {
        android.reset().await()
    }

    actual suspend fun setConfigSettings(settings: RemoteConfigSettings) {
        val androidSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = settings.minimumFetchIntervalInSeconds
            fetchTimeoutInSeconds = settings.fetchTimeoutInSeconds
        }
        android.setConfigSettingsAsync(androidSettings).await()
    }

    actual suspend fun setDefaults(defaults: Map<String, Any>) {
        android.setDefaultsAsync(defaults).await()
    }

    private fun FirebaseRemoteConfigSettings.asCommon(): RemoteConfigSettings {
        return RemoteConfigSettings(
            fetchTimeoutInSeconds = fetchTimeoutInSeconds,
            minimumFetchIntervalInSeconds = minimumFetchIntervalInSeconds,
        )
    }

    private fun FirebaseRemoteConfigInfo.asCommon(): RemoteConfigInfo {
        val lastFetchStatus = when (lastFetchStatus) {
            FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> LastFetchStatus.Success
            FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> LastFetchStatus.NoFetchYet
            FirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> LastFetchStatus.Failure
            FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> LastFetchStatus.Throttled
            else -> error("Unknown last fetch status value: $lastFetchStatus")
        }

        return RemoteConfigInfo(
            configSettings = configSettings.asCommon(),
            fetchTimeMillis = fetchTimeMillis,
            lastFetchStatus = lastFetchStatus
        )
    }
}
