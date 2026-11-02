/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")

package dev.gitlive.firebase.remoteconfig

/** The underlying Firebase Android SDK object. */
public val FirebaseRemoteConfig.android: com.google.firebase.remoteconfig.FirebaseRemoteConfig get() = compat
