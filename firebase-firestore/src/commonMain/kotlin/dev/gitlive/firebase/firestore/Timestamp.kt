package dev.gitlive.firebase.firestore

expect class Timestamp

expect fun Timestamp.now(): Timestamp
expect val Timestamp.seconds: Long
expect val Timestamp.nanoseconds: Int
