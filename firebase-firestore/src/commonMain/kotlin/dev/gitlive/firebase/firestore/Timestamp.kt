package dev.gitlive.firebase.firestore

expect class Timestamp

expect fun timestampNow(): Timestamp
expect val Timestamp.seconds: Long
expect val Timestamp.nanoseconds: Int
