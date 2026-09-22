/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

/*
 * firebase-java-sdk has no Cloud Messaging and the Apple and JS SDKs deliver messages through their own APIs, so off
 * Android a RemoteMessage is a plain value built with RemoteMessage.Builder.
 */

public actual class RemoteMessage internal constructor(
    @Suppress("DEPRECATION")
    @Deprecated("Upstream messaging through Firebase Cloud Messaging has been decommissioned")
    public actual val to: String?,
    public actual val data: Map<String, String>,
    public actual val messageId: String?,
    public actual val messageType: String?,
    public actual val collapseKey: String?,
    public actual val ttl: Int,
) {
    public actual val from: String? get() = null
    public actual val notification: Notification? get() = null
    public actual val originalPriority: Int get() = PRIORITY_UNKNOWN
    public actual val priority: Int get() = PRIORITY_UNKNOWN

    @Deprecated("Use from instead")
    public actual val senderId: String? get() = null
    public actual val sentTime: Long get() = 0

    @Suppress("DEPRECATION")
    override fun equals(other: Any?): Boolean = other is RemoteMessage && other.to == to && other.data == data && other.messageId == messageId &&
        other.messageType == messageType && other.collapseKey == collapseKey && other.ttl == ttl

    @Suppress("DEPRECATION")
    override fun hashCode(): Int = listOf(to, data, messageId, messageType, collapseKey, ttl).hashCode()

    @Suppress("DEPRECATION")
    override fun toString(): String = "RemoteMessage(to=$to, messageId=$messageId, data=$data)"

    public actual class Builder actual constructor(private val to: String) {
        private val data = mutableMapOf<String, String>()
        private var messageId: String? = null
        private var messageType: String? = null
        private var collapseKey: String? = null
        private var ttl: Int = 0

        public actual fun addData(key: String, value: String?): Builder = apply { if (value == null) data.remove(key) else data[key] = value }

        public actual fun build(): RemoteMessage = RemoteMessage(to, data.toMap(), messageId, messageType, collapseKey, ttl)

        public actual fun clearData(): Builder = apply { data.clear() }

        public actual fun setCollapseKey(collapseKey: String?): Builder = apply { this.collapseKey = collapseKey }

        public actual fun setData(data: Map<String, String>): Builder = apply {
            this.data.clear()
            this.data.putAll(data)
        }

        public actual fun setMessageId(messageId: String): Builder = apply { this.messageId = messageId }

        public actual fun setMessageType(messageType: String?): Builder = apply { this.messageType = messageType }

        public actual fun setTtl(ttl: Int): Builder = apply {
            require(ttl in 0..MAX_TTL_SECONDS) { "TTL must be between 0 and $MAX_TTL_SECONDS seconds" }
            this.ttl = ttl
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class MessagePriority

    public actual class Notification internal constructor(
        public actual val body: String? = null,
        public actual val bodyLocalizationArgs: Array<String>? = null,
        public actual val bodyLocalizationKey: String? = null,
        public actual val channelId: String? = null,
        public actual val clickAction: String? = null,
        public actual val color: String? = null,
        public actual val defaultLightSettings: Boolean = false,
        public actual val defaultSound: Boolean = false,
        public actual val defaultVibrateSettings: Boolean = false,
        public actual val eventTime: Long? = null,
        public actual val icon: String? = null,
        public actual val lightSettings: IntArray? = null,
        public actual val localOnly: Boolean = false,
        public actual val notificationCount: Int? = null,
        public actual val notificationPriority: Int? = null,
        public actual val sound: String? = null,
        public actual val sticky: Boolean = false,
        public actual val tag: String? = null,
        public actual val ticker: String? = null,
        public actual val title: String? = null,
        public actual val titleLocalizationArgs: Array<String>? = null,
        public actual val titleLocalizationKey: String? = null,
        public actual val vibrateTimings: LongArray? = null,
        public actual val visibility: Int? = null,
        internal val imageUrlValue: String? = null,
        internal val linkValue: String? = null,
    )

    public actual companion object {
        public actual val PRIORITY_HIGH: Int = 1
        public actual val PRIORITY_NORMAL: Int = 2
        public actual val PRIORITY_UNKNOWN: Int = 0
    }
}

private const val MAX_TTL_SECONDS = 86_400

public actual val RemoteMessage.Notification.imageUrl: String? get() = imageUrlValue

public actual val RemoteMessage.Notification.link: String? get() = linkValue

public actual class SendException internal constructor(message: String, public actual val errorCode: Int) : Exception(message) {
    public actual companion object {
        public actual val ERROR_INVALID_PARAMETERS: Int = 1
        public actual val ERROR_SIZE: Int = 2
        public actual val ERROR_TOO_MANY_MESSAGES: Int = 4
        public actual val ERROR_TTL_EXCEEDED: Int = 3
        public actual val ERROR_UNKNOWN: Int = 0
    }
}
