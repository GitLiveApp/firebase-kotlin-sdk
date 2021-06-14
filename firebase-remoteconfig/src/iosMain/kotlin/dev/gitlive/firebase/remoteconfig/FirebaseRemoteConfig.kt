package dev.gitlive.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorDomain
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorInternalError
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorThrottled
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigFetchAndActivateStatus
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigFetchStatus
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.app
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfig(FIRRemoteConfig.remoteConfig())

actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig =
    FirebaseRemoteConfig(FIRRemoteConfig.remoteConfigWithApp(Firebase.app.ios))

actual class FirebaseRemoteConfig internal constructor(val ios: FIRRemoteConfig) {
    actual val all: Map<String, FirebaseRemoteConfigValue>
        get() {
            return listOf(
                FIRRemoteConfigSource.FIRRemoteConfigSourceStatic,
                FIRRemoteConfigSource.FIRRemoteConfigSourceRemote,
                FIRRemoteConfigSource.FIRRemoteConfigSourceDefault,
            ).map { source ->
                val keys = ios.allKeysFromSource(source) as List<String>
                keys.map { it to FirebaseRemoteConfigValue(ios.configValueForKey(it, source)) }
            }.flatten().toMap()
        }

    actual val info: FirebaseRemoteConfigInfo
        get() {
            return FirebaseRemoteConfigInfo(
                configSettings = ios.configSettings.asCommon(),
                fetchTimeMillis = ios.lastFetchTime
                    ?.timeIntervalSince1970
                    ?.let { it.toLong() * 1000 }
                    ?.takeIf { it > 0 }
                    ?: -1L,
                lastFetchStatus = ios.lastFetchStatus.asCommon()
            )
        }

    actual suspend fun activate(): Boolean = ios.awaitResult { activateWithCompletion(it) }

    actual suspend fun ensureInitialized() =
        ios.await { ensureInitializedWithCompletionHandler(it) }

    actual suspend fun fetch(minimumFetchIntervalInSeconds: Long?) {
        val status: FIRRemoteConfigFetchStatus = if (minimumFetchIntervalInSeconds != null) {
            ios.awaitResult {
                fetchWithExpirationDuration(minimumFetchIntervalInSeconds.toDouble(), it)
            }
        } else {
            ios.awaitResult { fetchWithCompletionHandler(it) }
        }
    }

    actual suspend fun fetchAndActivate(): Boolean {
        val status: FIRRemoteConfigFetchAndActivateStatus = ios.awaitResult {
            fetchAndActivateWithCompletionHandler(it)
        }
        return status == FIRRemoteConfigFetchAndActivateStatus.FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote
    }

    actual fun getKeysByPrefix(prefix: String): Set<String> =
        all.keys.filter { it.startsWith(prefix) }.toSet()

    actual fun getValue(key: String): FirebaseRemoteConfigValue =
        FirebaseRemoteConfigValue(ios.configValueForKey(key))

    actual suspend fun reset() {
        // not implemented for iOS target
    }

    actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        val iosSettings = FIRRemoteConfigSettings().apply {
            minimumFetchInterval = settings.minimumFetchIntervalInSeconds.toDouble()
            fetchTimeout = settings.fetchTimeoutInSeconds.toDouble()
        }
        ios.setConfigSettings(iosSettings)
    }

    actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>) {
        ios.setDefaults(defaults.toMap())
    }

    private fun FIRRemoteConfigSettings.asCommon(): FirebaseRemoteConfigSettings {
        return FirebaseRemoteConfigSettings(
            fetchTimeoutInSeconds = fetchTimeout.toLong(),
            minimumFetchIntervalInSeconds = minimumFetchInterval.toLong(),
        )
    }

    private fun FIRRemoteConfigFetchStatus.asCommon(): FetchStatus {
        return when (this) {
            FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusSuccess -> FetchStatus.Success
            FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusNoFetchYet -> FetchStatus.NoFetchYet
            FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusFailure -> FetchStatus.Failure
            FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusThrottled -> FetchStatus.Throttled
        }
    }
}

private suspend inline fun <T, reified R> T.awaitResult(
    function: T.(callback: (R?, NSError?) -> Unit) -> Unit
): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as R
}

private suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
}


private fun NSError.toException() = when (domain) {
    FIRRemoteConfigErrorDomain -> {
        when (code) {
            FIRRemoteConfigErrorThrottled -> FirebaseRemoteConfigFetchThrottledException(
                localizedDescription
            )

            FIRRemoteConfigErrorInternalError -> FirebaseRemoteConfigServerException(
                localizedDescription
            )

            else -> FirebaseRemoteConfigClientException(localizedDescription)
        }
    }

    else -> FirebaseException(localizedDescription)
}


actual open class FirebaseRemoteConfigException(message: String) : FirebaseException(message)

actual class FirebaseRemoteConfigClientException(message: String) :
    FirebaseRemoteConfigException(message)

actual class FirebaseRemoteConfigFetchThrottledException(message: String) :
    FirebaseRemoteConfigException(message)

actual class FirebaseRemoteConfigServerException(message: String) :
    FirebaseRemoteConfigException(message)
