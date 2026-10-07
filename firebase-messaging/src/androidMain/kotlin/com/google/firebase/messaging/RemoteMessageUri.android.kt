/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("RemoteMessageUriKt")

package com.google.firebase.messaging

// Shipped (see keepClasses in the build file): the String forms of the SDK's android.net.Uri members, for common code.

public actual val RemoteMessage.Notification.imageUrl: String? get() = imageUrl?.toString()

public actual val RemoteMessage.Notification.link: String? get() = link?.toString()
