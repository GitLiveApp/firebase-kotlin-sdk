/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import android.net.Uri
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.messaging.stub

/*
 * Header stubs for com.google.firebase:firebase-messaging (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime.
 */

public actual class FirebaseMessaging private constructor() {
    public actual fun deleteToken(): Task<Nothing?> = stub()
    public actual fun getToken(): Task<String> = stub()

    /** Not in the common API (JS has no topics); the shipped nonJsMain extension binds to this member. */
    public fun subscribeToTopic(topic: String): Task<Nothing?> = stub()

    /** Not in the common API (JS has no topics); the shipped nonJsMain extension binds to this member. */
    public fun unsubscribeFromTopic(topic: String): Task<Nothing?> = stub()

    /** Not in the common API (JS has no auto-init); the shipped nonJsMain extension binds to this member. */
    public var isAutoInitEnabled: Boolean
        get() = stub()
        set(_) = stub()

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        @JvmField
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        @JvmStatic
        public actual fun getInstance(): FirebaseMessaging = stub()
    }
}

public actual class RemoteMessage private constructor() {
    public actual val collapseKey: String? get() = stub()
    public actual val data: Map<String, String> get() = stub()
    public actual val from: String? get() = stub()
    public actual val messageId: String? get() = stub()
    public actual val messageType: String? get() = stub()
    public actual val notification: Notification? get() = stub()
    public actual val originalPriority: Int get() = stub()
    public actual val priority: Int get() = stub()

    @Deprecated("Use from instead")
    public actual val senderId: String? get() = stub()
    public actual val sentTime: Long get() = stub()

    @Deprecated("Upstream messaging through Firebase Cloud Messaging has been decommissioned")
    public actual val to: String? get() = stub()
    public actual val ttl: Int get() = stub()

    public actual class Builder actual constructor(to: String) {
        public actual fun addData(key: String, value: String?): Builder = stub()
        public actual fun build(): RemoteMessage = stub()
        public actual fun clearData(): Builder = stub()
        public actual fun setCollapseKey(collapseKey: String?): Builder = stub()
        public actual fun setData(data: Map<String, String>): Builder = stub()
        public actual fun setMessageId(messageId: String): Builder = stub()
        public actual fun setMessageType(messageType: String?): Builder = stub()
        public actual fun setTtl(ttl: Int): Builder = stub()
    }

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class MessagePriority

    public actual class Notification private constructor() {
        public actual val body: String? get() = stub()
        public actual val bodyLocalizationArgs: Array<String>? get() = stub()
        public actual val bodyLocalizationKey: String? get() = stub()
        public actual val channelId: String? get() = stub()
        public actual val clickAction: String? get() = stub()
        public actual val color: String? get() = stub()
        public actual val defaultLightSettings: Boolean get() = stub()
        public actual val defaultSound: Boolean get() = stub()
        public actual val defaultVibrateSettings: Boolean get() = stub()
        public actual val eventTime: Long? get() = stub()
        public actual val icon: String? get() = stub()
        public actual val lightSettings: IntArray? get() = stub()
        public actual val localOnly: Boolean get() = stub()
        public actual val notificationCount: Int? get() = stub()
        public actual val notificationPriority: Int? get() = stub()
        public actual val sound: String? get() = stub()
        public actual val sticky: Boolean get() = stub()
        public actual val tag: String? get() = stub()
        public actual val ticker: String? get() = stub()
        public actual val title: String? get() = stub()
        public actual val titleLocalizationArgs: Array<String>? get() = stub()
        public actual val titleLocalizationKey: String? get() = stub()
        public actual val vibrateTimings: LongArray? get() = stub()
        public actual val visibility: Int? get() = stub()

        /** The SDK's Uri members; common code uses the String extensions of RemoteMessageUri.kt, which bind to these. */
        public val imageUrl: Uri? get() = stub()
        public val link: Uri? get() = stub()
    }

    public actual companion object {
        @JvmField
        public actual val PRIORITY_HIGH: Int = 1

        @JvmField
        public actual val PRIORITY_NORMAL: Int = 2

        @JvmField
        public actual val PRIORITY_UNKNOWN: Int = 0
    }
}

public actual class SendException private constructor() : Exception() {
    public actual val errorCode: Int get() = stub()

    public actual companion object {
        @JvmField
        public actual val ERROR_INVALID_PARAMETERS: Int = 1

        @JvmField
        public actual val ERROR_SIZE: Int = 2

        @JvmField
        public actual val ERROR_TOO_MANY_MESSAGES: Int = 4

        @JvmField
        public actual val ERROR_TTL_EXCEEDED: Int = 3

        @JvmField
        public actual val ERROR_UNKNOWN: Int = 0
    }
}
