package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class AbstractFirebaseReferenceSerializer<T>(
    private val isNullable: Boolean
) : KSerializer<T> {

    override val descriptor = buildClassSerialDescriptor("DocumentReference") {
        isNullable = this@AbstractFirebaseReferenceSerializer.isNullable
        element<String>("path")
    }

    protected fun encode(encoder: Encoder, value: FirebaseReference?) {
        if (encoder is FirebaseEncoder) {
            encoder.value = value?.let {
                when(value) {
                    is FirebaseReference.Value -> value.value.platformValue
                    is FirebaseReference.ServerDelete -> FieldValue.delete
                }
            }
        } else {
            throw IllegalArgumentException("This serializer shall be used with FirebaseEncoder")
        }
    }

    protected fun decode(decoder: Decoder): FirebaseReference? {
        if (decoder is FirebaseDecoder) {
            return decoder.value
                ?.let(DocumentReference::fromPlatformValue)
                ?.let(FirebaseReference::Value)
        } else {
            throw IllegalArgumentException("This serializer shall be used with FirebaseEncoder")
        }
    }
}

object FirebaseReferenceNullableSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference?>(
    isNullable = true
) {
    override fun serialize(encoder: Encoder, value: FirebaseReference?) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): FirebaseReference? = decode(decoder)
}

object FirebaseReferenceSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference>(
    isNullable = false
) {
    override fun serialize(encoder: Encoder, value: FirebaseReference) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): FirebaseReference = requireNotNull(decode(decoder))
}
