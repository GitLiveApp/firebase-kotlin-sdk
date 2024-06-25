package dev.gitlive.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(FIRMessaging.messaging())

actual class FirebaseMessaging(val ios: FIRMessaging) {
    actual fun subscribeToTopic(topic: String) {
        ios.subscribeToTopic(topic)
    }

    actual fun unsubscribeFromTopic(topic: String) {
        ios.unsubscribeFromTopic(topic)
    }

    actual suspend fun getToken(): String = awaitResult { ios.tokenWithCompletion(it) }
}

suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(Exception(error.toString()))
        }
    }
    job.await()
}

suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(Exception(error.toString()))
        }
    }
    return job.await() as R
}