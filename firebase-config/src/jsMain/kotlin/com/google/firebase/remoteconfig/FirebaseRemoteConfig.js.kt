/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.remoteconfig.SET_DEFAULTS_XML_ANDROID_ONLY
import dev.gitlive.firebase.remoteconfig.externals.RemoteConfig
import dev.gitlive.firebase.remoteconfig.externals.Value
import dev.gitlive.firebase.remoteconfig.externals.activate
import dev.gitlive.firebase.remoteconfig.externals.ensureInitialized
import dev.gitlive.firebase.remoteconfig.externals.fetchAndActivate
import dev.gitlive.firebase.remoteconfig.externals.fetchConfig
import dev.gitlive.firebase.remoteconfig.externals.getAll
import dev.gitlive.firebase.remoteconfig.externals.getRemoteConfig
import dev.gitlive.firebase.remoteconfig.externals.getValue
import kotlin.js.Promise
import kotlin.js.json

private const val MILLIS_PER_SECOND = 1000

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseRemoteConfig internal constructor(public val js: RemoteConfig) {

    public actual fun activate(): Task<Boolean> = task { activate(js) }

    public actual fun ensureInitialized(): Task<FirebaseRemoteConfigInfo> = task { ensureInitialized(js).then { info } }

    /** The JS SDK has no per-call interval: this is the same as [fetch]. */
    public actual fun fetch(): Task<Nothing?> = task { fetchConfig(js).then { null } }

    public actual fun fetch(minimumFetchIntervalInSeconds: Long): Task<Nothing?> = fetch()

    public actual fun fetchAndActivate(): Task<Boolean> = task { fetchAndActivate(js) }

    public actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = rethrow { allKeys().associateWith { getValue(it) } }

    public actual fun getBoolean(key: String): Boolean = getValue(key).asBoolean()

    public actual fun getDouble(key: String): Double = getValue(key).asDouble()

    public actual val info: FirebaseRemoteConfigInfo
        get() = rethrow {
            RemoteConfigInfo(
                configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setFetchTimeoutInSeconds(js.settings.fetchTimeoutMillis.toLong() / MILLIS_PER_SECOND)
                    .setMinimumFetchIntervalInSeconds(js.settings.minimumFetchIntervalMillis.toLong() / MILLIS_PER_SECOND)
                    .build(),
                fetchTimeMillis = js.fetchTimeMillis.toLong(),
                lastFetchStatus = when (js.lastFetchStatus) {
                    "success" -> LAST_FETCH_STATUS_SUCCESS
                    "failure" -> LAST_FETCH_STATUS_FAILURE
                    "throttle" -> LAST_FETCH_STATUS_THROTTLED
                    else -> LAST_FETCH_STATUS_NO_FETCH_YET
                },
            )
        }

    public actual fun getKeysByPrefix(prefix: String): Set<String> = rethrow { allKeys().filterTo(mutableSetOf()) { it.startsWith(prefix) } }

    public actual fun getLong(key: String): Long = getValue(key).asLong()

    public actual fun getString(key: String): String = getValue(key).asString()

    public actual fun getValue(key: String): FirebaseRemoteConfigValue = rethrow { JsRemoteConfigValue(getValue(js, key)) }

    /** The JS SDK has no reset; the returned task completes without doing anything. */
    public actual fun reset(): Task<Nothing?> = completedTask()

    public actual fun setConfigSettingsAsync(settings: FirebaseRemoteConfigSettings): Task<Nothing?> = rethrow {
        js.settings.fetchTimeoutMillis = settings.fetchTimeoutInSeconds * MILLIS_PER_SECOND
        js.settings.minimumFetchIntervalMillis = settings.minimumFetchIntervalInSeconds * MILLIS_PER_SECOND
        completedTask()
    }

    @Deprecated(SET_DEFAULTS_XML_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setDefaultsAsync(resourceId: Int): Task<Nothing?> = throw UnsupportedOperationException(SET_DEFAULTS_XML_ANDROID_ONLY)

    public actual fun setDefaultsAsync(defaults: Map<String, Any?>): Task<Nothing?> = rethrow {
        js.defaultConfig = json(*defaults.map { (key, value) -> key to value }.toTypedArray())
        completedTask()
    }

    private fun allKeys(): Set<String> {
        val objectKeys = js("Object.keys")
        return objectKeys(getAll(js)).unsafeCast<Array<String>>().toSet()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseRemoteConfig && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseRemoteConfig($js)"

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

        public actual fun getInstance(): FirebaseRemoteConfig = rethrow { FirebaseRemoteConfig(getRemoteConfig()) }

        public actual fun getInstance(app: FirebaseApp): FirebaseRemoteConfig = rethrow { FirebaseRemoteConfig(getRemoteConfig(app.js)) }
    }
}

/** @property js The underlying Firebase JS SDK object. */
internal class JsRemoteConfigValue(val js: Value) : FirebaseRemoteConfigValue {
    override fun asBoolean(): Boolean = rethrow { js.asBoolean() }

    override fun asByteArray(): ByteArray = asString().encodeToByteArray()

    override fun asDouble(): Double = rethrow { js.asNumber().toDouble() }

    override fun asLong(): Long = rethrow { js.asNumber().toLong() }

    override fun asString(): String = rethrow { js.asString() ?: "" }

    override val source: Int
        get() = when (rethrow { js.getSource() }) {
            "remote" -> FirebaseRemoteConfig.VALUE_SOURCE_REMOTE
            "default" -> FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT
            else -> FirebaseRemoteConfig.VALUE_SOURCE_STATIC
        }

    override fun equals(other: Any?): Boolean = other is JsRemoteConfigValue && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseRemoteConfigValue(${asString()})"
}

private inline fun <T> task(start: () -> Promise<T>): Task<T> {
    val source = TaskCompletionSource<T>()
    try {
        start().then({ source.setResult(it) }, { source.setException(it.toRemoteConfigException()) })
    } catch (e: Throwable) {
        source.setException(e.toRemoteConfigException())
    }
    return source.task
}

internal inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toRemoteConfigException()
}

/** Maps the JS SDK's `remoteconfig/<code>` errors to the Android SDK's exception classes. */
internal fun Throwable.toRemoteConfigException(): FirebaseRemoteConfigException {
    if (this is FirebaseRemoteConfigException) return this
    val code = asDynamic().code.unsafeCast<String?>() ?: ""
    val customData = asDynamic().customData
    val text = message ?: code
    return when (code.substringAfter('/')) {
        "fetch-throttle" -> FirebaseRemoteConfigFetchThrottledException((customData?.throttleEndTimeMillis as? Number)?.toLong() ?: 0L)
        "fetch-status" -> FirebaseRemoteConfigServerException((customData?.httpStatus as? Number)?.toInt() ?: -1, text, this)
        else -> FirebaseRemoteConfigClientException(text, this)
    }
}
