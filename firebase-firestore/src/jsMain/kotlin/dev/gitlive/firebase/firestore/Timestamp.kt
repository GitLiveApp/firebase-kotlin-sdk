package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*

actual typealias Timestamp = firebase.firestore.Timestamp

actual fun Timestamp.now(): Timestamp = Timestamp.now()
actual val Timestamp.seconds: Long get() = seconds
actual val Timestamp.nanoseconds: Int get() = nanoseconds
