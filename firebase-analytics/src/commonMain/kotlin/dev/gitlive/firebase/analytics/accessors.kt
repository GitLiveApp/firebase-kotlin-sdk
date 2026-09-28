/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("analyticsAndroid")
@file:JvmMultifileClass

package dev.gitlive.firebase.analytics

import com.google.firebase.analytics.analytics
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.analytics.internal.compatAnalytics
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/** The [FirebaseAnalytics] of the default app. */
public val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(com.google.firebase.Firebase.analytics)

/** The [FirebaseAnalytics] of [app] (the single instance on Android and Apple, where analytics is per process). */
public fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(compatAnalytics(app.compat))
