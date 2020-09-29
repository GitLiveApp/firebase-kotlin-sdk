package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

@Serializable
expect class Timestamp

expect fun timestampNow(): Timestamp
expect val Timestamp.seconds: Long
expect val Timestamp.nanoseconds: Int
