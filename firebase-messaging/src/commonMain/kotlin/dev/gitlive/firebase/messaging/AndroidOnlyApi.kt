/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

// Deprecation messages of the com.google.firebase.messaging members that only Android can provide; kept out of the stripped
// package, where a constant would be verified against the SDK's own facade.

internal const val DELIVERY_METRICS_ANDROID_ONLY =
    "Exporting delivery metrics to BigQuery is an Android SDK setting with no counterpart in the Firebase iOS and JS SDKs, " +
        "which configure it on their own side; a call belongs in androidMain"
internal const val NOTIFICATION_DELEGATION_ANDROID_ONLY =
    "Notification delegation (proxy notifications) is an Android feature with no counterpart in the Firebase iOS and JS SDKs; " +
        "a call belongs in androidMain"
internal const val REGISTER_NOT_AVAILABLE =
    "register() and unregister() were added to the Firebase Android SDK after firebase-messaging 25.1.2, the version this SDK " +
        "depends on, and are not available yet"
internal const val MESSAGING_SERVICE_ANDROID_ONLY =
    "FirebaseMessagingService is an android.app.Service: declare the subclass in androidMain and register it in the Android " +
        "manifest; on iOS messages are received by the app delegate, and on JS by the service worker"
