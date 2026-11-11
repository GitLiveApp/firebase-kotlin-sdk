/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.tasks

import dev.gitlive.firebase.GET_RESULT_CLASS_JVM_ONLY
import dev.gitlive.firebase.TASK_CONTINUATION_EXECUTOR_JVM_ONLY
import dev.gitlive.firebase.TASK_LISTENER_ACTIVITY_ANDROID_ONLY
import dev.gitlive.firebase.TASK_LISTENER_EXECUTOR_JVM_ONLY

/**
 * Represents an asynchronous operation, mirroring the Play Services Tasks API so that code written against the
 * Firebase Android SDK compiles unchanged on every platform.
 *
 * On Android and the JVM this is the real Play Services class; on Apple, JS and wasmJs platforms a Kotlin implementation
 * is provided. Listeners on the Kotlin implementation are invoked on the thread that completes the task.
 *
 * The static-only `Tasks` helper class is intentionally not mirrored; use `kotlinx.coroutines.tasks.await`
 * or a [TaskCompletionSource] instead.
 *
 * The `Executor` and `Activity` overloads of the listeners and continuations keep the SDK's signatures through the
 * error-deprecated `java.util.concurrent.Executor` and `android.app.Activity` stand-ins and can only be called from
 * Android/JVM code: from common code they fail to compile with a message naming the plain overload. Every `Task`
 * subclass (`LoadBundleTask`, `StorageTask`, ...) inherits them.
 */
public expect abstract class Task<TResult>() {
    public abstract val isComplete: Boolean
    public abstract val isSuccessful: Boolean
    public abstract val isCanceled: Boolean
    public abstract val result: TResult
    public abstract val exception: Exception?
    public abstract fun addOnSuccessListener(listener: OnSuccessListener<TResult>): Task<TResult>
    public abstract fun addOnFailureListener(listener: OnFailureListener): Task<TResult>
    public abstract fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult>
    public abstract fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult>
    public abstract fun <TContinuationResult> continueWith(continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult>
    public abstract fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult>
    public abstract fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult>

    /** Android/JVM only: the result, rethrowing a failure of type [exceptionType] as is; use [result] and [exception] in common code. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(GET_RESULT_CLASS_JVM_ONLY, ReplaceWith("result"), DeprecationLevel.ERROR)
    public fun <X : Throwable> getResult(exceptionType: java.lang.Class<X>): TResult

    /** JVM only: [addOnSuccessListener] called on [executor]; the multiplatform listener is called on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnSuccessListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnSuccessListener(executor: java.util.concurrent.Executor, listener: OnSuccessListener<TResult>): Task<TResult>

    /** Android only: [addOnSuccessListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnSuccessListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnSuccessListener(activity: android.app.Activity, listener: OnSuccessListener<TResult>): Task<TResult>

    /** JVM only: [addOnFailureListener] called on [executor]; the multiplatform listener is called on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnFailureListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnFailureListener(executor: java.util.concurrent.Executor, listener: OnFailureListener): Task<TResult>

    /** Android only: [addOnFailureListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnFailureListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnFailureListener(activity: android.app.Activity, listener: OnFailureListener): Task<TResult>

    /** JVM only: [addOnCompleteListener] called on [executor]; the multiplatform listener is called on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnCompleteListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnCompleteListener(executor: java.util.concurrent.Executor, listener: OnCompleteListener<TResult>): Task<TResult>

    /** Android only: [addOnCompleteListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnCompleteListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnCompleteListener(activity: android.app.Activity, listener: OnCompleteListener<TResult>): Task<TResult>

    /** JVM only: [addOnCanceledListener] called on [executor]; the multiplatform listener is called on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnCanceledListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnCanceledListener(executor: java.util.concurrent.Executor, listener: OnCanceledListener): Task<TResult>

    /** Android only: [addOnCanceledListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnCanceledListener(listener)"), DeprecationLevel.ERROR)
    public open fun addOnCanceledListener(activity: android.app.Activity, listener: OnCanceledListener): Task<TResult>

    /** JVM only: [continueWith] run on [executor]; the multiplatform continuation runs on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("continueWith(continuation)"), DeprecationLevel.ERROR)
    public open fun <TContinuationResult> continueWith(executor: java.util.concurrent.Executor, continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult>

    /** JVM only: [continueWithTask] run on [executor]; the multiplatform continuation runs on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("continueWithTask(continuation)"), DeprecationLevel.ERROR)
    public open fun <TContinuationResult> continueWithTask(executor: java.util.concurrent.Executor, continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult>

    /** JVM only: [onSuccessTask] run on [executor]; the multiplatform continuation runs on the completing thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("onSuccessTask(successContinuation)"), DeprecationLevel.ERROR)
    public open fun <TContinuationResult> onSuccessTask(executor: java.util.concurrent.Executor, successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult>
}

public fun interface OnCompleteListener<TResult> {
    public fun onComplete(task: Task<TResult>)
}

public fun interface OnSuccessListener<in TResult> {
    public fun onSuccess(result: TResult)
}

public fun interface OnFailureListener {
    public fun onFailure(e: Exception)
}

public fun interface OnCanceledListener {
    public fun onCanceled()
}

public fun interface Continuation<TResult, TContinuationResult> {
    public fun then(task: Task<TResult>): TContinuationResult
}

public fun interface SuccessContinuation<TResult, TContinuationResult> {
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
public class RuntimeExecutionException(cause: Throwable) : RuntimeException(cause)
