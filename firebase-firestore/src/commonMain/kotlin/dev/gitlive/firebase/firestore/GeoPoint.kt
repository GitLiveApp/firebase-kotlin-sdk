package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/** A class representing a platform specific Firebase GeoPoint. */
expect class NativeGeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
expect class GeoPoint internal constructor(nativeValue: NativeGeoPoint) {
    constructor(latitude: Double, longitude: Double)
    val latitude: Double
    val longitude: Double
    internal val nativeValue: NativeGeoPoint
}

/** Serializer for [GeoPoint]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object GeoPointSerializer : KSerializer<GeoPoint> by SpecialValueSerializer(
    serialName = "GeoPoint",
    toNativeValue = GeoPoint::nativeValue,
    fromNativeValue = { value ->
        when (value) {
            is NativeGeoPoint -> GeoPoint(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)
