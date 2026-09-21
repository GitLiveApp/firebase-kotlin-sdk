/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("RemoteConfigNonJsKt")

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task

/*
 * Unlike the header stubs around it, this file is shipped (see keepClasses in the build file): common code reaches the
 * SDK's members through these extensions, which bind to the members. firebase-java-sdk has no custom signals, so
 * setCustomSignals fails at runtime on the JVM, as it would without this layer.
 */

public actual fun FirebaseRemoteConfig.addOnConfigUpdateListener(listener: ConfigUpdateListener): ConfigUpdateListenerRegistration = addOnConfigUpdateListener(listener)

public actual fun FirebaseRemoteConfig.setCustomSignals(customSignals: CustomSignals): Task<Nothing?> = setCustomSignals(customSignals)
