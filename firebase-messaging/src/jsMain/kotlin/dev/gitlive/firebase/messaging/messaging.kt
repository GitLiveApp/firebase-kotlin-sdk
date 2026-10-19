/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.messaging.externals.Messaging
import com.google.firebase.messaging.FirebaseMessaging as CompatFirebaseMessaging

/** The underlying Firebase JS SDK object. */
public val FirebaseMessaging.js: Messaging get() = compat.js

// This is not supported in the JS SDK
// https://firebase.google.com/docs/reference/js/messaging_.md#@firebase/messaging
internal actual fun CompatFirebaseMessaging.subscribeToTopicOrThrow(topic: String): Unit = throw NotImplementedError("Subscribing to topics is not supported in the JS SDK")

internal actual fun CompatFirebaseMessaging.unsubscribeFromTopicOrThrow(topic: String): Unit = throw NotImplementedError("Unsubscribing from topics is not supported in the JS SDK")
