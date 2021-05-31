package dev.gitlive.firebase.remoteconfig

expect class RemoteConfigValue {
    fun asBoolean(): Boolean
    fun asByteArray(): ByteArray
    fun asDouble(): Double
    fun asLong(): Long
    fun asString(): String
    fun getSource(): ValueSource
}

enum class ValueSource { Static, Default, Remote }
