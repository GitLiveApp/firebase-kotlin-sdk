package dev.gitlive.firebase.remoteconfig

data class FirebaseRemoteConfigInfo(
    val configSettings: FirebaseRemoteConfigSettings,
    val fetchTimeMillis: Long,
    val lastFetchStatus: FetchStatus,
)

enum class FetchStatus { Success, Failure, Throttled, NoFetchYet }
