/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import com.google.firebase.remoteconfig.FirebaseRemoteConfig as CompatFirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo as CompatFirebaseRemoteConfigInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings as CompatFirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig as compatRemoteConfig

// The Android-SDK-shaped entry points are reached through the `Firebase.remoteConfig` extensions (RemoteConfigKt),
// which are real static methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseRemoteConfig] instance of the default [FirebaseApp]. */
public val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfig(com.google.firebase.Firebase.compatRemoteConfig)

/** Returns the [FirebaseRemoteConfig] instance of a given [FirebaseApp]. */
public fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = FirebaseRemoteConfig(com.google.firebase.Firebase.compatRemoteConfig(app.compat))

/**
 * Entry point for the Firebase Remote Config API.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.remoteconfig.FirebaseRemoteConfig] this wraps.
 */
public class FirebaseRemoteConfig internal constructor(public val compat: CompatFirebaseRemoteConfig) {
    /** Returns a map of Firebase Remote Config key value pairs. */
    public val all: Map<String, FirebaseRemoteConfigValue>
        get() = compat.all.mapValues { FirebaseRemoteConfigValue(it.value) }

    /** Returns the state of this [FirebaseRemoteConfig] instance. */
    public val info: FirebaseRemoteConfigInfo
        get() = compat.info.toCommon()

    /**
     * Asynchronously activates the most recently fetched configs, so that the fetched key value pairs take effect.
     *
     * @return true if there was a Fetched Config, and it was activated; false if no Fetched Config was found, or the
     *   Fetched Config was already activated.
     */
    public suspend fun activate(): Boolean = compat.activate().await()

    /** Ensures the last activated config is available to the getters. */
    public suspend fun ensureInitialized() {
        compat.ensureInitialized().await()
    }

    /**
     * Starts fetching configs, adhering to the specified minimum fetch interval.
     *
     * @param minimumFetchInterval If a fetch was made less than this interval ago, the cached config is used; the
     *   configured [FirebaseRemoteConfigSettings.minimumFetchInterval] when null.
     */
    public suspend fun fetch(minimumFetchInterval: Duration? = null) {
        if (minimumFetchInterval == null) compat.fetch().await() else compat.fetch(minimumFetchInterval.inWholeSeconds).await()
    }

    /**
     * Asynchronously fetches and then activates the fetched configs.
     *
     * @return true if the fetched config was activated; false otherwise.
     */
    public suspend fun fetchAndActivate(): Boolean = compat.fetchAndActivate().await()

    /** Returns the set of parameter keys that start with the given prefix. */
    public fun getKeysByPrefix(prefix: String): Set<String> = compat.getKeysByPrefix(prefix)

    /** Returns the parameter value for the given key. */
    public fun getValue(key: String): FirebaseRemoteConfigValue = FirebaseRemoteConfigValue(compat.getValue(key))

    /** Deletes all activated, fetched and defaults configs and resets the settings (a no-op on Apple and JS). */
    public suspend fun reset() {
        compat.reset().await()
    }

    /** Asynchronously changes the settings for this [FirebaseRemoteConfig] instance. */
    public suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        val compatSettings = CompatFirebaseRemoteConfigSettings.Builder()
            .setFetchTimeoutInSeconds(settings.fetchTimeout.inWholeSeconds)
            .setMinimumFetchIntervalInSeconds(settings.minimumFetchInterval.inWholeSeconds)
            .build()
        compat.setConfigSettingsAsync(compatSettings).await()
    }

    /** Asynchronously sets default configs using the given map. */
    public suspend fun setDefaults(vararg defaults: Pair<String, Any?>) {
        compat.setDefaultsAsync(defaults.toMap()).await()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseRemoteConfig && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseRemoteConfig($compat)"

    @OptIn(ExperimentalTime::class)
    private fun CompatFirebaseRemoteConfigInfo.toCommon(): FirebaseRemoteConfigInfo = FirebaseRemoteConfigInfo(
        configSettings = FirebaseRemoteConfigSettings(
            fetchTimeout = configSettings.fetchTimeoutInSeconds.seconds,
            minimumFetchInterval = configSettings.minimumFetchIntervalInSeconds.seconds,
        ),
        fetchTime = Instant.fromEpochMilliseconds(fetchTimeMillis),
        lastFetchStatus = when (lastFetchStatus) {
            CompatFirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> FetchStatus.Success
            CompatFirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> FetchStatus.NoFetchYet
            CompatFirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> FetchStatus.Throttled
            CompatFirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> FetchStatus.Failure
            else -> error("Unknown last fetch status value: $lastFetchStatus")
        },
    )
}

@Deprecated("Replaced with Kotlin Duration", replaceWith = ReplaceWith("fetch(minimumFetchIntervalInSeconds.seconds)"))
public suspend fun FirebaseRemoteConfig.fetch(minimumFetchIntervalInSeconds: Long) {
    fetch(minimumFetchIntervalInSeconds.seconds)
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

public typealias FirebaseRemoteConfigException = com.google.firebase.remoteconfig.FirebaseRemoteConfigException
public typealias FirebaseRemoteConfigClientException = com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
public typealias FirebaseRemoteConfigFetchThrottledException = com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
public typealias FirebaseRemoteConfigServerException = com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException
