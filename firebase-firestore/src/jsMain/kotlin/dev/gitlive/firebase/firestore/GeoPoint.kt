package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
actual typealias PlatformGeoPoint = firebase.firestore.GeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
actual class GeoPoint internal actual constructor(internal actual val platformValue: PlatformGeoPoint) {
    actual constructor(latitude: Double, longitude: Double) : this(PlatformGeoPoint(latitude, longitude))
    actual val latitude: Double = platformValue.latitude
    actual val longitude: Double = platformValue.longitude

    override fun equals(other: Any?): Boolean =
        this === other || other is GeoPoint && platformValue.isEqual(other.platformValue)
    override fun hashCode(): Int = platformValue.hashCode()
    override fun toString(): String = "GeoPoint[lat=$latitude,long=$longitude]"
}
