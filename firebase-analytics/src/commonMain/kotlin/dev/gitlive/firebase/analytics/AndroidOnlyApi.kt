/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

// Deprecation messages of the com.google.firebase.analytics members that only Android can provide.
internal const val SET_CURRENT_SCREEN_DEPRECATED =
    "Deprecated in the Firebase Android SDK and Android-only (it takes an android.app.Activity); log a SCREEN_VIEW event with the SCREEN_NAME and " +
        "SCREEN_CLASS parameters instead"
internal const val PREVIEW_ACTIVITY_ANDROID_ONLY =
    "GoogleAnalyticsServerPreviewActivity is the android.app.Activity that receives Google Analytics server preview links and only exists on Android, " +
        "where the SDK's manifest registers it"
