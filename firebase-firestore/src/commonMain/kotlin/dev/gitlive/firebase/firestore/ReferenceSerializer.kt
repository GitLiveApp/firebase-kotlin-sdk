package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

sealed class AbstractFirebaseReferenceSerializer<T>(
    private val isNullable: Boolean
) : KSerializer<T> {

    override val descriptor = buildClassSerialDescriptor("DocumentReference", isNullable = isNullable) {
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
        return if (decoder is FirebaseDecoder) {
            decoder.value
                ?.let(DocumentReference::fromPlatformValue)
                ?.let(FirebaseReference::Value)
        } else {
            throw IllegalArgumentException("This serializer shall be used with FirebaseDecoder")
        }
    }
}

/** A nullable serializer for [FirebaseReference]. */
@Deprecated("Consider using DocumentReference and FirebaseDocumentReferenceSerializer instead")
object FirebaseReferenceNullableSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference?>(
    isNullable = true
) {
    override fun serialize(encoder: Encoder, value: FirebaseReference?) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): FirebaseReference? = decode(decoder)
}

/** A serializer for [FirebaseReference]. */
@Deprecated("Consider using DocumentReference and FirebaseDocumentReferenceSerializer instead")
object FirebaseReferenceSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference>(
    isNullable = false
) {
    override fun serialize(encoder: Encoder, value: FirebaseReference) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): FirebaseReference = requireNotNull(decode(decoder))
}
