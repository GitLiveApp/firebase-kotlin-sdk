/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.remoteconfig.SET_DEFAULTS_XML_ANDROID_ONLY

/**
 * The entry point of Firebase Remote Config, as the Android SDK's `com.google.firebase.remoteconfig.FirebaseRemoteConfig`.
 * Real-time config updates (`addOnConfigUpdateListener`, `configUpdates`) and custom signals (`setCustomSignals`) exist
 * on Android, the JVM and Apple platforms, as extensions in `nonJsMain`; the JS SDK has neither.
 */
public expect class FirebaseRemoteConfig {
    /** Makes the most recently fetched config available to the getters; true if it changed the active config. */
    public fun activate(): Task<Boolean>

    /** Completes with the [FirebaseRemoteConfigInfo] once the config has been loaded from disk. */
    public fun ensureInitialized(): Task<FirebaseRemoteConfigInfo>

    /** Fetches config from the backend, honouring the configured minimum fetch interval. */
    public fun fetch(): Task<Nothing?>

    /** Fetches config from the backend, if the cached config is older than [minimumFetchIntervalInSeconds]. */
    public fun fetch(minimumFetchIntervalInSeconds: Long): Task<Nothing?>

    /** [fetch] followed by [activate]; true if the fetched config changed the active config. */
    public fun fetchAndActivate(): Task<Boolean>

    /** All active values, keyed by parameter name. */
    public val all: Map<String, FirebaseRemoteConfigValue>

    /** The active value of [key] as a boolean, or [DEFAULT_VALUE_FOR_BOOLEAN]. */
    public fun getBoolean(key: String): Boolean

    /** The active value of [key] as a double, or [DEFAULT_VALUE_FOR_DOUBLE]. */
    public fun getDouble(key: String): Double

    /** The state of the most recent fetch and the current settings. */
    public val info: FirebaseRemoteConfigInfo

    /** The active keys starting with [prefix]. */
    public fun getKeysByPrefix(prefix: String): Set<String>

    /** The active value of [key] as a long, or [DEFAULT_VALUE_FOR_LONG]. */
    public fun getLong(key: String): Long

    /** The active value of [key] as a string, or [DEFAULT_VALUE_FOR_STRING]. */
    public fun getString(key: String): String

    /** The active value of [key]. */
    public fun getValue(key: String): FirebaseRemoteConfigValue

    /**
     * Clears the fetched, active and default config and the settings. The Apple and JS SDKs have no such operation, so
     * the returned task completes without doing anything there.
     */
    public fun reset(): Task<Nothing?>

    /** Applies [settings]. */
    public fun setConfigSettingsAsync(settings: FirebaseRemoteConfigSettings): Task<Nothing?>

    /** Android-only: reads the defaults from an XML resource. */
    @Deprecated(SET_DEFAULTS_XML_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public fun setDefaultsAsync(resourceId: Int): Task<Nothing?>

    /** Sets the default values, used until a fetched config is activated and for keys the backend does not set. */
    public fun setDefaultsAsync(defaults: Map<String, Any?>): Task<Nothing?>

    public companion object {
        /** The value returned for a boolean key that is not set. */
        public val DEFAULT_VALUE_FOR_BOOLEAN: Boolean

        /** The value returned for a byte array key that is not set. */
        public val DEFAULT_VALUE_FOR_BYTE_ARRAY: ByteArray

        /** The value returned for a double key that is not set. */
        public val DEFAULT_VALUE_FOR_DOUBLE: Double

        /** The value returned for a long key that is not set. */
        public val DEFAULT_VALUE_FOR_LONG: Long

        /** The value returned for a string key that is not set. */
        public val DEFAULT_VALUE_FOR_STRING: String

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch failed. */
        public val LAST_FETCH_STATUS_FAILURE: Int

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: nothing has been fetched yet. */
        public val LAST_FETCH_STATUS_NO_FETCH_YET: Int

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch succeeded. */
        public val LAST_FETCH_STATUS_SUCCESS: Int

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch was throttled. */
        public val LAST_FETCH_STATUS_THROTTLED: Int

        /** [FirebaseRemoteConfigValue.source]: the value comes from the defaults set by the app. */
        public val VALUE_SOURCE_DEFAULT: Int

        /** [FirebaseRemoteConfigValue.source]: the value comes from the Remote Config backend. */
        public val VALUE_SOURCE_REMOTE: Int

        /** [FirebaseRemoteConfigValue.source]: the key is not set and the static default is returned. */
        public val VALUE_SOURCE_STATIC: Int

        /** The [FirebaseRemoteConfig] of the default [FirebaseApp]. */
        public fun getInstance(): FirebaseRemoteConfig

        /** The [FirebaseRemoteConfig] of [app]. */
        public fun getInstance(app: FirebaseApp): FirebaseRemoteConfig
    }
}
