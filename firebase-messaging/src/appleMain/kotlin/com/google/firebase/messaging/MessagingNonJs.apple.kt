/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task

public actual fun FirebaseMessaging.subscribeToTopic(topic: String): Task<Nothing?> = task { completion -> ios.subscribeToTopic(topic) { error -> completion(null, error) } }

public actual fun FirebaseMessaging.unsubscribeFromTopic(topic: String): Task<Nothing?> = task { completion -> ios.unsubscribeFromTopic(topic) { error -> completion(null, error) } }

public actual var FirebaseMessaging.isAutoInitEnabled: Boolean
    get() = ios.isAutoInitEnabled()
    set(value) {
        ios.autoInitEnabled = value
    }
