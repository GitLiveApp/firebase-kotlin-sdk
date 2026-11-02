/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.messaging.MESSAGING_UNSUPPORTED
import com.google.firebase.messaging.FirebaseMessaging as CompatFirebaseMessaging

// firebase-java-sdk has no Cloud Messaging: the layer's tasks fail with UnsupportedOperationException, and these
// Unit-returning helpers throw it rather than drop the failed task.
internal actual fun CompatFirebaseMessaging.subscribeToTopicOrThrow(topic: String): Unit = throw UnsupportedOperationException(MESSAGING_UNSUPPORTED)

internal actual fun CompatFirebaseMessaging.unsubscribeFromTopicOrThrow(topic: String): Unit = throw UnsupportedOperationException(MESSAGING_UNSUPPORTED)
