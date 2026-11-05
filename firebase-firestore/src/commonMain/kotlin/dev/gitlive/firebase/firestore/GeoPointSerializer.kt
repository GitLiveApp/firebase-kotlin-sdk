package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.internal.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/** Serializer for [GeoPoint]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
public object GeoPointSerializer : KSerializer<GeoPoint> by SpecialValueSerializer(
    serialName = "GeoPoint",
    toNativeValue = GeoPoint::nativeValue,
    fromNativeValue = { raw ->
        when (val value = raw?.toCompatSpecialValue()) {
            is NativeGeoPoint -> GeoPoint(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
)
