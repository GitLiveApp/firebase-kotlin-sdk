package dev.gitlive.firebase.remoteconfig

/** Wrapper for a Remote Config parameter value, with methods to get it as different types. */
expect class FirebaseRemoteConfigValue {
    /**
     * Gets the value as a [Boolean].
     *
     * @return [Boolean] representation of this parameter value.
     */
    fun asBoolean(): Boolean

    /**
     * Gets the value as a [ByteArray].
     *
     * @return [ByteArray] representation of this parameter value.
     */
    fun asByteArray(): ByteArray

    /**
     * Gets the value as a [Double].
     *
     * @return [Double] representation of this parameter value.
     */
    fun asDouble(): Double

    /**
     * Gets the value as a [Long].
     *
     * @return [Long] representation of this parameter value.
     */
    fun asLong(): Long

    /**
     * Gets the value as a [String].
     *
     * @return [String] representation of this parameter value.
     */
    fun asString(): String

    /**
     * Indicates at which source this value came from.
     *
     * @return [ValueSource.Remote] if the value was retrieved from the server, [ValueSource.Default] if the value was set as a default, or [ValueSource.Stataic] if no value was found and a static default value was returned instead.
     */
    fun getSource(): ValueSource
}

enum class ValueSource {
    /** Indicates that the value returned is the static default value. */
    Static,

    /** Indicates that the value returned was retrieved from the defaults set by the client. */
    Default,

    /** Indicates that the value returned was retrieved from the Firebase Remote Config server. */
    Remote
}
