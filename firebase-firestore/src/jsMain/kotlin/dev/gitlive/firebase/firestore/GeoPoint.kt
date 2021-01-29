package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*

actual typealias GeoPoint = firebase.firestore.GeoPoint

actual fun geoPointWith(latitude: Double, longitude: Double) = GeoPoint(latitude, longitude)
actual val GeoPoint.latitude: Double get() = latitude
actual val GeoPoint.longitude: Double get() = longitude
