package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
public expect class NativeGeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
public expect class GeoPoint internal constructor(nativeValue: NativeGeoPoint) {
    public constructor(latitude: Double, longitude: Double)
    public val latitude: Double
    public val longitude: Double
    internal val nativeValue: NativeGeoPoint
}
