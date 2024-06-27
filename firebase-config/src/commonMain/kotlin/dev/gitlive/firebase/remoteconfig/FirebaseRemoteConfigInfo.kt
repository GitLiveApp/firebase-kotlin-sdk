package dev.gitlive.firebase.remoteconfig

import kotlinx.datetime.Instant

/** Wraps the current state of the [FirebaseRemoteConfig] singleton object. */
public data class FirebaseRemoteConfigInfo(
    /**
     * Gets the current settings of the [FirebaseRemoteConfig] singleton object.
     *
     * @return A [FirebaseRemoteConfig] object indicating the current settings.
     */
    val configSettings: FirebaseRemoteConfigSettings,

    /**
     * Gets the [Instant] of the last successful fetch, regardless of
     * whether the fetch was activated or not.
     *
     * @return `Instant.fromEpochMilliseconds(-1)` if no fetch attempt has been made yet. Otherwise, returns the timestamp of the last
     *     successful fetch operation.
     */
    val fetchTime: Instant,

    /**
     * Gets the status of the most recent fetch attempt.
     *
     * @return Will return one of [FetchStatus.Success], [FetchStatus.Failure], [FetchStatus.Throttled], or [FetchStatus.NoFetchYet]
     */
    val lastFetchStatus: FetchStatus,
) {
    @Deprecated("Replaced with Kotlin Duration", replaceWith = ReplaceWith("fetchTime"))
    val fetchTimeMillis: Long get() = fetchTime.toEpochMilliseconds()
}

public enum class FetchStatus {
    /**
     * Indicates that the most recent fetch of parameter values from the Firebase Remote Config server
     * was completed successfully.
     */
    Success,

    /**
     * Indicates that the most recent attempt to fetch parameter values from the Firebase Remote
     * Config server has failed.
     */
    Failure,

    /**
     * Indicates that the most recent attempt to fetch parameter values from the Firebase Remote
     * Config server was throttled.
     */
    Throttled,

    /**
     * Indicates that the FirebaseRemoteConfig singleton object has not yet attempted to fetch
     * parameter values from the Firebase Remote Config server.
     */
    NoFetchYet,
}
