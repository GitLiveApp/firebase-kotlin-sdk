package dev.gitlive.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

public val FirebaseMessaging.ios: FIRMessaging get() = FIRMessaging.messaging()

public actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(FIRMessaging.messaging())

public actual class FirebaseMessaging(internal val ios: FIRMessaging) {
    public actual fun subscribeToTopic(topic: String) {
        ios.subscribeToTopic(topic)
    }

    public actual fun unsubscribeFromTopic(topic: String) {
        ios.unsubscribeFromTopic(topic)
    }

    public actual suspend fun getToken(): String = awaitResult { ios.tokenWithCompletion(it) }

    public actual suspend fun deleteToken() {
        await { ios.deleteTokenWithCompletion(it) }
    }
}

public suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(Exception(error.toString()))
        }
    }
    job.await()
}

public suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(Exception(error.toString()))
        }
    }
    return job.await() as R
}
