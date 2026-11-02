/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("RemoteConfigKt")

package com.google.firebase.remoteconfig

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * The real-time update and custom signal extensions of the Android SDK's RemoteConfigKt, as plain code that is a
 * header stub on Android and the JVM: part of the RemoteConfigKt multifile facade (see RemoteConfig.kt), so that it is
 * named like the SDK's facade it binds to.
 */

/** Real-time config updates as a [Flow]; each emission means the backend has a newer config to fetch and activate. */
public val FirebaseRemoteConfig.configUpdates: Flow<ConfigUpdate>
    get() = callbackFlow {
        val registration = addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    trySend(configUpdate)
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    close(error)
                }
            },
        )
        awaitClose { registration.remove() }
    }

/** Builds [CustomSignals] with [builder] applied to a [CustomSignals.Builder]. */
public fun customSignals(builder: CustomSignals.Builder.() -> Unit): CustomSignals = CustomSignals.Builder().apply(builder).build()
