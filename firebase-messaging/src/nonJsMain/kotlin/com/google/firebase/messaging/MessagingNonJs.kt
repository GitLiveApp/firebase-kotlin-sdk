/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task

/*
 * The members of the Android SDK's FirebaseMessaging that the JS SDK does not have, as extensions: on Android they are
 * shipped and bind to the SDK's members, which Android code resolves directly (a member wins over an extension).
 */

/** Subscribes this app instance to [topic]; the Android SDK's `subscribeToTopic`. */
public expect fun FirebaseMessaging.subscribeToTopic(topic: String): Task<Nothing?>

/** Unsubscribes this app instance from [topic]; the Android SDK's `unsubscribeFromTopic`. */
public expect fun FirebaseMessaging.unsubscribeFromTopic(topic: String): Task<Nothing?>

/** Whether the registration token is requested automatically at startup; the Android SDK's `isAutoInitEnabled` / `setAutoInitEnabled`. */
public expect var FirebaseMessaging.isAutoInitEnabled: Boolean
