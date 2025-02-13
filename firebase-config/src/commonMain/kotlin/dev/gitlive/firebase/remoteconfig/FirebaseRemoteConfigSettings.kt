package dev.gitlive.firebase.remoteconfig

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val CONNECTION_TIMEOUT = 1.minutes

// https://firebase.google.com/docs/remote-config/get-started?hl=en&platform=android#throttling
private val DEFAULT_FETCH_INTERVAL = 12.hours

/** Wraps the settings for [FirebaseRemoteConfig] operations. */
public data class FirebaseRemoteConfigSettings(
    /**
     * Returns the fetch timeout in seconds.
     *
     * The timeout specifies how long the client should wait for a connection to the Firebase
     * Remote Config server.
     */
    var fetchTimeout: Duration = CONNECTION_TIMEOUT,

    /** Returns the minimum interval between successive fetches calls in seconds. */
    var minimumFetchInterval: Duration = DEFAULT_FETCH_INTERVAL,
) {

    @Deprecated("Replaced with Kotlin Duration", replaceWith = ReplaceWith("fetchTimeout"))
    public var fetchTimeoutInSeconds: Long
        get() = fetchTimeout.inWholeSeconds
        set(value) {
            fetchTimeout = value.seconds
        }

    @Deprecated("Replaced with Kotlin Duration", replaceWith = ReplaceWith("minimumFetchInterval"))
    public var minimumFetchIntervalInSeconds: Long
        get() = minimumFetchInterval.inWholeSeconds
        set(value) {
            minimumFetchInterval = value.seconds
        }
}
