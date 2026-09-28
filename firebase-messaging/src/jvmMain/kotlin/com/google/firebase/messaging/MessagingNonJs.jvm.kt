/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task

public actual fun FirebaseMessaging.subscribeToTopic(topic: String): Task<Nothing?> = unsupported()

public actual fun FirebaseMessaging.unsubscribeFromTopic(topic: String): Task<Nothing?> = unsupported()

private var autoInitEnabled = true

/** Kept in memory: there is no registration to initialise on the JVM. */
public actual var FirebaseMessaging.isAutoInitEnabled: Boolean
    get() = autoInitEnabled
    set(value) {
        autoInitEnabled = value
    }
