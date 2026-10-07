/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

/*
 * The Android SDK returns the notification's image and link as android.net.Uri, which common code cannot name, so
 * they are String extensions here; on Android the SDK's Uri members bind for Android code (a member wins over an
 * extension) and this file is shipped for common code.
 */

/** The URL of the notification image, if any. */
public expect val RemoteMessage.Notification.imageUrl: String?

/** The link opened when the notification is tapped, if any. */
public expect val RemoteMessage.Notification.link: String?
