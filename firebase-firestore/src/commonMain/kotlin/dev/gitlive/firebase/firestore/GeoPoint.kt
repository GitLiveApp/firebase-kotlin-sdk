package dev.gitlive.firebase.firestore

/** A class representing a Firebase GeoPoint. */
expect class GeoPoint

expect fun geoPointWith(latitude: Double, longitude: Double): GeoPoint
expect val GeoPoint.latitude: Double
expect val GeoPoint.longitude: Double
