/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.messaging.FirebaseMessaging as CompatFirebaseMessaging
import com.google.firebase.messaging.messaging as compatMessaging

// The Android-SDK-shaped singleton is reached through the `Firebase.messaging` extension (MessagingKt), a real
// static method on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseMessaging] instance of the default app. */
public val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(com.google.firebase.Firebase.compatMessaging)

/**
 * Firebase Cloud Messaging.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.messaging.FirebaseMessaging] this wraps.
 */
public class FirebaseMessaging internal constructor(public val compat: CompatFirebaseMessaging) {
    /** Subscribes to [topic]; throws [NotImplementedError] on JS, whose SDK has no topics. */
    public fun subscribeToTopic(topic: String) {
        compat.subscribeToTopicOrThrow(topic)
    }

    /** Unsubscribes from [topic]; throws [NotImplementedError] on JS, whose SDK has no topics. */
    public fun unsubscribeFromTopic(topic: String) {
        compat.unsubscribeFromTopicOrThrow(topic)
    }

    /** The registration token of this app instance. */
    public suspend fun getToken(): String = compat.getToken().await()

    /** Deletes the registration token of this app instance. */
    public suspend fun deleteToken() {
        compat.deleteToken().await()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseMessaging && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseMessaging($compat)"
}

/** The topic members exist off JS only (nonJsMain), so the wrapper reaches them through these. */
internal expect fun CompatFirebaseMessaging.subscribeToTopicOrThrow(topic: String)

internal expect fun CompatFirebaseMessaging.unsubscribeFromTopicOrThrow(topic: String)
