package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

expect val Firebase.remoteConfig: RemoteConfig

expect fun Firebase.remoteConfig(app: FirebaseApp): RemoteConfig

expect class RemoteConfig {
    suspend fun activate(): Boolean
    suspend fun ensureInitialized(): RemoteConfigInfo
    suspend fun fetch(minimumFetchIntervalInSeconds: Long? = null)
    suspend fun fetchAndActivate(): Boolean
    fun getAll(): Map<String, RemoteConfigValue>
    fun getBoolean(key: String): Boolean
    fun getDouble(key: String): Double
    fun getInfo(): RemoteConfigInfo
    fun getKeysByPrefix(prefix: String): Set<String>
    fun getLong(key: String): Long
    fun getString(key: String): String
    fun getValue(key: String): RemoteConfigValue
    suspend fun reset()
    suspend fun setConfigSettings(settings: RemoteConfigSettings)
    suspend fun setDefaults(defaults: Map<String, Any>)
}
