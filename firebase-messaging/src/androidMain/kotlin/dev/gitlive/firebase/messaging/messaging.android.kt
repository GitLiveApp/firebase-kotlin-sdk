/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.messaging

/** The underlying Firebase Android SDK object. */
public val FirebaseMessaging.android: com.google.firebase.messaging.FirebaseMessaging get() = compat
