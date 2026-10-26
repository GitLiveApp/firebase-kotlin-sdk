package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.remoteconfig.externals.Value

public val FirebaseRemoteConfigValue.js: Value get() = js

public actual class FirebaseRemoteConfigValue(internal val js: Value) {
    public actual fun asBoolean(): Boolean = rethrow { js.asBoolean() }
    public actual fun asByteArray(): ByteArray = rethrow { js.asString()?.encodeToByteArray() ?: byteArrayOf() }
    public actual fun asDouble(): Double = rethrow { js.asNumber().toDouble() }
    public actual fun asLong(): Long = rethrow { js.asNumber().toLong() }
    public actual fun asString(): String = rethrow { js.asString() ?: "" }
    public actual fun getSource(): ValueSource = rethrow { js.getSource().toSource() }

    private fun String.toSource() = when (this) {
        "default" -> ValueSource.Default
        "remote" -> ValueSource.Remote
        "static" -> ValueSource.Static
        else -> error("Unknown ValueSource: $this")
    }
}
