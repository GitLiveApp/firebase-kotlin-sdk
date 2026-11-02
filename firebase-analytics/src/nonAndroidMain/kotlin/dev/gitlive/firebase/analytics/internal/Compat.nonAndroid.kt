/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/** A [FirebaseAnalytics] over the platform client of [app]; repeated lookups are equal as on Android, as it compares the client. */
internal actual fun compatAnalytics(app: FirebaseApp?): FirebaseAnalytics = FirebaseAnalytics.create(nativeAnalytics(app))
