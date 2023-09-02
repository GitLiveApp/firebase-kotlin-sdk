package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
actual typealias NativeGeoPoint = com.google.firebase.firestore.GeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
actual class GeoPoint internal actual constructor(internal actual val nativeValue: NativeGeoPoint) {
    actual constructor(latitude: Double, longitude: Double) : this(NativeGeoPoint(latitude, longitude))
    actual val latitude: Double = nativeValue.latitude
    actual val longitude: Double = nativeValue.longitude

    override fun equals(other: Any?): Boolean =
        this === other || other is GeoPoint && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}
