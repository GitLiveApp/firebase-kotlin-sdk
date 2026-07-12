package dev.gitlive.firebase.remoteconfig

import swiftPMImport.dev.gitlive.firebase.config.FIRRemoteConfigSource
import swiftPMImport.dev.gitlive.firebase.config.FIRRemoteConfigValue

public actual class FirebaseRemoteConfigValue internal constructor(private val ios: FIRRemoteConfigValue) {
    @ExperimentalUnsignedTypes
    public actual fun asByteArray(): ByteArray = ios.dataValue.toByteArray()

    public actual fun asBoolean(): Boolean = ios.boolValue
    public actual fun asDouble(): Double = ios.numberValue.doubleValue
    public actual fun asLong(): Long = ios.numberValue.longValue
    public actual fun asString(): String = ios.stringValue ?: ""
    public actual fun getSource(): ValueSource = when (ios.source) {
        FIRRemoteConfigSource.FIRRemoteConfigSourceStatic -> ValueSource.Static
        FIRRemoteConfigSource.FIRRemoteConfigSourceDefault -> ValueSource.Default
        FIRRemoteConfigSource.FIRRemoteConfigSourceRemote -> ValueSource.Remote
        else -> error("Unknown value source:${ios.source}")
    }
}
