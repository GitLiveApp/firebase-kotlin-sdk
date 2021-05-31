package dev.gitlive.firebase.remoteconfig

data class RemoteConfigInfo(
    val configSettings: RemoteConfigSettings,
    val fetchTimeMillis: Long,
    val lastFetchStatus: LastFetchStatus,
)

enum class LastFetchStatus { Success, Failure, Throttled, NoFetchYet }
