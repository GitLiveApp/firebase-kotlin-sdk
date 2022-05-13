package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
actual typealias PlatformGeoPoint = com.google.firebase.firestore.GeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
actual class GeoPoint internal actual constructor(internal actual val platformValue: PlatformGeoPoint) {
    actual constructor(latitude: Double, longitude: Double) : this(PlatformGeoPoint(latitude, longitude))
    actual val latitude: Double = platformValue.latitude
    actual val longitude: Double = platformValue.longitude
    override fun equals(other: Any?): Boolean =
        this === other || other is GeoPoint && platformValue == other.platformValue
    override fun hashCode(): Int = platformValue.hashCode()
    override fun toString(): String = platformValue.toString()
}
