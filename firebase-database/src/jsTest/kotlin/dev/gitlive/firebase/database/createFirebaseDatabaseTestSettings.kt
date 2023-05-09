package dev.gitlive.firebase.database

actual fun createFirebaseDatabaseTestSettings(
    persistenceEnabled: Boolean,
    persistenceCacheSizeBytes: Long?,
) = FirebaseDatabase.Settings(persistenceEnabled, persistenceCacheSizeBytes)
