package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRGeoPoint

actual typealias GeoPoint = FIRGeoPoint

actual fun geoPointWith(latitude: Double, longitude: Double) = FIRGeoPoint(latitude, longitude)
actual val GeoPoint.latitude: Double get() = latitude
actual val GeoPoint.longitude: Double get() = longitude
