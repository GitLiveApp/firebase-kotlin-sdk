/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * The actual of the common [FirebaseMessaging] for the platforms whose SDKs have topic subscriptions and auto-init,
 * which the JS SDK lacks, with those members added: an actual class may declare more members than its expect, and a
 * consumer's common source set that targets Android and Apple platforms only resolves against this declaration, so
 * Android SDK code calling them compiles unchanged there, while common code that also targets JS does not resolve them.
 *
 * On Android this class is a header stub like the rest of the package (stripped, the SDK's class binds at runtime), so
 * its bodies never run there; on the JVM and Apple platforms they delegate to [NativeMessaging].
 */
public actual class FirebaseMessaging private constructor(internal val native: NativeMessaging) {
    public actual fun deleteToken(): Task<Nothing?> = native.deleteToken()

    public actual fun getToken(): Task<String> = native.getToken()

    /** Subscribes this app instance to [topic]; the Android SDK's `subscribeToTopic`. Not available on JS. */
    public fun subscribeToTopic(topic: String): Task<Nothing?> = native.subscribeToTopic(topic)

    /** Unsubscribes this app instance from [topic]; the Android SDK's `unsubscribeFromTopic`. Not available on JS. */
    public fun unsubscribeFromTopic(topic: String): Task<Nothing?> = native.unsubscribeFromTopic(topic)

    /** Whether the registration token is requested automatically at startup; the Android SDK's `isAutoInitEnabled` / `setAutoInitEnabled`. Not available on JS. */
    public var isAutoInitEnabled: Boolean
        get() = native.isAutoInitEnabled
        set(value) {
            native.isAutoInitEnabled = value
        }

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        @JvmField
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        private val shared by lazy { FirebaseMessaging(NativeMessaging.getInstance()) }

        @JvmStatic
        public actual fun getInstance(): FirebaseMessaging = shared
    }
}

/** The platform's messaging object behind [FirebaseMessaging]; dead code on Android, where the SDK's class binds. */
internal expect class NativeMessaging {
    fun deleteToken(): Task<Nothing?>
    fun getToken(): Task<String>
    fun subscribeToTopic(topic: String): Task<Nothing?>
    fun unsubscribeFromTopic(topic: String): Task<Nothing?>
    var isAutoInitEnabled: Boolean

    companion object {
        fun getInstance(): NativeMessaging
    }
}
