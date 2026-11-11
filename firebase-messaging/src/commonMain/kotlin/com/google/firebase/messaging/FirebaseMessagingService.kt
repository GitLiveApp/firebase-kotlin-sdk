/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import dev.gitlive.firebase.messaging.MESSAGING_SERVICE_ANDROID_ONLY

/**
 * Android only: the `android.app.Service` that receives messages and token refreshes. Declared so that a subclass written in
 * common code fails with a message saying where it belongs, rather than with an unresolved reference; on Android the real
 * class is used. Received messages are platform code on every platform (see `api/android-sdk/exclusions.txt`).
 */
@Deprecated(MESSAGING_SERVICE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public abstract class FirebaseMessagingService {
    /** Called with each message received while the app is in the foreground, or carrying data. */
    public open fun onMessageReceived(message: RemoteMessage) {}

    /** Called when the registration token is created or refreshed. */
    public open fun onNewToken(token: String) {}

    /** Called when pending messages were deleted on the server before they could be delivered. */
    public open fun onDeletedMessages() {}
}
