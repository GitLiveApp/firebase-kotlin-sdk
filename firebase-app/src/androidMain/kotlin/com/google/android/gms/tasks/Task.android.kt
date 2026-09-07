/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.tasks

/*
 * Header stubs for the Play Services Tasks API.
 *
 * Android and the JVM already have these classes (from play-services-tasks and firebase-java-sdk respectively) and
 * consumers get them from Play Services too, so they must not be shipped by this library. These declarations only
 * satisfy the `expect` declarations at compile time; the build deletes them from the compilation output
 * (see buildSrc utils/HeaderStubs.kt), so everything binds to the real classes. Only members that exist on the real
 * classes with the same JVM signature may be declared here (verified by the build).
 */

public actual abstract class Task<TResult> actual constructor() {
    public actual abstract val isComplete: Boolean
    public actual abstract val isSuccessful: Boolean
    public actual abstract val isCanceled: Boolean
    public actual abstract val result: TResult
    public actual abstract val exception: Exception?
    public actual abstract fun addOnSuccessListener(listener: OnSuccessListener<in TResult>): Task<TResult>
    public actual abstract fun addOnFailureListener(listener: OnFailureListener): Task<TResult>
    public actual abstract fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult>
    public actual abstract fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult>
    public actual abstract fun <TContinuationResult> continueWith(continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult>
    public actual abstract fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult>
    public actual abstract fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult>
}

public actual fun interface OnCompleteListener<TResult> {
    public actual fun onComplete(task: Task<TResult>)
}

public actual fun interface OnSuccessListener<in TResult> {
    public actual fun onSuccess(result: TResult)
}

public actual fun interface OnFailureListener {
    public actual fun onFailure(e: Exception)
}

public actual fun interface OnCanceledListener {
    public actual fun onCanceled()
}

public actual fun interface Continuation<TResult, TContinuationResult> {
    public actual fun then(task: Task<TResult>): TContinuationResult
}

public actual fun interface SuccessContinuation<TResult, TContinuationResult> {
    public actual fun then(result: TResult): Task<TContinuationResult>
}

public actual class TaskCompletionSource<TResult> actual constructor() {
    public actual val task: Task<TResult> get() = stub()
    public actual fun setResult(result: TResult?): Unit = stub()
    public actual fun setException(e: Exception): Unit = stub()
    public actual fun trySetResult(result: TResult?): Boolean = stub()
    public actual fun trySetException(e: Exception): Boolean = stub()
}

public actual class RuntimeExecutionException actual constructor(cause: Throwable) : RuntimeException(cause)

private fun stub(): Nothing = throw UnsupportedOperationException("Header stub; the real Play Services class is used at runtime")
