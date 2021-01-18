package dev.gitlive.firebase.config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseRemoteConfig] instance of the default [FirebaseApp]. */
expect val Firebase.config: FirebaseRemoteConfig

/** Returns the [FirebaseRemoteConfig] instance of the given [FirebaseApp]. */
expect fun Firebase.config(app: FirebaseApp): FirebaseRemoteConfig

typealias Config = Map<Any?, Any?>

expect class FirebaseRemoteConfig {
    val configSettings: FirebaseRemoteConfigSettings

    suspend fun fetch()
    suspend fun fetch(expiration: Long)
    suspend fun fetchAndActivate(): Boolean
    suspend fun activate(): Boolean
    suspend fun ensureInitialized(): FirebaseRemoteConfigInfo
    suspend fun setDefaults(defaults: Config)
    suspend fun setConfigs(config: FirebaseRemoteConfigSettings)
    suspend fun getAll(source: FirebaseRemoteConfigSource): List<FirebaseRemoteConfigValue>
    fun getValue(key: String): FirebaseRemoteConfigValue
    fun getKeysByPrefix(prefix: String? = null): Set<Any?>
}

data class FirebaseRemoteConfigSettings(val fetchTimeout: Long? /* seconds */, val minimumFetchInterval: Long? /* seconds */)

enum class FirebaseRemoteConfigSource(val value: Int) {
    DEFAULT(1),
    REMOTE(2),
    STATIC(0);

    companion object {
        fun fromString(value: String): FirebaseRemoteConfigSource = when(value) {
            "static" -> STATIC
            "default" -> DEFAULT
            "remote" -> REMOTE
            else -> throw UnsupportedOperationException(value)
        }
    }
}

data class FirebaseRemoteConfigValue (
    val name: String,
    val source: FirebaseRemoteConfigSource,
    private val asBoolean: () -> Boolean,
    private val asByteArray: () -> ByteArray?,
    private val asDouble: () -> Double?,
    private val asLong: () -> Long?,
    private val asString: () -> String?
) {
    val booleanValue: Boolean
        get() = asBoolean()

    val dataValue: ByteArray?
        get() = asByteArray()

    val doubleValue: Double?
        get() = asDouble()

    val longValue: Long?
        get() = asLong()

    val stringValue: String?
        get() = asString()

}

data class FirebaseRemoteConfigInfo (
    val configSettings: FirebaseRemoteConfigSettings,
    val lastFetchTime: Long,
    val lastFetchStatus: FirebaseRemoteConfigFetchStatus,
)

enum class FirebaseRemoteConfigFetchStatus(val value: Byte) {
    NO_FETCH_YET(0),
    SUCCESS(-1),
    FAILURE(1),
    THROTTLED(2);

    companion object {
        fun fromString(value: String) = when(value) {
            "no-fetch-yet" -> NO_FETCH_YET
            "success" -> SUCCESS
            "failure" -> FAILURE
            "throttle" -> THROTTLED
            else -> throw UnsupportedOperationException(value)
        }
    }
}

expect open class FirebaseRemoteConfigException : FirebaseException
expect class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigFetchThrottledException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException
