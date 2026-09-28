/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp

/*
 * The Kotlin extensions of the Android SDK's firebase-config (RemoteConfigKt), as plain common code: on Android and the
 * JVM the facade is a header stub that is stripped, so the SDK's own facade binds. The real-time update and custom
 * signal extensions are in nonJsMain (RemoteConfigUpdates.kt).
 */

/** The [FirebaseRemoteConfig] of the default [FirebaseApp]; the Android SDK's `Firebase.remoteConfig`. */
public val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfig.getInstance()

/** The [FirebaseRemoteConfig] of [app]. */
public fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(app)

/** The active value of [key]; see [FirebaseRemoteConfig.getValue]. */
public operator fun FirebaseRemoteConfig.get(key: String): FirebaseRemoteConfigValue = getValue(key)

/** Builds [FirebaseRemoteConfigSettings] with [init] applied to a [FirebaseRemoteConfigSettings.Builder]. */
public fun remoteConfigSettings(init: FirebaseRemoteConfigSettings.Builder.() -> Unit): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder().apply(init).build()
