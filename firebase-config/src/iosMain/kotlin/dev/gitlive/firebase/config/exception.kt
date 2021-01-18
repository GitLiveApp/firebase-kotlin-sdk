package dev.gitlive.firebase.config

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorDomain
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorInternalError
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigErrorThrottled
import dev.gitlive.firebase.FirebaseException
import platform.Foundation.NSError

actual open class FirebaseRemoteConfigException(message: String) : FirebaseException(message)
actual class FirebaseRemoteConfigClientException(message: String) : FirebaseRemoteConfigException(message)
actual class FirebaseRemoteConfigFetchThrottledException(message: String) : FirebaseRemoteConfigException(message)
actual class FirebaseRemoteConfigServerException(message: String) : FirebaseRemoteConfigException(message)

fun NSError.toException(): FirebaseRemoteConfigException = when(domain) {
    FIRRemoteConfigErrorDomain -> when(code) {
        FIRRemoteConfigErrorInternalError -> FirebaseRemoteConfigServerException(toString())
        FIRRemoteConfigErrorThrottled -> FirebaseRemoteConfigFetchThrottledException(toString())
//        FIRRemoteConfigErrorUnknown -> ??
        else -> FirebaseRemoteConfigException(toString())
    }
    else -> FirebaseRemoteConfigException(toString())
}