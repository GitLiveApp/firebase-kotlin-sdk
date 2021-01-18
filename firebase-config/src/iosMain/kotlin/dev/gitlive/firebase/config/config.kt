package dev.gitlive.firebase.config

import cocoapods.FirebaseRemoteConfig.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy
import kotlin.coroutines.coroutineContext
import kotlin.native.concurrent.freeze
import kotlin.collections.toSet

actual val Firebase.config by lazy {
    FirebaseRemoteConfig(FIRRemoteConfig.remoteConfig())
}

actual fun Firebase.config(app: FirebaseApp) =
    FirebaseRemoteConfig(FIRRemoteConfig.remoteConfigWithApp(app.ios))

actual class FirebaseRemoteConfig internal constructor(private val ios: FIRRemoteConfig) {
    actual val configSettings: FirebaseRemoteConfigSettings
        get() = ios.configSettings.run {
            FirebaseRemoteConfigSettings(fetchTimeout.toLong(), minimumFetchInterval.toLong())
        }

    actual suspend fun fetch() {
        awaitResult<FIRRemoteConfigFetchStatus> { ios.fetchWithCompletionHandler(it) }
    }

    actual suspend  fun fetch(expiration: Long) {
        awaitResult<FIRRemoteConfigFetchStatus> {
            ios.fetchWithExpirationDuration(expiration.toDouble(), it)
        }
    }

    actual suspend  fun fetchAndActivate(): Boolean =
        awaitResult<FIRRemoteConfigFetchAndActivateStatus> {
            ios.fetchAndActivateWithCompletionHandler(it.freeze())
        }.run {
            when (this) {
                FIRRemoteConfigFetchAndActivateStatus
                    .FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote -> true
                FIRRemoteConfigFetchAndActivateStatus
                    .FIRRemoteConfigFetchAndActivateStatusSuccessUsingPreFetchedData -> true
                FIRRemoteConfigFetchAndActivateStatus
                    .FIRRemoteConfigFetchAndActivateStatusError -> false
            }
        }

    actual suspend  fun activate(): Boolean =
        ios.activateFetched()

    actual suspend  fun ensureInitialized(): FirebaseRemoteConfigInfo =
        await { ios.ensureInitializedWithCompletionHandler(it.freeze()) }.run {
            FirebaseRemoteConfigInfo(
                configSettings,
                ios.lastFetchTime!!.timeIntervalSince1970.toLong(),
                when (ios.lastFetchStatus) {
                    FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusNoFetchYet ->
                        FirebaseRemoteConfigFetchStatus.NO_FETCH_YET
                    FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusSuccess ->
                        FirebaseRemoteConfigFetchStatus.SUCCESS
                    FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusFailure ->
                        FirebaseRemoteConfigFetchStatus.FAILURE
                    FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusThrottled ->
                        FirebaseRemoteConfigFetchStatus.THROTTLED
                }
            )
        }

    actual suspend  fun setDefaults(defaults: Config) {
        ios.setDefaults(defaults)
    }

    @ExperimentalUnsignedTypes
    actual fun getValue(key: String): FirebaseRemoteConfigValue =
        ios.configValueForKey(key).run {
            FirebaseRemoteConfigValue(
                key,
                when(source) {
                    FIRRemoteConfigSource.FIRRemoteConfigSourceRemote ->
                        FirebaseRemoteConfigSource.REMOTE
                    FIRRemoteConfigSource.FIRRemoteConfigSourceDefault ->
                        FirebaseRemoteConfigSource.DEFAULT
                    FIRRemoteConfigSource.FIRRemoteConfigSourceStatic ->
                        FirebaseRemoteConfigSource.STATIC
                },
                { boolValue },
                { dataValue.toByteArray() },
                { numberValue?.doubleValue },
                { numberValue?.longValue },
                { stringValue }
            )
        }

    @ExperimentalUnsignedTypes
    actual suspend fun getAll(source: FirebaseRemoteConfigSource): List<FirebaseRemoteConfigValue> =
        ios.allKeysFromSource(when(source) {
            FirebaseRemoteConfigSource.DEFAULT -> FIRRemoteConfigSource.FIRRemoteConfigSourceDefault
            FirebaseRemoteConfigSource.REMOTE -> FIRRemoteConfigSource.FIRRemoteConfigSourceRemote
            FirebaseRemoteConfigSource.STATIC -> FIRRemoteConfigSource.FIRRemoteConfigSourceStatic
        }).map { getValue(it as String) }


    actual fun getKeysByPrefix(prefix: String?): Set<Any?> =
        ios.keysWithPrefix(prefix).toSet()

    actual suspend fun setConfigs(config: FirebaseRemoteConfigSettings) {
        ios.setConfigSettings(FIRRemoteConfigSettings().apply {
            minimumFetchInterval = config.minimumFetchInterval?.toDouble()
                ?: ios.configSettings.minimumFetchInterval
            fetchTimeout = config.fetchTimeout?.toDouble() ?: ios.configSettings.fetchTimeout
        })
    }

}

suspend inline fun <reified T> awaitResult(
    function: (callback: (T?, NSError?) -> Unit) -> Unit
): T {
    val job = CompletableDeferred<T?>()
    function { result, error ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(Throwable(error.localizedDescription))
        }
    }
    return job.await() as T
}

suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val result = function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }

    job.await()
    return result
}

@ExperimentalUnsignedTypes
fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), bytes, length)
    }
}
