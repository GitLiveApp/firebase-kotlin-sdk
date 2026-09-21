/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task

/*
 * The members of the Android SDK's FirebaseRemoteConfig that the JS SDK does not have, as extensions: a member of the
 * common expect class cannot mention a nonJsMain type. On Android and the JVM these are shipped and bind to the SDK's
 * members, which Android code resolves directly (a member wins over an extension).
 */

/** Starts listening for real-time config updates; the Android SDK's `addOnConfigUpdateListener`. */
public expect fun FirebaseRemoteConfig.addOnConfigUpdateListener(listener: ConfigUpdateListener): ConfigUpdateListenerRegistration

/** Sets the custom signals sent with fetches; the Android SDK's `setCustomSignals`. */
public expect fun FirebaseRemoteConfig.setCustomSignals(customSignals: CustomSignals): Task<Nothing?>
