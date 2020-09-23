package dev.gitlive.firebase.firestore

actual typealias Timestamp = com.google.firebase.Timestamp

actual fun Timestamp.now(): Timestamp = Timestamp.now()
actual val Timestamp.seconds: Long get() = seconds
actual val Timestamp.nanoseconds: Int get() = nanoseconds
