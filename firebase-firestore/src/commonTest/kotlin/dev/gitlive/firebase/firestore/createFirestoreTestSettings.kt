package dev.gitlive.firebase.firestore

expect fun createFirestoreTestSettings(
    sslEnabled: Boolean? = null,
    host: String? = null,
    cacheSettings: LocalCacheSettings? = null
): FirebaseFirestore.Settings
