/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("MessagingNonJsKt")

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task

// Shipped (see keepClasses in the build file): common code reaches the SDK's members through these extensions.

public actual fun FirebaseMessaging.subscribeToTopic(topic: String): Task<Nothing?> = subscribeToTopic(topic)

public actual fun FirebaseMessaging.unsubscribeFromTopic(topic: String): Task<Nothing?> = unsubscribeFromTopic(topic)

public actual var FirebaseMessaging.isAutoInitEnabled: Boolean
    get() = isAutoInitEnabled
    set(value) {
        isAutoInitEnabled = value
    }
