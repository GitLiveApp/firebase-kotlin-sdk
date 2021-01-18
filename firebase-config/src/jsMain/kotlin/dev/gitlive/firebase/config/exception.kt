package dev.gitlive.firebase.config

import dev.gitlive.firebase.FirebaseException

actual open class FirebaseRemoteConfigException(code: String?, cause: Throwable) : FirebaseException(code, cause)
actual class FirebaseRemoteConfigClientException(message: String, cause: Throwable) : FirebaseRemoteConfigException(message, cause)
actual class FirebaseRemoteConfigFetchThrottledException(message: String, cause: Throwable) : FirebaseRemoteConfigException(message, cause)
actual class FirebaseRemoteConfigServerException(message: String, cause: Throwable) : FirebaseRemoteConfigException(message, cause)

internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.config.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw Throwable(e.toString())
    }
}

//private fun errorToException(cause: dynamic) = when(val code = cause.code?.toString()?.toLowerCase()) {
//    ""
//}
