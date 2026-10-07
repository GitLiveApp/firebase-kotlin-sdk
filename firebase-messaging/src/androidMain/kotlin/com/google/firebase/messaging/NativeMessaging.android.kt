/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.messaging.stub

/** Never used on Android: the FirebaseMessaging header stub is stripped and the SDK's class binds. Stripped, unverified. */
internal actual class NativeMessaging private constructor() {
    actual fun deleteToken(): Task<Nothing?> = stub()
    actual fun getToken(): Task<String> = stub()
    actual fun subscribeToTopic(topic: String): Task<Nothing?> = stub()
    actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = stub()
    actual var isAutoInitEnabled: Boolean
        get() = stub()
        set(_) = stub()

    actual companion object {
        actual fun getInstance(): NativeMessaging = stub()
    }
}
