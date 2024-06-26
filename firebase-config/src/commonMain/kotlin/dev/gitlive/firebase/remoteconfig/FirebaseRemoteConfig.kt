package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseRemoteConfig] instance of the default [FirebaseApp]. */
public expect val Firebase.remoteConfig: FirebaseRemoteConfig

/** Returns the [FirebaseRemoteConfig] instance of a given [FirebaseApp]. */
public expect fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig

/**
 * Entry point for the Firebase Remote Config API.
 *
 * Callers should first get the singleton object using [Firebase.remoteConfig], and then call
 * operations on that singleton object. The singleton contains the complete set of Remote Config
 * parameter values available to your app. The singleton also stores values fetched from the Remote
 * Config server until they are made available for use with a call to [activate].
 */
public expect class FirebaseRemoteConfig {
    /**
     * Returns a [Map] of Firebase Remote Config key value pairs.
     *
     * Evaluates the values of the parameters in the following order:
     *
     * - The activated value, if the last successful [activate] contained the key.
     * - The default value, if the key was set with [setDefaults].
     */
    public val all: Map<String, FirebaseRemoteConfigValue>

    /**
     * Returns the state of this [FirebaseRemoteConfig] instance as a [FirebaseRemoteConfigInfo].
     */
    public val info: FirebaseRemoteConfigInfo

    /**
     * Asynchronously activates the most recently fetched configs, so that the fetched key value pairs
     * take effect.
     *
     * @return true result if the current call activated the fetched
     *     configs; if the fetched configs were already activated by a previous call, it instead
     *     returns a false result.
     */
    public suspend fun activate(): Boolean

    /**
     * Ensures the last activated config are available to the app.
     */
    public suspend fun ensureInitialized()

    /**
     * Starts fetching configs, adhering to the specified minimum fetch interval.
     *
     * The fetched configs only take effect after the next [activate] call.
     *
     * Depending on the time elapsed since the last fetch from the Firebase Remote Config backend,
     * configs are either served from local storage, or fetched from the backend.
     *
     * Note: Also initializes the Firebase installations SDK that creates installation IDs to
     * identify Firebase installations and periodically sends data to Firebase servers. Remote Config
     * requires installation IDs for Fetch requests. To stop the periodic sync, call [FirebaseInstallations.delete]. Sending a Fetch request
     * after deletion will create a new installation ID for this Firebase installation and resume the
     * periodic sync.
     *
     * @param minimumFetchIntervalInSeconds If configs in the local storage were fetched more than
     *     this many seconds ago, configs are served from the backend instead of local storage.
     */
    public suspend fun fetch(minimumFetchIntervalInSeconds: Long? = null)

    /**
     * Asynchronously fetches and then activates the fetched configs.
     *
     * If the time elapsed since the last fetch from the Firebase Remote Config backend is more
     * than the default minimum fetch interval, configs are fetched from the backend.
     *
     * After the fetch is complete, the configs are activated so that the fetched key value pairs
     * take effect.
     *
     * @return [Boolean] with a true result if the current call activated the fetched
     *     configs; if no configs were fetched from the backend and the local fetched configs have
     *     already been activated, returns a [Boolean] with a false result.
     */
    public suspend fun fetchAndActivate(): Boolean

    /**
     * Returns a [Set] of all Firebase Remote Config parameter keys with the given prefix.
     *
     * @param prefix The key prefix to look for. If the prefix is empty, all keys are returned.
     * @return [Set] of Remote Config parameter keys that start with the specified prefix.
     */
    public fun getKeysByPrefix(prefix: String): Set<String>

    /**
     * Returns the parameter value for the given key as a [FirebaseRemoteConfigValue].
     *
     * Evaluates the value of the parameter in the following order:
     *
     * - The activated value, if the last successful [activate] contained the key.
     * - The default value, if the key was set with [setDefaults].
     * - A [FirebaseRemoteConfigValue] that returns the static value for each type.
     *
     * @param key A Firebase Remote Config parameter key.
     * @return [FirebaseRemoteConfigValue] representing the value of the Firebase Remote Config
     *     parameter with the given key.
     */
    public fun getValue(key: String): FirebaseRemoteConfigValue

    /**
     * Deletes all activated, fetched and defaults configs and resets all Firebase Remote Config
     * settings.
     */
    public suspend fun reset()

    /**
     * Asynchronously changes the settings for this [FirebaseRemoteConfig] instance.
     *
     * @param init A builder to set the settings.
     */
    public suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit)

    /**
     * Asynchronously sets default configs using the given [Map].
     *
     * @param defaults [Map] of key value pairs representing Firebase Remote Config parameter
     *     keys and values.
     */
    public suspend fun setDefaults(vararg defaults: Pair<String, Any?>)
}

@Suppress("IMPLICIT_CAST_TO_ANY")
public inline operator fun <reified T> FirebaseRemoteConfig.get(key: String): T {
    val configValue = getValue(key)
    return when (T::class) {
        Boolean::class -> configValue.asBoolean()
        Double::class -> configValue.asDouble()
        Long::class -> configValue.asLong()
        String::class -> configValue.asString()
        FirebaseRemoteConfigValue::class -> configValue
        else -> throw IllegalArgumentException()
    } as T
}

/**
 * Exception that gets thrown when an operation on Firebase Remote Config fails.
 */
public expect open class FirebaseRemoteConfigException : FirebaseException

/**
 * Exception that gets thrown when an operation on Firebase Remote Config fails.
 */
public expect class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException

/**
 * Exception that gets thrown when an operation on Firebase Remote Config fails.
 */
public expect class FirebaseRemoteConfigFetchThrottledException : FirebaseRemoteConfigException

/**
 * Exception that gets thrown when an operation on Firebase Remote Config fails.
 */
public expect class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException
