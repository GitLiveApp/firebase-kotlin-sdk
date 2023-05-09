package dev.gitlive.firebase.database

expect fun createFirebaseDatabaseTestSettings(
    persistenceEnabled: Boolean = false,
    persistenceCacheSizeBytes: Long? = null,
): FirebaseDatabase.Settings
