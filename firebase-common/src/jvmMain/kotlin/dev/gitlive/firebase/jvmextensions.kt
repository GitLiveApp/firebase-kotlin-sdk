package dev.gitlive.firebase

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resumeWithException

suspend fun <T> ApiFuture<T>.await(dispatcher: CoroutineDispatcher = Dispatchers.Default): T {
    try {
        if (isDone) {
            return get() as T
        }
    } catch (e: ExecutionException) {
        throw e.cause ?: e // unwrap original cause from ExecutionException
    }

    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        val callback = object: ApiFutureCallback<T> {
            override fun onFailure(t: Throwable?) {
                cont.resumeWithException(t ?: error("Error was null."))
            }

            override fun onSuccess(result: T?) {
                cont.resume(result!!) { cancel(true) }
            }
        }
        ApiFutures.addCallback(this, callback, dispatcher.asExecutor())
        cont.invokeOnCancellation {
            cancel(false)
        }
    }
}

fun <T> ApiFuture<T>.asDeferred(dispatcher: CoroutineDispatcher = Dispatchers.Default): Deferred<T> {
    val deferred = CompletableDeferred<T>()
    if(isDone) {
        if(isCancelled) deferred.cancel()
        else deferred.complete(get())
    } else {
        val callback = object: ApiFutureCallback<T> {
            override fun onSuccess(result: T) {
                deferred.complete(result)
            }

            override fun onFailure(t: Throwable?) {
                deferred.completeExceptionally(t ?: error("Error was null."))
            }
        }

        ApiFutures.addCallback(this, callback, dispatcher.asExecutor())
    }

    return object : Deferred<T> by deferred {}
}