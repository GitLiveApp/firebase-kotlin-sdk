/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.remoteconfig.SET_DEFAULTS_XML_ANDROID_ONLY
import dev.gitlive.firebase.remoteconfig.stub

/*
 * Header stubs for com.google.firebase:firebase-config (see buildSrc utils/HeaderStubs.kt): compiled against, verified
 * to match the real classes, and deleted from the output so the real SDK binds at runtime.
 */

public actual class FirebaseRemoteConfig private constructor() {
    public actual fun activate(): Task<Boolean> = stub()
    public actual fun ensureInitialized(): Task<FirebaseRemoteConfigInfo> = stub()
    public actual fun fetch(): Task<Nothing?> = stub()
    public actual fun fetch(minimumFetchIntervalInSeconds: Long): Task<Nothing?> = stub()
    public actual fun fetchAndActivate(): Task<Boolean> = stub()
    public actual val all: Map<String, FirebaseRemoteConfigValue> get() = stub()
    public actual fun getBoolean(key: String): Boolean = stub()
    public actual fun getDouble(key: String): Double = stub()
    public actual val info: FirebaseRemoteConfigInfo get() = stub()
    public actual fun getKeysByPrefix(prefix: String): Set<String> = stub()
    public actual fun getLong(key: String): Long = stub()
    public actual fun getString(key: String): String = stub()
    public actual fun getValue(key: String): FirebaseRemoteConfigValue = stub()
    public actual fun reset(): Task<Nothing?> = stub()
    public actual fun setConfigSettingsAsync(settings: FirebaseRemoteConfigSettings): Task<Nothing?> = stub()

    @Deprecated(SET_DEFAULTS_XML_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setDefaultsAsync(resourceId: Int): Task<Nothing?> = stub()

    public actual fun setDefaultsAsync(defaults: Map<String, Any?>): Task<Nothing?> = stub()

    /** Not in the common API (JS has no real-time updates); the shipped nonJsMain extension binds to this member. */
    public fun addOnConfigUpdateListener(listener: ConfigUpdateListener): ConfigUpdateListenerRegistration = stub()

    /** Not in the common API (JS has no custom signals); the shipped nonJsMain extension binds to this member. */
    public fun setCustomSignals(customSignals: CustomSignals): Task<Nothing?> = stub()

    public actual companion object {
        /** The value returned for a boolean key that is not set. */
        @JvmField
        public actual val DEFAULT_VALUE_FOR_BOOLEAN: Boolean = false

        /** The value returned for a byte array key that is not set. */
        @JvmField
        public actual val DEFAULT_VALUE_FOR_BYTE_ARRAY: ByteArray = ByteArray(0)

        /** The value returned for a double key that is not set. */
        @JvmField
        public actual val DEFAULT_VALUE_FOR_DOUBLE: Double = 0.0

        /** The value returned for a long key that is not set. */
        @JvmField
        public actual val DEFAULT_VALUE_FOR_LONG: Long = 0L

        /** The value returned for a string key that is not set. */
        @JvmField
        public actual val DEFAULT_VALUE_FOR_STRING: String = ""

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch failed. */
        @JvmField
        public actual val LAST_FETCH_STATUS_FAILURE: Int = 1

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: nothing has been fetched yet. */
        @JvmField
        public actual val LAST_FETCH_STATUS_NO_FETCH_YET: Int = 0

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch succeeded. */
        @JvmField
        public actual val LAST_FETCH_STATUS_SUCCESS: Int = -1

        /** [FirebaseRemoteConfigInfo.lastFetchStatus]: the last fetch was throttled. */
        @JvmField
        public actual val LAST_FETCH_STATUS_THROTTLED: Int = 2

        /** [FirebaseRemoteConfigValue.source]: the value comes from the defaults set by the app. */
        @JvmField
        public actual val VALUE_SOURCE_DEFAULT: Int = 1

        /** [FirebaseRemoteConfigValue.source]: the value comes from the Remote Config backend. */
        @JvmField
        public actual val VALUE_SOURCE_REMOTE: Int = 2

        /** [FirebaseRemoteConfigValue.source]: the key is not set and the static default is returned. */
        @JvmField
        public actual val VALUE_SOURCE_STATIC: Int = 0

        @JvmStatic
        public actual fun getInstance(): FirebaseRemoteConfig = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseRemoteConfig = stub()
    }
}

public actual interface FirebaseRemoteConfigValue {
    public actual fun asBoolean(): Boolean
    public actual fun asByteArray(): ByteArray
    public actual fun asDouble(): Double
    public actual fun asLong(): Long
    public actual fun asString(): String
    public actual val source: Int
}

public actual interface FirebaseRemoteConfigInfo {
    public actual val configSettings: FirebaseRemoteConfigSettings
    public actual val fetchTimeMillis: Long
    public actual val lastFetchStatus: Int
}

public actual class FirebaseRemoteConfigSettings private constructor() {
    public actual val fetchTimeoutInSeconds: Long get() = stub()
    public actual val minimumFetchIntervalInSeconds: Long get() = stub()
    public actual fun toBuilder(): Builder = stub()

    public actual class Builder actual constructor() {
        public actual var fetchTimeoutInSeconds: Long
            get() = stub()
            set(_) = stub()
        public actual var minimumFetchIntervalInSeconds: Long
            get() = stub()
            set(_) = stub()
        public actual fun setFetchTimeoutInSeconds(duration: Long): Builder = stub()
        public actual fun setMinimumFetchIntervalInSeconds(minimumFetchInterval: Long): Builder = stub()
        public actual fun build(): FirebaseRemoteConfigSettings = stub()
    }
}

public actual class CustomSignals private constructor() {
    public actual class Builder actual constructor() {
        public actual fun put(key: String, value: String?): Builder = stub()
        public actual fun put(key: String, value: Long): Builder = stub()
        public actual fun put(key: String, value: Double): Builder = stub()
        public actual fun build(): CustomSignals = stub()
    }
}

// The exception stubs' bodies never run: the real classes bind at runtime, so the super calls only need to compile.

public actual open class FirebaseRemoteConfigException : FirebaseException {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, code: Code) : super(message)
    public actual constructor(message: String, cause: Throwable?) : super(message)
    public actual constructor(message: String, cause: Throwable?, code: Code) : super(message)

    public actual val code: Code get() = stub()

    public actual enum class Code(private val value: Int) {
        UNKNOWN(0),
        CONFIG_UPDATE_STREAM_ERROR(1),
        CONFIG_UPDATE_MESSAGE_INVALID(2),
        CONFIG_UPDATE_NOT_FETCHED(3),
        CONFIG_UPDATE_UNAVAILABLE(4),
        ;

        public actual fun value(): Int = value
    }
}

public actual open class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, code: Code) : super(message, code)
    public actual constructor(message: String, cause: Throwable?) : super(message, cause)
    public actual constructor(message: String, cause: Throwable?, code: Code) : super(message, cause, code)
}

public actual open class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException {
    public actual constructor(httpStatusCode: Int, message: String) : super(message)
    public actual constructor(httpStatusCode: Int, message: String, code: Code) : super(message, code)
    public actual constructor(httpStatusCode: Int, message: String, cause: Throwable?) : super(message, cause)
    public actual constructor(httpStatusCode: Int, message: String, cause: Throwable?, code: Code) : super(message, cause, code)
    public actual constructor(message: String, code: Code) : super(message, code)
    public actual constructor(message: String, cause: Throwable?, code: Code) : super(message, cause, code)

    public actual val httpStatusCode: Int get() = stub()
}

public actual open class FirebaseRemoteConfigFetchThrottledException actual constructor(throttleEndTimeMillis: Long) : FirebaseRemoteConfigException("") {
    public actual val throttleEndTimeMillis: Long get() = stub()
}
