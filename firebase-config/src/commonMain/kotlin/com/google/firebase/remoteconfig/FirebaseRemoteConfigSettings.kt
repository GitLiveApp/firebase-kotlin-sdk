/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

/**
 * The settings of a [FirebaseRemoteConfig], as the Android SDK's `FirebaseRemoteConfigSettings`: built with [Builder],
 * whose setters can be chained or, from Kotlin, assigned as properties (`remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }`).
 */
public expect class FirebaseRemoteConfigSettings {
    /** How long a fetch may take before failing, in seconds. */
    public val fetchTimeoutInSeconds: Long

    /** How old the cached config may be before [FirebaseRemoteConfig.fetch] contacts the backend, in seconds. */
    public val minimumFetchIntervalInSeconds: Long

    /** A [Builder] initialised with these settings. */
    public fun toBuilder(): Builder

    public class Builder {
        public constructor()

        /** See [FirebaseRemoteConfigSettings.fetchTimeoutInSeconds]; 60 seconds by default. */
        public var fetchTimeoutInSeconds: Long

        /** See [FirebaseRemoteConfigSettings.minimumFetchIntervalInSeconds]; 12 hours by default. */
        public var minimumFetchIntervalInSeconds: Long

        /** Sets [fetchTimeoutInSeconds]; throws [IllegalArgumentException] if negative. */
        public fun setFetchTimeoutInSeconds(duration: Long): Builder

        /** Sets [minimumFetchIntervalInSeconds]; throws [IllegalArgumentException] if negative. */
        public fun setMinimumFetchIntervalInSeconds(minimumFetchInterval: Long): Builder

        public fun build(): FirebaseRemoteConfigSettings
    }
}
