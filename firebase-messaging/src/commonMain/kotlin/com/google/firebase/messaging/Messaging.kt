/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.firebase.Firebase

/*
 * The Kotlin extensions of the Android SDK's firebase-messaging (MessagingKt), as plain common code: on Android the
 * facade is a header stub that is stripped, so the SDK's own facade binds.
 */

/** The [FirebaseMessaging] singleton; the Android SDK's `Firebase.messaging`. */
public val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging.getInstance()

/** Builds a [RemoteMessage] addressed to [to] with [init] applied to a [RemoteMessage.Builder]. */
public inline fun remoteMessage(to: String, init: RemoteMessage.Builder.() -> Unit): RemoteMessage = RemoteMessage.Builder(to).apply(init).build()
