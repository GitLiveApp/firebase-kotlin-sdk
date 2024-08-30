package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.SerializationStrategy

public data class EncodableValue(public val encoded: (EncodeSettings.Builder.() -> Unit) -> Any?)

public inline fun <reified T> T.encodable(): EncodableValue = EncodableValue {
    encode(this, it)
}
public fun <T : Any> T.encodableWithStrategy(stategy: SerializationStrategy<T>): EncodableValue = EncodableValue {
    dev.gitlive.firebase.internal.encode(stategy, this, it)
}
