package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRTimestamp

actual typealias Timestamp = FIRTimestamp

actual fun Timestamp.now(): Timestamp = FIRTimestamp.timestamp()
actual val Timestamp.seconds: Long get() = seconds
actual val Timestamp.nanoseconds: Int get() = nanoseconds
