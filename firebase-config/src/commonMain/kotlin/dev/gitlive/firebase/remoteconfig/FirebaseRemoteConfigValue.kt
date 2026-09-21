/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig as CompatFirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue as CompatFirebaseRemoteConfigValue

/**
 * Wrapper for a Remote Config parameter value, with methods to get it as different types.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.remoteconfig.FirebaseRemoteConfigValue] this wraps.
 */
public class FirebaseRemoteConfigValue internal constructor(public val compat: CompatFirebaseRemoteConfigValue) {
    /** Gets the value as a boolean. */
    public fun asBoolean(): Boolean = compat.asBoolean()

    /** Gets the value as a byte array. */
    public fun asByteArray(): ByteArray = compat.asByteArray()

    /** Gets the value as a double. */
    public fun asDouble(): Double = compat.asDouble()

    /** Gets the value as a long. */
    public fun asLong(): Long = compat.asLong()

    /** Gets the value as a string. */
    public fun asString(): String = compat.asString()

    /** Indicates at which source this value came from. */
    public fun getSource(): ValueSource = when (compat.source) {
        CompatFirebaseRemoteConfig.VALUE_SOURCE_STATIC -> ValueSource.Static
        CompatFirebaseRemoteConfig.VALUE_SOURCE_DEFAULT -> ValueSource.Default
        CompatFirebaseRemoteConfig.VALUE_SOURCE_REMOTE -> ValueSource.Remote
        else -> error("Unknown value source: ${compat.source}")
    }

    override fun equals(other: Any?): Boolean = other is FirebaseRemoteConfigValue && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseRemoteConfigValue(${asString()})"
}

public enum class ValueSource {
    Static,
    Default,
    Remote,
}
