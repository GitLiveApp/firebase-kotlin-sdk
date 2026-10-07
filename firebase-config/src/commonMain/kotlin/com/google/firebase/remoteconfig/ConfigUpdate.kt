/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import kotlin.jvm.JvmStatic

/** A real-time config update: the keys whose values changed on the backend, as the Android SDK's `ConfigUpdate`. */
public abstract class ConfigUpdate {
    /** The parameter keys whose values were added, changed or removed since the last fetch. */
    public abstract val updatedKeys: Set<String>

    public companion object {
        /** A [ConfigUpdate] for [updatedKeys]. */
        @JvmStatic
        public fun create(updatedKeys: Set<String>): ConfigUpdate = Simple(updatedKeys.toSet())
    }

    private class Simple(override val updatedKeys: Set<String>) : ConfigUpdate() {
        override fun equals(other: Any?): Boolean = other is ConfigUpdate && other.updatedKeys == updatedKeys

        override fun hashCode(): Int = updatedKeys.hashCode()

        override fun toString(): String = "ConfigUpdate(updatedKeys=$updatedKeys)"
    }
}

/** Receives real-time config updates; see `FirebaseRemoteConfig.addOnConfigUpdateListener`. */
public interface ConfigUpdateListener {
    /** Called when the backend has a newer config: fetch and activate to apply the [configUpdate]. */
    public fun onUpdate(configUpdate: ConfigUpdate)

    /** Called when the real-time stream fails; [error] carries a [FirebaseRemoteConfigException.Code]. */
    public fun onError(error: FirebaseRemoteConfigException)
}

/** The registration of a [ConfigUpdateListener]; [remove] stops the updates. */
public fun interface ConfigUpdateListenerRegistration {
    /** Stops delivering updates to the listener. */
    public fun remove()
}
