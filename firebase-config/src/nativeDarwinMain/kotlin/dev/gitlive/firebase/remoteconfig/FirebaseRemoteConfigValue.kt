package dev.gitlive.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSource
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigValue

actual class FirebaseRemoteConfigValue internal constructor(private val native: FIRRemoteConfigValue) {
    @ExperimentalUnsignedTypes
    actual fun asByteArray(): ByteArray = native.dataValue.toByteArray()

    actual fun asBoolean(): Boolean = native.boolValue
    actual fun asDouble(): Double = native.numberValue.doubleValue
    actual fun asLong(): Long = native.numberValue.longValue
    actual fun asString(): String = native.stringValue ?: ""
    actual fun getSource(): ValueSource = when (native.source) {
        FIRRemoteConfigSource.FIRRemoteConfigSourceStatic -> ValueSource.Static
        FIRRemoteConfigSource.FIRRemoteConfigSourceDefault -> ValueSource.Default
        FIRRemoteConfigSource.FIRRemoteConfigSourceRemote -> ValueSource.Remote
        else -> error("Unknown value source:${native.source}")
    }
}
