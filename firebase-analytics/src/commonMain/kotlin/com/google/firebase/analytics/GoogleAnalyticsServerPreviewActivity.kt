/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import android.os.Bundle
import dev.gitlive.firebase.analytics.PREVIEW_ACTIVITY_ANDROID_ONLY

/**
 * Android only: the `android.app.Activity` the Firebase Android SDK registers to receive Google Analytics server preview links.
 * Declared so that common code referring to it fails with a message saying so, rather than with an unresolved reference; on
 * Android the SDK's activity is used (see buildSrc utils/HeaderStubs.kt).
 */
@Suppress("DEPRECATION_ERROR")
@Deprecated(PREVIEW_ACTIVITY_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class GoogleAnalyticsServerPreviewActivity {
    public fun onCreate(savedInstanceState: Bundle?): Unit = throw UnsupportedOperationException(PREVIEW_ACTIVITY_ANDROID_ONLY)
}
