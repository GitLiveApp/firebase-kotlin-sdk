/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.tasks

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException

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
}

public actual class TaskCompletionSource<TResult> actual constructor() {
    private val impl = TaskImpl<TResult>()

    public actual val task: Task<TResult> get() = impl

    public actual fun setResult(result: TResult?) {
        check(impl.trySetResult(result)) { "Task is already complete" }
    }

    public actual fun setException(e: Exception) {
        check(impl.trySetException(e)) { "Task is already complete" }
    }

    public actual fun trySetResult(result: TResult?): Boolean = impl.trySetResult(result)

    public actual fun trySetException(e: Exception): Boolean = impl.trySetException(e)
}

/** Pure Kotlin [Task] used on platforms without Play Services. Thread-safe; listeners run on the completing thread. */
@OptIn(ExperimentalAtomicApi::class)
internal class TaskImpl<TResult> : Task<TResult>() {

    private val state = AtomicReference<TaskState<TResult>>(TaskState.Pending(emptyList()))

    override val isComplete: Boolean get() = state.load() !is TaskState.Pending
    override val isSuccessful: Boolean get() = state.load() is TaskState.Success
    override val isCanceled: Boolean get() = state.load() is TaskState.Canceled

    @Suppress("UNCHECKED_CAST")
    override val result: TResult
        get() = when (val current = state.load()) {
            is TaskState.Success -> current.value as TResult
            is TaskState.Failure -> throw RuntimeExecutionException(current.exception)
            is TaskState.Canceled -> throw CancellationException("Task is already canceled")
            is TaskState.Pending -> throw IllegalStateException("Task is not yet complete")
        }

    override val exception: Exception? get() = (state.load() as? TaskState.Failure)?.exception

    fun trySetResult(value: TResult?): Boolean = complete(TaskState.Success(value))
    fun trySetException(exception: Exception): Boolean = complete(TaskState.Failure(exception))
    fun tryCancel(): Boolean = complete(TaskState.Canceled())

    private fun complete(newState: TaskState<TResult>): Boolean {
        while (true) {
            val current = state.load()
            if (current !is TaskState.Pending) return false
            if (state.compareAndSet(current, newState)) {
                current.listeners.forEach { it(this) }
                return true
            }
        }
    }

    private fun addListener(listener: (Task<TResult>) -> Unit): Task<TResult> {
        while (true) {
            val current = state.load()
            if (current !is TaskState.Pending) {
                listener(this)
                return this
            }
            if (state.compareAndSet(current, TaskState.Pending(current.listeners + listener))) return this
        }
    }

    override fun addOnSuccessListener(listener: OnSuccessListener<TResult>): Task<TResult> = addListener { task -> if (task.isSuccessful) listener.onSuccess(task.result) }

    override fun addOnFailureListener(listener: OnFailureListener): Task<TResult> = addListener { task -> task.exception?.let(listener::onFailure) }

    override fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult> = addListener { task -> listener.onComplete(task) }

    override fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult> = addListener { task -> if (task.isCanceled) listener.onCanceled() }

    override fun <TContinuationResult> continueWith(continuation: Continuation<TResult, TContinuationResult>): Task<TContinuationResult> {
        val next = TaskImpl<TContinuationResult>()
        addOnCompleteListener { task -> next.completeWith { continuation.then(task) } }
        return next
    }

    override fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult> {
        val next = TaskImpl<TContinuationResult>()
        addOnCompleteListener { task -> next.completeWithTask { continuation.then(task) } }
        return next
    }

    override fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<TResult, TContinuationResult>): Task<TContinuationResult> {
        val next = TaskImpl<TContinuationResult>()
        addOnCompleteListener { task ->
            val e = task.exception
            when {
                e != null -> next.trySetException(e)
                task.isCanceled -> next.tryCancel()
                else -> next.completeWithTask { successContinuation.then(task.result) }
            }
        }
        return next
    }

    private inline fun completeWith(block: () -> TResult) {
        try {
            trySetResult(block())
        } catch (e: RuntimeExecutionException) {
            trySetException(e.cause as? Exception ?: e)
        } catch (e: CancellationException) {
            tryCancel()
        } catch (e: Exception) {
            trySetException(e)
        }
    }

    private inline fun completeWithTask(block: () -> Task<TResult>) {
        val task = try {
            block()
        } catch (e: RuntimeExecutionException) {
            trySetException(e.cause as? Exception ?: e)
            return
        } catch (e: CancellationException) {
            tryCancel()
            return
        } catch (e: Exception) {
            trySetException(e)
            return
        }
        task.addOnCompleteListener { completed ->
            val e = completed.exception
            when {
                e != null -> trySetException(e)
                completed.isCanceled -> tryCancel()
                else -> trySetResult(completed.result)
            }
        }
    }
}

private sealed class TaskState<T> {
    class Pending<T>(val listeners: List<(Task<T>) -> Unit>) : TaskState<T>()
    class Success<T>(val value: T?) : TaskState<T>()
    class Failure<T>(val exception: Exception) : TaskState<T>()
    class Canceled<T> : TaskState<T>()
}
