package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

expect val Firebase.remoteConfig: FirebaseRemoteConfig

expect fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig

expect class FirebaseRemoteConfig {
    suspend fun activate(): Boolean
    suspend fun ensureInitialized()
    suspend fun fetch(minimumFetchIntervalInSeconds: Long? = null)
    suspend fun fetchAndActivate(): Boolean
    fun getAll(): Map<String, FirebaseRemoteConfigValue>
    fun getBoolean(key: String): Boolean
    fun getDouble(key: String): Double
    fun getInfo(): FirebaseRemoteConfigInfo
    fun getKeysByPrefix(prefix: String): Set<String>
    fun getLong(key: String): Long
    fun getString(key: String): String
    fun getValue(key: String): FirebaseRemoteConfigValue
    suspend fun reset()
    suspend fun setConfigSettings(settings: FirebaseRemoteConfigSettings)
    suspend fun setDefaults(defaults: Map<String, Any?>)
}

expect open class FirebaseRemoteConfigException : FirebaseException
expect class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigFetchThrottledException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException
