/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

/** The state of the most recent fetch and the current settings, as the Android SDK's `FirebaseRemoteConfigInfo`. */
public expect interface FirebaseRemoteConfigInfo {
    /** The current settings. */
    public val configSettings: FirebaseRemoteConfigSettings

    /** When the last successful fetch completed, in milliseconds since the epoch, or -1 if nothing has been fetched. */
    public val fetchTimeMillis: Long

    /** One of the `FirebaseRemoteConfig.LAST_FETCH_STATUS_*` constants. */
    public val lastFetchStatus: Int
}
