/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import android.content.Context

/** The context the `dev.gitlive` API passes on to the `com.google.firebase` layer: the given one, or a placeholder where the platform has none. */
internal fun Any?.asContext(): Context = this as? Context ?: Context()
