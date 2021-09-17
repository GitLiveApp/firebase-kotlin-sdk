package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.firebase

actual class FirebaseRemoteConfigValue(val js: firebase.remoteConfig.Value) {
    actual fun asBoolean(): Boolean = rethrow { js.asBoolean() }
    actual fun asByteArray(): ByteArray = rethrow { js.asString()?.encodeToByteArray() ?: byteArrayOf() }
    actual fun asDouble(): Double = rethrow { js.asNumber().toDouble() }
    actual fun asLong(): Long = rethrow { js.asNumber().toLong() }
    actual fun asString(): String = rethrow { js.asString() ?: "" }
    actual fun getSource(): ValueSource = rethrow { js.getSource().toSource() }

    private fun String.toSource() = when (this) {
        "default" -> ValueSource.Default
        "remote" -> ValueSource.Remote
        "static" -> ValueSource.Static
        else -> error("Unknown ValueSource: $this")
    }
}
