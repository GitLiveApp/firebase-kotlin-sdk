/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.tasks

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Awaits the completion of the task without blocking a thread, returning its result or throwing its exception.
 *
 * This is the multiplatform equivalent of `kotlinx.coroutines.tasks.await` (which is Android/JVM only).
 */
public suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val e = exception
        return when {
            e != null -> throw e
            isCanceled -> throw CancellationException("Task $this was cancelled normally.")
            else -> result
        }
    }
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            val e = task.exception
            when {
                e != null -> continuation.resumeWithException(e)
                task.isCanceled -> continuation.cancel()
                else -> continuation.resume(task.result)
            }
        }
    }
}
