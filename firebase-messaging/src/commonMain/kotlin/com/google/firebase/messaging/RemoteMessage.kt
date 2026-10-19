/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

/**
 * A message received from, or (with [Builder]) sent to, Firebase Cloud Messaging, as the Android SDK's `RemoteMessage`.
 * On Android the SDK delivers received messages to a `FirebaseMessagingService`; the other platforms receive messages
 * through their own notification APIs, so there a [RemoteMessage] only comes from [Builder].
 */
public expect class RemoteMessage {
    /** The collapse key, if any. */
    public val collapseKey: String?

    /** The data payload. */
    public val data: Map<String, String>

    /** The sender, if any. */
    public val from: String?

    /** The message id, if any. */
    public val messageId: String?

    /** The message type, if any. */
    public val messageType: String?

    /** The notification payload, if the message carries one. */
    public val notification: Notification?

    /** The priority the message was sent with; one of the `PRIORITY_*` constants. */
    public val originalPriority: Int

    /** The priority the message was delivered with; one of the `PRIORITY_*` constants. */
    public val priority: Int

    /** The sender id. */
    @Deprecated("Use from instead")
    public val senderId: String?

    /** When the message was sent, in milliseconds since the epoch. */
    public val sentTime: Long

    /** The recipient. */
    @Deprecated("Upstream messaging through Firebase Cloud Messaging has been decommissioned")
    public val to: String?

    /** The time to live in seconds. */
    public val ttl: Int

    /** Builds a [RemoteMessage] addressed to [to]. */
    public class Builder {
        public constructor(to: String)

        /** Adds a data entry; a null [value] removes the key. */
        public fun addData(key: String, value: String?): Builder

        public fun build(): RemoteMessage

        /** Removes all data entries. */
        public fun clearData(): Builder

        public fun setCollapseKey(collapseKey: String?): Builder

        /** Replaces the data payload. */
        public fun setData(data: Map<String, String>): Builder

        public fun setMessageId(messageId: String): Builder

        public fun setMessageType(messageType: String?): Builder

        /** The time to live in seconds, from 0 to 86400 (a day). */
        public fun setTtl(ttl: Int): Builder
    }

    /** The priorities of a message; the Android SDK's `@IntDef`. */
    @Retention(AnnotationRetention.SOURCE)
    public annotation class MessagePriority

    /** The notification payload of a message, as the Android SDK's `RemoteMessage.Notification`; the image and link URLs are `imageUrl` and `link` extensions. */
    public class Notification {
        /** The body text. */
        public val body: String?

        /** The arguments of the localized body. */
        public val bodyLocalizationArgs: Array<String>?

        /** The key of the localized body in the app's string resources. */
        public val bodyLocalizationKey: String?

        /** The Android notification channel. */
        public val channelId: String?

        /** The action taken when the notification is tapped. */
        public val clickAction: String?

        /** The notification colour, as `#rrggbb`. */
        public val color: String?

        /** Whether the default light settings are used. */
        public val defaultLightSettings: Boolean

        /** Whether the default sound is used. */
        public val defaultSound: Boolean

        /** Whether the default vibration settings are used. */
        public val defaultVibrateSettings: Boolean

        /** The event time, in milliseconds since the epoch. */
        public val eventTime: Long?

        /** The icon resource name. */
        public val icon: String?

        /** The LED colour, on and off durations. */
        public val lightSettings: IntArray?

        /** Whether the notification is local to the device. */
        public val localOnly: Boolean

        /** The number of items the notification represents. */
        public val notificationCount: Int?

        /** The Android notification priority. */
        public val notificationPriority: Int?

        /** The sound resource name. */
        public val sound: String?

        /** Whether the notification persists when tapped. */
        public val sticky: Boolean

        /** The notification tag. */
        public val tag: String?

        /** The ticker text. */
        public val ticker: String?

        /** The title. */
        public val title: String?

        /** The arguments of the localized title. */
        public val titleLocalizationArgs: Array<String>?

        /** The key of the localized title in the app's string resources. */
        public val titleLocalizationKey: String?

        /** The vibration pattern. */
        public val vibrateTimings: LongArray?

        /** The Android notification visibility. */
        public val visibility: Int?
    }

    public companion object {
        /** [priority]: high. */
        public val PRIORITY_HIGH: Int

        /** [priority]: normal. */
        public val PRIORITY_NORMAL: Int

        /** [priority]: not set. */
        public val PRIORITY_UNKNOWN: Int
    }
}
