package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

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
