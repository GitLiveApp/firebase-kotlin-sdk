package dev.gitlive.firebase.firestore

expect class GeoPoint

expect fun geoPointWith(latitude: Double, longitude: Double): GeoPoint
expect val GeoPoint.latitude: Double
expect val GeoPoint.longitude: Double
