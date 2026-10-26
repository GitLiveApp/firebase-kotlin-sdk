/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

private const val DEFAULT_FETCH_TIMEOUT_SECONDS = 60L
private const val DEFAULT_MINIMUM_FETCH_INTERVAL_SECONDS = 43_200L // 12 hours, as the Android SDK

public actual class FirebaseRemoteConfigSettings private constructor(
    public actual val fetchTimeoutInSeconds: Long,
    public actual val minimumFetchIntervalInSeconds: Long,
) {
    public actual fun toBuilder(): Builder = Builder().setFetchTimeoutInSeconds(fetchTimeoutInSeconds).setMinimumFetchIntervalInSeconds(minimumFetchIntervalInSeconds)

    override fun equals(other: Any?): Boolean = other is FirebaseRemoteConfigSettings && other.fetchTimeoutInSeconds == fetchTimeoutInSeconds && other.minimumFetchIntervalInSeconds == minimumFetchIntervalInSeconds

    override fun hashCode(): Int = 31 * fetchTimeoutInSeconds.hashCode() + minimumFetchIntervalInSeconds.hashCode()

    override fun toString(): String = "FirebaseRemoteConfigSettings(fetchTimeoutInSeconds=$fetchTimeoutInSeconds, minimumFetchIntervalInSeconds=$minimumFetchIntervalInSeconds)"

    public actual class Builder actual constructor() {
        public actual var fetchTimeoutInSeconds: Long = DEFAULT_FETCH_TIMEOUT_SECONDS
            set(value) {
                require(value >= 0) { "Fetch connection timeout has to be a non-negative number. $value is an invalid argument" }
                field = value
            }

        public actual var minimumFetchIntervalInSeconds: Long = DEFAULT_MINIMUM_FETCH_INTERVAL_SECONDS
            set(value) {
                require(value >= 0) { "Minimum interval between fetches has to be a non-negative number. $value is an invalid argument" }
                field = value
            }

        public actual fun setFetchTimeoutInSeconds(duration: Long): Builder = apply { fetchTimeoutInSeconds = duration }

        public actual fun setMinimumFetchIntervalInSeconds(minimumFetchInterval: Long): Builder = apply { minimumFetchIntervalInSeconds = minimumFetchInterval }

        public actual fun build(): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings(fetchTimeoutInSeconds, minimumFetchIntervalInSeconds)
    }
}

/** The [FirebaseRemoteConfigInfo] of the non-JVM implementations. */
internal class RemoteConfigInfo(
    override val configSettings: FirebaseRemoteConfigSettings,
    override val fetchTimeMillis: Long,
    override val lastFetchStatus: Int,
) : FirebaseRemoteConfigInfo {
    override fun toString(): String = "FirebaseRemoteConfigInfo(configSettings=$configSettings, fetchTimeMillis=$fetchTimeMillis, lastFetchStatus=$lastFetchStatus)"
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

/** Completes a `Task<Nothing?>` immediately, for the operations the platform SDK performs synchronously or lacks. */
internal fun completedTask(): com.google.android.gms.tasks.Task<Nothing?> = com.google.android.gms.tasks.TaskCompletionSource<Nothing?>().apply { setResult(null) }.task
