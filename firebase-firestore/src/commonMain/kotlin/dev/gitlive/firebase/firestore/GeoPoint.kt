package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint; the Android-SDK-shaped [com.google.firebase.firestore.GeoPoint]. */
public typealias NativeGeoPoint = com.google.firebase.firestore.GeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
public class GeoPoint internal constructor(internal val nativeValue: NativeGeoPoint) {
    public constructor(latitude: Double, longitude: Double) : this(NativeGeoPoint(latitude, longitude))

    /** The Android-SDK-shaped [com.google.firebase.firestore.GeoPoint] this wraps. */
    public val compat: NativeGeoPoint get() = nativeValue

    public val latitude: Double get() = nativeValue.latitude
    public val longitude: Double get() = nativeValue.longitude

    override fun equals(other: Any?): Boolean = this === other || other is GeoPoint && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}
