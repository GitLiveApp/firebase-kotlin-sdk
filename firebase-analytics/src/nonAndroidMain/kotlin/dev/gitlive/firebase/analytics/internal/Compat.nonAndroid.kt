/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

private val instances = mutableMapOf<String?, FirebaseAnalytics>()

/** One [FirebaseAnalytics] per app (keyed by name), so repeated lookups are equal as on Android. */
internal actual fun compatAnalytics(app: FirebaseApp?): FirebaseAnalytics = instances.getOrPut(app?.name) { FirebaseAnalytics.create(nativeAnalytics(app)) }
