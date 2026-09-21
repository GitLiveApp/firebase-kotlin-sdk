/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.android.gms.tasks.Task

@Suppress("UNCHECKED_CAST")
public actual fun FirebaseRemoteConfig.addOnConfigUpdateListener(listener: ConfigUpdateListener): ConfigUpdateListenerRegistration {
    val registration = ios.addOnConfigUpdateListener { update, error ->
        if (error != null) {
            listener.onError(error.toRemoteConfigException())
        } else if (update != null) {
            listener.onUpdate(ConfigUpdate.create(update.updatedKeys as Set<String>))
        }
    }
    return ConfigUpdateListenerRegistration { registration.remove() }
}

public actual fun FirebaseRemoteConfig.setCustomSignals(customSignals: CustomSignals): Task<Nothing?> = task { completion ->
    ios.setCustomSignals(customSignals.signals.mapKeys { it.key }) { error -> completion(null, error) }
}
