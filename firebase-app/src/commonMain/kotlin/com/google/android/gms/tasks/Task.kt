/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.tasks

/**
 * Represents an asynchronous operation, mirroring the Play Services Tasks API so that code written against the
 * Firebase Android SDK compiles unchanged on every platform.
 *
 * On Android and the JVM this is the real Play Services class; on Apple and JS platforms a Kotlin implementation
 * is provided. Listeners on the Kotlin implementation are invoked on the thread that completes the task.
 *
 * The static-only `Tasks` helper class is intentionally not mirrored; use `await()` from
 * `dev.gitlive.firebase.tasks` or a [TaskCompletionSource] instead.
 */
public expect abstract class Task<TResult>() {
    public abstract val isComplete: Boolean
    public abstract val isSuccessful: Boolean
    public abstract val isCanceled: Boolean
    public abstract val result: TResult
    public abstract val exception: Exception?
    public abstract fun addOnSuccessListener(listener: OnSuccessListener<in TResult>): Task<TResult>
    public abstract fun addOnFailureListener(listener: OnFailureListener): Task<TResult>
    public abstract fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult>
    public abstract fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult>
    public abstract fun <TContinuationResult> continueWith(continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult>
    public abstract fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult>
    public abstract fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult>
}

public expect fun interface OnCompleteListener<TResult> {
    public fun onComplete(task: Task<TResult>)
}

public expect fun interface OnSuccessListener<in TResult> {
    public fun onSuccess(result: TResult)
}

public expect fun interface OnFailureListener {
    public fun onFailure(e: Exception)
}

public expect fun interface OnCanceledListener {
    public fun onCanceled()
}

public expect fun interface Continuation<TResult, TContinuationResult> {
    public fun then(task: Task<TResult>): TContinuationResult
}

public expect fun interface SuccessContinuation<TResult, TContinuationResult> {
    public fun then(result: TResult): Task<TContinuationResult>
}

/** Provides a [Task] whose completion is controlled by the holder of the source. */
public expect class TaskCompletionSource<TResult>() {
    public val task: Task<TResult>
    public fun setResult(result: TResult?)
    public fun setException(e: Exception)
    public fun trySetResult(result: TResult?): Boolean
    public fun trySetException(e: Exception): Boolean
}

/** Thrown by [Task.result] when the task failed. */
public expect class RuntimeExecutionException(cause: Throwable) : RuntimeException
