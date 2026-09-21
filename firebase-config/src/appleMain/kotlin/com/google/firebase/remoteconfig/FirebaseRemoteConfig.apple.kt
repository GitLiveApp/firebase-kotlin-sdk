/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorDomain
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorInternalError
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorThrottled
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigFetchAndActivateStatus
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigFetchStatus
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSource
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigThrottledEndTimeInSecondsKey
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigUpdateErrorDomain
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigUpdateErrorMessageInvalid
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigUpdateErrorNotFetched
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigUpdateErrorStreamError
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigUpdateErrorUnavailable
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigValue
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.remoteconfig.SET_DEFAULTS_XML_ANDROID_ONLY
import dev.gitlive.firebase.remoteconfig.toByteArray
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.timeIntervalSince1970

private const val MILLIS_PER_SECOND = 1000.0
private const val NO_FETCH_YET_TIME_MILLIS = -1L

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseRemoteConfig internal constructor(public val ios: FIRRemoteConfig) {

    public actual fun activate(): Task<Boolean> = task { completion -> ios.activateWithCompletion { changed, error -> completion(changed, error) } }

    public actual fun ensureInitialized(): Task<FirebaseRemoteConfigInfo> = task { completion -> ios.ensureInitializedWithCompletionHandler { error -> completion(info, error) } }

    public actual fun fetch(): Task<Nothing?> = task { completion -> ios.fetchWithCompletionHandler { _, error -> completion(null, error) } }

    public actual fun fetch(minimumFetchIntervalInSeconds: Long): Task<Nothing?> = task { completion ->
        ios.fetchWithExpirationDuration(minimumFetchIntervalInSeconds.toDouble()) { _, error -> completion(null, error) }
    }

    public actual fun fetchAndActivate(): Task<Boolean> = task { completion ->
        ios.fetchAndActivateWithCompletionHandler { status, error ->
            completion(status == FIRRemoteConfigFetchAndActivateStatus.FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote, error)
        }
    }

    /** The active values: the defaults, overridden by the activated remote values (the iOS SDK keeps them per source). */
    @Suppress("UNCHECKED_CAST")
    public actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = listOf(FIRRemoteConfigSource.FIRRemoteConfigSourceDefault, FIRRemoteConfigSource.FIRRemoteConfigSourceRemote)
            .flatMap { source -> (ios.allKeysFromSource(source) as List<String>).map { key -> key to IosRemoteConfigValue(ios.configValueForKey(key, source)) } }
            .toMap()

    public actual fun getBoolean(key: String): Boolean = getValue(key).asBoolean()

    public actual fun getDouble(key: String): Double = getValue(key).asDouble()

    public actual val info: FirebaseRemoteConfigInfo
        get() = RemoteConfigInfo(
            configSettings = ios.configSettings.toCompat(),
            fetchTimeMillis = ios.lastFetchTime?.timeIntervalSince1970?.let { (it * MILLIS_PER_SECOND).toLong() }?.takeIf { it > 0 } ?: NO_FETCH_YET_TIME_MILLIS,
            lastFetchStatus = when (ios.lastFetchStatus) {
                FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusSuccess -> LAST_FETCH_STATUS_SUCCESS
                FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusNoFetchYet -> LAST_FETCH_STATUS_NO_FETCH_YET
                FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusThrottled -> LAST_FETCH_STATUS_THROTTLED
                else -> LAST_FETCH_STATUS_FAILURE
            },
        )

    @Suppress("UNCHECKED_CAST")
    public actual fun getKeysByPrefix(prefix: String): Set<String> = ios.keysWithPrefix(prefix) as Set<String>

    public actual fun getLong(key: String): Long = getValue(key).asLong()

    public actual fun getString(key: String): String = getValue(key).asString()

    public actual fun getValue(key: String): FirebaseRemoteConfigValue = IosRemoteConfigValue(ios.configValueForKey(key))

    /** The iOS SDK has no reset; the returned task completes without doing anything. */
    public actual fun reset(): Task<Nothing?> = completedTask()

    public actual fun setConfigSettingsAsync(settings: FirebaseRemoteConfigSettings): Task<Nothing?> {
        ios.configSettings = FIRRemoteConfigSettings().apply {
            fetchTimeout = settings.fetchTimeoutInSeconds.toDouble()
            minimumFetchInterval = settings.minimumFetchIntervalInSeconds.toDouble()
        }
        return completedTask()
    }

    @Deprecated(SET_DEFAULTS_XML_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setDefaultsAsync(resourceId: Int): Task<Nothing?> = throw UnsupportedOperationException(SET_DEFAULTS_XML_ANDROID_ONLY)

    public actual fun setDefaultsAsync(defaults: Map<String, Any?>): Task<Nothing?> {
        ios.setDefaults(defaults.mapKeys { it.key })
        return completedTask()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseRemoteConfig && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseRemoteConfig($ios)"

    public actual companion object {
        /** The value returned for a boolean key that is not set. */
        public actual val DEFAULT_VALUE_FOR_BOOLEAN: Boolean = false

        /** The value returned for a byte array key that is not set. */
        public actual val DEFAULT_VALUE_FOR_BYTE_ARRAY: ByteArray = ByteArray(0)

        /** The value returned for a double key that is not set. */
        public actual val DEFAULT_VALUE_FOR_DOUBLE: Double = 0.0

        /** The value returned for a long key that is not set. */
        public actual val DEFAULT_VALUE_FOR_LONG: Long = 0L

        /** The value returned for a string key that is not set. */
        public actual val DEFAULT_VALUE_FOR_STRING: String = ""

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch failed. */
        public actual val LAST_FETCH_STATUS_FAILURE: Int = 1

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: nothing has been fetched yet. */
        public actual val LAST_FETCH_STATUS_NO_FETCH_YET: Int = 0

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch succeeded. */
        public actual val LAST_FETCH_STATUS_SUCCESS: Int = -1

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch was throttled. */
        public actual val LAST_FETCH_STATUS_THROTTLED: Int = 2

        /** [FirebaseRemoteConfigValue.source]: the value comes from the defaults set by the app. */
        public actual val VALUE_SOURCE_DEFAULT: Int = 1

        /** [FirebaseRemoteConfigValue.source]: the value comes from the Remote Config backend. */
        public actual val VALUE_SOURCE_REMOTE: Int = 2

        /** [FirebaseRemoteConfigValue.source]: the key is not set and the static default is returned. */
        public actual val VALUE_SOURCE_STATIC: Int = 0

        public actual fun getInstance(): FirebaseRemoteConfig = FirebaseRemoteConfig(FIRRemoteConfig.remoteConfig())

        public actual fun getInstance(app: FirebaseApp): FirebaseRemoteConfig = FirebaseRemoteConfig(FIRRemoteConfig.remoteConfigWithApp(app.ios as objcnames.classes.FIRApp))
    }
}

/** @property ios The underlying Firebase iOS SDK object. */
internal class IosRemoteConfigValue(val ios: FIRRemoteConfigValue) : FirebaseRemoteConfigValue {
    override fun asBoolean(): Boolean = ios.boolValue

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun asByteArray(): ByteArray = ios.dataValue.toByteArray()

    override fun asDouble(): Double = ios.numberValue.doubleValue

    override fun asLong(): Long = ios.numberValue.longValue

    override fun asString(): String = ios.stringValue

    override val source: Int
        get() = when (ios.source) {
            FIRRemoteConfigSource.FIRRemoteConfigSourceRemote -> FirebaseRemoteConfig.VALUE_SOURCE_REMOTE
            FIRRemoteConfigSource.FIRRemoteConfigSourceDefault -> FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT
            else -> FirebaseRemoteConfig.VALUE_SOURCE_STATIC
        }

    override fun equals(other: Any?): Boolean = other is IosRemoteConfigValue && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseRemoteConfigValue(${asString()})"
}

internal fun FIRRemoteConfigSettings.toCompat(): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
    .setFetchTimeoutInSeconds(fetchTimeout.toLong())
    .setMinimumFetchIntervalInSeconds(minimumFetchInterval.toLong())
    .build()

internal inline fun <T> task(crossinline start: ((T, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(error.toRemoteConfigException()) }
    return source.task
}

/** Maps the iOS SDK's error domains and codes to the Android SDK's exception classes and [FirebaseRemoteConfigException.Code]s. */
internal fun NSError.toRemoteConfigException(): FirebaseRemoteConfigException = when (domain) {
    FIRRemoteConfigErrorDomain -> when (code) {
        FIRRemoteConfigErrorThrottled -> FirebaseRemoteConfigFetchThrottledException(
            ((userInfo[FIRRemoteConfigThrottledEndTimeInSecondsKey] as? NSNumber)?.doubleValue ?: 0.0).let { (it * MILLIS_PER_SECOND).toLong() },
        )
        FIRRemoteConfigErrorInternalError -> FirebaseRemoteConfigServerException(localizedDescription, FirebaseRemoteConfigException.Code.UNKNOWN)
        else -> FirebaseRemoteConfigClientException(localizedDescription)
    }
    FIRRemoteConfigUpdateErrorDomain -> FirebaseRemoteConfigException(
        localizedDescription,
        when (code) {
            FIRRemoteConfigUpdateErrorStreamError -> FirebaseRemoteConfigException.Code.CONFIG_UPDATE_STREAM_ERROR
            FIRRemoteConfigUpdateErrorNotFetched -> FirebaseRemoteConfigException.Code.CONFIG_UPDATE_NOT_FETCHED
            FIRRemoteConfigUpdateErrorMessageInvalid -> FirebaseRemoteConfigException.Code.CONFIG_UPDATE_MESSAGE_INVALID
            FIRRemoteConfigUpdateErrorUnavailable -> FirebaseRemoteConfigException.Code.CONFIG_UPDATE_UNAVAILABLE
            else -> FirebaseRemoteConfigException.Code.UNKNOWN
        },
    )
    else -> FirebaseRemoteConfigClientException(localizedDescription)
}
