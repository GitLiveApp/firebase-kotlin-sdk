package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.externals.remoteconfig.*
import kotlinx.coroutines.await
import kotlin.js.json

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = rethrow { FirebaseRemoteConfig(getRemoteConfig()) }

actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = rethrow {
    FirebaseRemoteConfig(getRemoteConfig(app.js))
}

actual class FirebaseRemoteConfig internal constructor(val js: RemoteConfig) {
    actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = rethrow { getAllKeys().map { Pair(it, getValue(it)) }.toMap() }

    actual val info: FirebaseRemoteConfigInfo
        get() = rethrow {
            FirebaseRemoteConfigInfo(
                configSettings = js.settings.toFirebaseRemoteConfigSettings(),
                fetchTimeMillis = js.fetchTimeMillis,
                lastFetchStatus = js.lastFetchStatus.toFetchStatus()
            )
        }

    actual suspend fun activate(): Boolean = rethrow { activate(js).await() }
    actual suspend fun ensureInitialized(): Unit = rethrow { ensureInitialized(js).await() }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long?): Unit =
        rethrow { fetchConfig(js).await() }

    actual suspend fun fetchAndActivate(): Boolean = rethrow { fetchAndActivate(js).await() }

    actual fun getValue(key: String): FirebaseRemoteConfigValue = rethrow {
        FirebaseRemoteConfigValue(getValue(js, key))
    }

    actual fun getKeysByPrefix(prefix: String): Set<String> {
        return getAllKeys().filter { it.startsWith(prefix) }.toSet()
    }

    private fun getAllKeys(): Set<String> {
        val objectKeys = js("Object.keys")
        return objectKeys(getAll(js)).unsafeCast<Array<String>>().toSet()
    }

    actual suspend fun reset() {
        // not implemented for JS target
    }

    actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        js.settings.apply {
            fetchTimeoutMillis = settings.fetchTimeoutInSeconds * 1000
            minimumFetchIntervalMillis = settings.minimumFetchIntervalInSeconds * 1000
        }
    }

    actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>) = rethrow {
        js.defaultConfig = json(*defaults)
    }

    private fun Settings.toFirebaseRemoteConfigSettings(): FirebaseRemoteConfigSettings {
        return FirebaseRemoteConfigSettings(
            fetchTimeoutInSeconds = fetchTimeoutMillis.toLong() / 1000,
            minimumFetchIntervalInSeconds = minimumFetchIntervalMillis.toLong() / 1000
        )
    }

    private fun String.toFetchStatus(): FetchStatus {
        return when (this) {
            "no-fetch-yet" -> FetchStatus.NoFetchYet
            "success" -> FetchStatus.Success
            "failure" -> FetchStatus.Failure
            "throttle" -> FetchStatus.Throttled
            else -> error("Unknown FetchStatus: $this")
        }
    }
}

actual open class FirebaseRemoteConfigException(code: String, cause: Throwable) :
    FirebaseException(code, cause)

actual class FirebaseRemoteConfigClientException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)

actual class FirebaseRemoteConfigFetchThrottledException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)

actual class FirebaseRemoteConfigServerException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)


internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic) = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code ->
        when {
            else -> {
                println("Unknown error code in ${JSON.stringify(error)}")
                FirebaseRemoteConfigException(code, error)
            }
        }
    }
