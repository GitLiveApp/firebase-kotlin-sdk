/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

/** The settings of a [FirebaseDataConnect] instance: the [host] it connects to, whether over TLS, and its query cache. */
public class DataConnectSettings(
    public val host: String = "firebasedataconnect.googleapis.com",
    public val sslEnabled: Boolean = true,
    public val cacheSettings: CacheSettings?,
) {
    public constructor(
        host: String = "firebasedataconnect.googleapis.com",
        sslEnabled: Boolean = true,
    ) : this(host = host, sslEnabled = sslEnabled, cacheSettings = null)

    override fun equals(other: Any?): Boolean = other is DataConnectSettings && other.host == host && other.sslEnabled == sslEnabled && other.cacheSettings == cacheSettings

    override fun hashCode(): Int = listOf(DataConnectSettings::class, host, sslEnabled, cacheSettings).hashCode()

    override fun toString(): String = "DataConnectSettings(host=$host, sslEnabled=$sslEnabled, cacheSettings=$cacheSettings)"
}

/** A copy of these settings with the given values replaced. */
public fun DataConnectSettings.copy(
    host: String = this.host,
    sslEnabled: Boolean = this.sslEnabled,
): DataConnectSettings = DataConnectSettings(host = host, sslEnabled = sslEnabled, cacheSettings = cacheSettings)

/** A copy of these settings with the given values replaced. */
public fun DataConnectSettings.copy(
    host: String = this.host,
    sslEnabled: Boolean = this.sslEnabled,
    cacheSettings: CacheSettings?,
): DataConnectSettings = DataConnectSettings(host = host, sslEnabled = sslEnabled, cacheSettings = cacheSettings)
