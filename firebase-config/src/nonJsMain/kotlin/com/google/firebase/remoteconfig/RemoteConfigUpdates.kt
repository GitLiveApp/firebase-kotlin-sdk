/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * The real-time update and custom signal extensions of the Android SDK's RemoteConfigKt, as plain code that is a
 * header stub on Android and the JVM. The JS SDK has neither, hence nonJsMain.
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
