@file:JvmName("android")
package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import com.google.firebase.ktx.Firebase as AndroidFirebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig as AndroidFirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo as AndroidFirebaseRemoteConfigInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings as AndroidFirebaseRemoteConfigSettings

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfig(AndroidFirebase.remoteConfig)

actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig =
    FirebaseRemoteConfig(AndroidFirebase.remoteConfig)

actual class FirebaseRemoteConfig internal constructor(val android: AndroidFirebaseRemoteConfig) {
    actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = android.all.mapValues { FirebaseRemoteConfigValue(it.value) }

    actual val info: FirebaseRemoteConfigInfo
        get() = android.info.asCommon()

    actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        val androidSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = settings.minimumFetchIntervalInSeconds
            fetchTimeoutInSeconds = settings.fetchTimeoutInSeconds
        }
        android.setConfigSettingsAsync(androidSettings).await()
    }

    actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>) {
        android.setDefaultsAsync(defaults.toMap()).await()
    }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long?) {
        minimumFetchIntervalInSeconds
            ?.also { android.fetch(it).await() }
            ?: run { android.fetch().await() }
    }

    actual suspend fun activate(): Boolean = android.activate().await()
    actual suspend fun ensureInitialized() = android.ensureInitialized().await().let { }
    actual suspend fun fetchAndActivate(): Boolean = android.fetchAndActivate().await()
    actual fun getKeysByPrefix(prefix: String): Set<String> = android.getKeysByPrefix(prefix)
    actual fun getValue(key: String) = FirebaseRemoteConfigValue(android.getValue(key))
    actual suspend fun reset() = android.reset().await().let { }

    private fun AndroidFirebaseRemoteConfigSettings.asCommon(): FirebaseRemoteConfigSettings {
        return FirebaseRemoteConfigSettings(
            fetchTimeoutInSeconds = fetchTimeoutInSeconds,
            minimumFetchIntervalInSeconds = minimumFetchIntervalInSeconds,
        )
    }

    private fun AndroidFirebaseRemoteConfigInfo.asCommon(): FirebaseRemoteConfigInfo {
        val lastFetchStatus = when (lastFetchStatus) {
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> FetchStatus.Success
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> FetchStatus.NoFetchYet
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> FetchStatus.Failure
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> FetchStatus.Throttled
            else -> error("Unknown last fetch status value: $lastFetchStatus")
        }

        return FirebaseRemoteConfigInfo(
            configSettings = configSettings.asCommon(),
            fetchTimeMillis = fetchTimeMillis,
            lastFetchStatus = lastFetchStatus
        )
    }
}

actual typealias FirebaseRemoteConfigException = com.google.firebase.remoteconfig.FirebaseRemoteConfigException
actual typealias FirebaseRemoteConfigClientException = FirebaseRemoteConfigClientException
actual typealias FirebaseRemoteConfigFetchThrottledException = FirebaseRemoteConfigFetchThrottledException
actual typealias FirebaseRemoteConfigServerException = FirebaseRemoteConfigServerException
