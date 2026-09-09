package dev.gitlive.firebase.remoteconfig

/**
 * Information about the updated config passed to the listener
 * registered via [FirebaseRemoteConfig.configUpdates].
 */
public data class ConfigUpdate(
    /**
     * A set of parameter keys whose values have been updated from a remote config fetch.
     * This includes keys that are added, deleted, or whose values have changed.
     */
    public val updatedKeys: Set<String>,
)
