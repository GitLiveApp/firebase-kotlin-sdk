/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.tasks

import dev.gitlive.firebase.stub
import dev.gitlive.firebase.GET_RESULT_CLASS_JVM_ONLY
import dev.gitlive.firebase.TASK_CONTINUATION_EXECUTOR_JVM_ONLY
import dev.gitlive.firebase.TASK_LISTENER_ACTIVITY_ANDROID_ONLY
import dev.gitlive.firebase.TASK_LISTENER_EXECUTOR_JVM_ONLY

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
    public actual abstract fun addOnSuccessListener(listener: OnSuccessListener<TResult>): Task<TResult>
    public actual abstract fun addOnFailureListener(listener: OnFailureListener): Task<TResult>
    public actual abstract fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult>
    public actual abstract fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult>
    public actual abstract fun <TContinuationResult> continueWith(continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult>
    public actual abstract fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult>
    public actual abstract fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult>

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(GET_RESULT_CLASS_JVM_ONLY, ReplaceWith("result"), DeprecationLevel.ERROR)
    public actual fun <X : Throwable> getResult(exceptionType: java.lang.Class<X>): TResult = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnSuccessListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnSuccessListener(executor: java.util.concurrent.Executor, listener: OnSuccessListener<TResult>): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnSuccessListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnSuccessListener(activity: android.app.Activity, listener: OnSuccessListener<TResult>): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnFailureListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnFailureListener(executor: java.util.concurrent.Executor, listener: OnFailureListener): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnFailureListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnFailureListener(activity: android.app.Activity, listener: OnFailureListener): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnCompleteListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnCompleteListener(executor: java.util.concurrent.Executor, listener: OnCompleteListener<TResult>): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnCompleteListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnCompleteListener(activity: android.app.Activity, listener: OnCompleteListener<TResult>): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addOnCanceledListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnCanceledListener(executor: java.util.concurrent.Executor, listener: OnCanceledListener): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addOnCanceledListener(listener)"), DeprecationLevel.ERROR)
    public actual open fun addOnCanceledListener(activity: android.app.Activity, listener: OnCanceledListener): Task<TResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("continueWith(continuation)"), DeprecationLevel.ERROR)
    public actual open fun <TContinuationResult> continueWith(executor: java.util.concurrent.Executor, continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("continueWithTask(continuation)"), DeprecationLevel.ERROR)
    public actual open fun <TContinuationResult> continueWithTask(executor: java.util.concurrent.Executor, continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult> = stub()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TASK_CONTINUATION_EXECUTOR_JVM_ONLY, ReplaceWith("onSuccessTask(successContinuation)"), DeprecationLevel.ERROR)
    public actual open fun <TContinuationResult> onSuccessTask(executor: java.util.concurrent.Executor, successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult> = stub()
}

public actual class TaskCompletionSource<TResult> actual constructor() {
    public actual val task: Task<TResult> get() = stub()
    public actual fun setResult(result: TResult?): Unit = stub()
    public actual fun setException(e: Exception): Unit = stub()
    public actual fun trySetResult(result: TResult?): Boolean = stub()
    public actual fun trySetException(e: Exception): Boolean = stub()
}
