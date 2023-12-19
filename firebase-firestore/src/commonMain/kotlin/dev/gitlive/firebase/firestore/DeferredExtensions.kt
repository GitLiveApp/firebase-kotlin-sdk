package dev.gitlive.firebase.firestore

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

@PublishedApi
internal fun <T, R> Deferred<T>.convert(converter: (T) -> R): Deferred<R> {
    val deferred = CompletableDeferred<R>()
    invokeOnCompletion { exception ->
        if (exception == null) {
            deferred.complete(converter(getCompleted()))
        } else {
            deferred.completeExceptionally(exception)
        }
    }
    return deferred
}
