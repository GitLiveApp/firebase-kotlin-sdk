package dev.gitlive.firebase.firestore

actual typealias GeoPoint = com.google.firebase.firestore.GeoPoint

actual fun geoPointWith(latitude: Double, longitude: Double) = GeoPoint(latitude, longitude)
actual val GeoPoint.latitude: Double get() = latitude
actual val GeoPoint.longitude: Double get() = longitude
