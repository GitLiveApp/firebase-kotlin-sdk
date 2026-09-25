/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics

/** The Firebase iOS SDK class this wraps (its API is static). */
public val FirebaseAnalytics.ios: FIRAnalytics.Companion get() = compat.ios

/** The Firebase iOS SDK class this wraps (its API is static). */
public val com.google.firebase.analytics.FirebaseAnalytics.ios: FIRAnalytics.Companion get() = native.ios
