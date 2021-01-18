package dev.gitlive.firebase.config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.firebase
import dev.gitlive.firebase.remoteConfig
import kotlinx.coroutines.await
import kotlin.js.json

/** Returns the [FirebaseRemoteConfig] instance of the default [FirebaseApp]. */
actual val Firebase.config: FirebaseRemoteConfig
    get() = rethrow { remoteConfig; FirebaseRemoteConfig(firebase.remoteConfig()) }

/** Returns the [FirebaseRemoteConfig] instance of the given [FirebaseApp]. */
actual fun Firebase.config(app: FirebaseApp): FirebaseRemoteConfig = rethrow {
    remoteConfig; FirebaseRemoteConfig(firebase.remoteConfig(app.js))
}

actual class FirebaseRemoteConfig(internal val js: firebase.remoteConfig.RemoteConfig) {
    actual val configSettings: FirebaseRemoteConfigSettings
        get() = FirebaseRemoteConfigSettings(
            js.settings["minimumFetchIntervalMillis"].toString().toLong(),
            js.settings["fetchTimeoutMillis"].toString().toLong()
        )

    actual suspend fun fetch() {
        rethrow { js.fetch() }
    }

    actual suspend fun fetch(expiration: Long) {
        val fetchInterval = js.settings["minimumFetchIntervalMillis"]
        js.settings["minimumFetchIntervalMillis"] = expiration
        rethrow { js.fetch().await() }.run {
            js.settings["minimumFetchIntervalMillis"] = fetchInterval
        }
    }

    actual suspend fun fetchAndActivate(): Boolean =
        rethrow { js.fetchAndActivate().await() }

    actual suspend fun activate(): Boolean =
       rethrow { js.activate().await() }

    actual suspend fun ensureInitialized(): FirebaseRemoteConfigInfo =
        rethrow {
            js.ensureInitialized().await().run {
                FirebaseRemoteConfigInfo(
                    FirebaseRemoteConfigSettings(
                        js.settings["fetchTimeoutMillis"].run { toString().toLong() / 1000 },
                        js.settings["minimumFetchIntervalMillis"].run { toString().toLong() / 1000 }
                    ),
                    js.fetchTimeMillis,
                    FirebaseRemoteConfigFetchStatus.fromString(js.lastFetchStatus),
                )
            }
        }

    actual suspend fun setDefaults(defaults: Config) {
        val defaultsAsPairs = defaults.map { it.key.toString() to it.value }
        rethrow {
            js.defaultConfig = json(*defaultsAsPairs.toTypedArray())
        }
    }

    actual fun getValue(key: String): FirebaseRemoteConfigValue =
        rethrow { js.getValue(key) }.run {
            FirebaseRemoteConfigValue(
                key,
                FirebaseRemoteConfigSource.fromString(getSource()),
                ::asBoolean,
                { asString().encodeToByteArray() },
                { asNumber().toDouble() },
                { asNumber().toLong() },
                ::asString
            )
        }

    actual suspend fun getAll(source: FirebaseRemoteConfigSource): List<FirebaseRemoteConfigValue> =
        rethrow {
            val values = JSON.parse<Array<Pair<String, firebase.remoteConfig.Value>>>(js.getAll().toString())
            values.map {
                FirebaseRemoteConfigValue(
                    it.first,
                    FirebaseRemoteConfigSource.fromString(it.second.getSource()),
                    it.second::asBoolean,
                    { it.second.asString().encodeToByteArray() },
                    { it.second.asNumber().toDouble() },
                    { it.second.asNumber().toLong() },
                    it.second::asString
                )
            }
        }


    actual fun getKeysByPrefix(prefix: String?): Set<Any?> {
        TODO("No method found in js sdk.")
    }

    actual suspend fun setConfigs(config: FirebaseRemoteConfigSettings) {
        setFetchTimeoutMillis(
            config.fetchTimeout?.times(1000)
                ?: js.settings["fetchTimeoutMillis"].toString().toLong()
        )
        setMinimumFetchIntervalMillis(
            config.minimumFetchInterval?.times(1000)
                ?: js.settings["minimumFetchIntervalMillis"].toString().toLong()
        )
    }

    private fun setFetchTimeoutMillis(value: Long) {
        js.settings["fetchTimeoutMillis"] = value
    }

    private fun setMinimumFetchIntervalMillis(value: Long) {
        js.settings["minimumFetchIntervalMillis"] = value
    }
}
