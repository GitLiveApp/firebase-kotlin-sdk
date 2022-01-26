package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue as AndroidFirebaseRemoteConfigValue

actual class FirebaseRemoteConfigValue internal constructor(
    private val android: AndroidFirebaseRemoteConfigValue
) {
    actual fun asBoolean(): Boolean = android.asBoolean()
    actual fun asByteArray(): ByteArray = android.asByteArray()
    actual fun asDouble(): Double = android.asDouble()
    actual fun asLong(): Long = android.asLong()
    actual fun asString(): String = android.asString()
    actual fun getSource(): ValueSource = when (android.source) {
        FirebaseRemoteConfig.VALUE_SOURCE_STATIC -> ValueSource.Static
        FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT -> ValueSource.Default
        FirebaseRemoteConfig.VALUE_SOURCE_REMOTE -> ValueSource.Remote
        else -> error("Unknown value source:${android.source}")
    }
}
