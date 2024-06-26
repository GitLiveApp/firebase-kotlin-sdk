package dev.gitlive.firebase.remoteconfig

private const val CONNECTION_TIMEOUT_IN_SECONDS = 60L

// https://firebase.google.com/docs/remote-config/get-started?hl=en&platform=android#throttling
private const val DEFAULT_FETCH_INTERVAL_IN_SECONDS = 12 * 3600L

/** Wraps the settings for [FirebaseRemoteConfig] operations. */
public data class FirebaseRemoteConfigSettings(
    /**
     * Returns the fetch timeout in seconds.
     *
     * The timeout specifies how long the client should wait for a connection to the Firebase
     * Remote Config server.
     */
    var fetchTimeoutInSeconds: Long = CONNECTION_TIMEOUT_IN_SECONDS,

    /** Returns the minimum interval between successive fetches calls in seconds. */
    var minimumFetchIntervalInSeconds: Long = DEFAULT_FETCH_INTERVAL_IN_SECONDS,
)
