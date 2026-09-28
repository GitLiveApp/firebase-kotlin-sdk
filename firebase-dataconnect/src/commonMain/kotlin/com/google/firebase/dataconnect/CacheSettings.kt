/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlin.time.Duration

/** The settings of the query result cache, see [DataConnectSettings.cacheSettings]. */
public class CacheSettings(
    public val storage: Storage = Storage.PERSISTENT,
    public val maxAge: Duration = Duration.ZERO,
) {
    /** Where cached query results are kept. */
    public enum class Storage {
        MEMORY,
        PERSISTENT,
    }

    override fun equals(other: Any?): Boolean = other is CacheSettings && other.storage == storage && other.maxAge == maxAge

    override fun hashCode(): Int = listOf(CacheSettings::class, storage, maxAge).hashCode()

    override fun toString(): String = "CacheSettings(storage=$storage, maxAge=$maxAge)"
}

/** A copy of these settings with the given values replaced. */
public fun CacheSettings.copy(
    storage: CacheSettings.Storage = this.storage,
    maxAge: Duration = this.maxAge,
): CacheSettings = CacheSettings(storage = storage, maxAge = maxAge)
