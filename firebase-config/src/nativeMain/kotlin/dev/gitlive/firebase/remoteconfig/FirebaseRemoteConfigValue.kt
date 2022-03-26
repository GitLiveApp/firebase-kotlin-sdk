package dev.gitlive.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSource
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigValue

actual class FirebaseRemoteConfigValue internal constructor(private val ios: FIRRemoteConfigValue) {
    @ExperimentalUnsignedTypes
    actual fun asByteArray(): ByteArray = ios.dataValue.toByteArray()

    actual fun asBoolean(): Boolean = ios.boolValue
    actual fun asDouble(): Double = ios.numberValue.doubleValue
    actual fun asLong(): Long = ios.numberValue.longValue
    actual fun asString(): String = ios.stringValue ?: ""
    actual fun getSource(): ValueSource = when (ios.source) {
        FIRRemoteConfigSource.FIRRemoteConfigSourceStatic -> ValueSource.Static
        FIRRemoteConfigSource.FIRRemoteConfigSourceDefault -> ValueSource.Default
        FIRRemoteConfigSource.FIRRemoteConfigSourceRemote -> ValueSource.Remote
        else -> error("Unknown value source:${ios.source}")
    }
}
