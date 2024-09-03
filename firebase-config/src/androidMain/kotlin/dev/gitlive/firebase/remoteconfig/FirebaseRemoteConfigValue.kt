package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue as AndroidFirebaseRemoteConfigValue

public actual class FirebaseRemoteConfigValue internal constructor(
    private val android: AndroidFirebaseRemoteConfigValue,
) {
    public actual fun asBoolean(): Boolean = android.asBoolean()
    public actual fun asByteArray(): ByteArray = android.asByteArray()
    public actual fun asDouble(): Double = android.asDouble()
    public actual fun asLong(): Long = android.asLong()
    public actual fun asString(): String = android.asString()
    public actual fun getSource(): ValueSource = when (android.source) {
        FirebaseRemoteConfig.VALUE_SOURCE_STATIC -> ValueSource.Static
        FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT -> ValueSource.Default
        FirebaseRemoteConfig.VALUE_SOURCE_REMOTE -> ValueSource.Remote
        else -> error("Unknown value source:${android.source}")
    }
}
