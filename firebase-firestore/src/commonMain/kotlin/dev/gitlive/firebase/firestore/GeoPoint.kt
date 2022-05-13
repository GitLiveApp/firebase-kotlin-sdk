package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
expect class PlatformGeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = FirebaseGeoPointSerializer::class)
expect class GeoPoint internal constructor(platformValue: PlatformGeoPoint) {
    constructor(latitude: Double, longitude: Double)
    val latitude: Double
    val longitude: Double
    internal val platformValue: PlatformGeoPoint
}
