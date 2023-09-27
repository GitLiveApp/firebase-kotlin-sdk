package dev.gitlive.firebase.firestore

actual fun createFirestoreTestSettings(
    sslEnabled: Boolean?,
    host: String?,
    cacheSettings: LocalCacheSettings?
) = FirebaseFirestore.Settings(
    sslEnabled, host, cacheSettings
)
