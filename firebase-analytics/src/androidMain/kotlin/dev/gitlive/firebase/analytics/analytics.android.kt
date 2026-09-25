/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("analyticsAndroid")
@file:JvmMultifileClass

package dev.gitlive.firebase.analytics

/** The Firebase Android SDK object this wraps. */
public val FirebaseAnalytics.android: com.google.firebase.analytics.FirebaseAnalytics get() = compat
