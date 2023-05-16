package dev.gitlive.firebase.firestore

import platform.darwin.DISPATCH_QUEUE_CONCURRENT
import platform.darwin.dispatch_queue_create

private val firestoreTestQueue = dispatch_queue_create("FirestoreQueue", DISPATCH_QUEUE_CONCURRENT)

actual fun createFirestoreTestSettings(
    sslEnabled: Boolean?,
    host: String?,
    cacheSettings: LocalCacheSettings?
) = FirebaseFirestore.Settings(
    sslEnabled, host, cacheSettings, firestoreTestQueue
)
