package dev.gitlive.firebase.database

import platform.darwin.DISPATCH_QUEUE_CONCURRENT
import platform.darwin.dispatch_queue_create

private val firebaseDatabaseTestQueue = dispatch_queue_create("FirebaseDatabaseQueue", DISPATCH_QUEUE_CONCURRENT)

actual fun createFirebaseDatabaseTestSettings(
    persistenceEnabled: Boolean,
    persistenceCacheSizeBytes: Long?,
) = FirebaseDatabase.Settings(persistenceEnabled, persistenceCacheSizeBytes, firebaseDatabaseTestQueue)
