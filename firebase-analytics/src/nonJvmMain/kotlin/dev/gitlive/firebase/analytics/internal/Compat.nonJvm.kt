/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle

/** The bundle's values as the platform SDK takes them: nested bundles as maps and bundle arrays as lists. */
internal fun Bundle.toParameters(): Map<String, Any?> = map.mapValues { (_, value) -> value.toParameter() }

private fun Any?.toParameter(): Any? = when (this) {
    is Bundle -> toParameters()
    is Array<*> -> map { it.toParameter() }
    else -> this
}

/** A bundle of these parameters (nested maps become bundles; arrays and lists of bundles are kept for `items`). */
internal actual fun Map<String, Any?>.toBundle(): Bundle = Bundle(
    entries.associateTo(linkedMapOf()) { (key, value) ->
        key to when (value) {
            is Map<*, *> -> value.entries.associate { (k, v) -> k.toString() to v }.toBundle()
            is List<*> -> value.map { (it as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }?.toBundle() ?: it }.toTypedArray()
            else -> value
        }
    },
)
