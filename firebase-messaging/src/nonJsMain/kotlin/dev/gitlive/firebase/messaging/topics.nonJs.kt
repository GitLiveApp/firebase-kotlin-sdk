/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.messaging.subscribeToTopic
import com.google.firebase.messaging.unsubscribeFromTopic
import com.google.firebase.messaging.FirebaseMessaging as CompatFirebaseMessaging

internal actual fun CompatFirebaseMessaging.subscribeToTopicOrThrow(topic: String) {
    subscribeToTopic(topic)
}

internal actual fun CompatFirebaseMessaging.unsubscribeFromTopicOrThrow(topic: String) {
    unsubscribeFromTopic(topic)
}
