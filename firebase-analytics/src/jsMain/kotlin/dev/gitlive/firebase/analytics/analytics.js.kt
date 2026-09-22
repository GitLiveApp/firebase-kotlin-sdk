/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

/** The Firebase JS SDK object this wraps. */
public val FirebaseAnalytics.js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics get() = compat.js

/** The Firebase JS SDK object this wraps. */
public val com.google.firebase.analytics.FirebaseAnalytics.js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics get() = native.js
