/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import kotlin.coroutines.cancellation.CancellationException

/*
 * The storage task hierarchy of the Android SDK, implemented over the Apple and JS SDKs' upload and download tasks:
 * the platform reports progress, pauses, completion, failure and cancellation through the report* functions, and
 * pause/resume/cancel are forwarded to the platform task through a controller.
 */

public actual abstract class CancellableTask<StateT> actual constructor() : Task<StateT>() {
    public actual abstract fun cancel(): Boolean
    public actual abstract val isInProgress: Boolean
    public actual abstract fun addOnProgressListener(listener: OnProgressListener<StateT>): CancellableTask<StateT>
}

public actual abstract class ControllableTask<StateT> actual constructor() : CancellableTask<StateT>() {
    public actual abstract val isPaused: Boolean
    public actual abstract fun pause(): Boolean
    public actual abstract fun resume(): Boolean
    public actual abstract fun addOnPausedListener(listener: OnPausedListener<StateT>): ControllableTask<StateT>
}

private enum class TaskStatus { QUEUED, IN_PROGRESS, PAUSED, SUCCESS, FAILURE, CANCELED }

public actual abstract class StorageTask<ResultT : StorageTask.ProvideError> protected actual constructor() : ControllableTask<ResultT>() {
    private var status = TaskStatus.QUEUED
    private var error: Exception? = null
    private var finalSnapshot: ResultT? = null
    private val canceledListeners = mutableListOf<OnCanceledListener>()
    private val completeListeners = mutableListOf<OnCompleteListener<ResultT>>()
    private val failureListeners = mutableListOf<OnFailureListener>()
    private val pausedListeners = mutableListOf<OnPausedListener<ResultT>>()
    private val progressListeners = mutableListOf<OnProgressListener<ResultT>>()
    private val successListeners = mutableListOf<OnSuccessListener<in ResultT>>()

    /** The reference the task operates on. */
    internal abstract val reference: StorageReference

    /** A snapshot of the platform task's current state, carrying [error]. */
    internal abstract fun snapshotWith(error: Exception?): ResultT

    internal abstract fun controlPause(): Boolean

    internal abstract fun controlResume(): Boolean

    internal abstract fun controlCancel(): Boolean

    public actual val snapshot: ResultT get() = finalSnapshot ?: snapshotWith(error)

    // The Task members are implemented by the concrete subclasses (the expect classes leave them inherited): the state.
    internal val completeState: Boolean get() = status == TaskStatus.SUCCESS || status == TaskStatus.FAILURE || status == TaskStatus.CANCELED
    internal val successState: Boolean get() = status == TaskStatus.SUCCESS
    internal val canceledState: Boolean get() = status == TaskStatus.CANCELED
    internal val exceptionState: Exception? get() = error

    internal fun resultState(): ResultT = when (status) {
        TaskStatus.SUCCESS -> finalSnapshot!!
        TaskStatus.FAILURE -> throw RuntimeExecutionException(error!!)
        TaskStatus.CANCELED -> throw CancellationException("Task is already canceled")
        else -> throw IllegalStateException("Task is not yet complete")
    }

    actual override val isInProgress: Boolean get() = status == TaskStatus.QUEUED || status == TaskStatus.IN_PROGRESS
    actual override val isPaused: Boolean get() = status == TaskStatus.PAUSED

    actual override fun cancel(): Boolean = !isComplete && controlCancel()

    actual override fun pause(): Boolean = isInProgress && controlPause()

    actual override fun resume(): Boolean = isPaused && controlResume()

    /** The platform task made progress (or resumed). */
    internal fun reportProgress() {
        if (isComplete) return
        status = TaskStatus.IN_PROGRESS
        onProgress()
        val current = snapshot
        progressListeners.toList().forEach { it.onProgress(current) }
    }

    /** The platform task was paused. */
    internal fun reportPaused() {
        if (isComplete) return
        status = TaskStatus.PAUSED
        onPaused()
        val current = snapshot
        pausedListeners.toList().forEach { it.onPaused(current) }
    }

    /** The platform task completed successfully. */
    internal fun reportSuccess() {
        if (!complete(TaskStatus.SUCCESS, null)) return
        onSuccess()
        val final = finalSnapshot!!
        successListeners.toList().forEach { it.onSuccess(final) }
        completeListeners.toList().forEach { it.onComplete(this) }
    }

    /** The platform task failed with [e]. */
    internal fun reportFailure(e: Exception) {
        if (!complete(TaskStatus.FAILURE, e)) return
        onFailure()
        failureListeners.toList().forEach { it.onFailure(e) }
        completeListeners.toList().forEach { it.onComplete(this) }
    }

    /** The platform task was cancelled. */
    internal fun reportCanceled() {
        if (!complete(TaskStatus.CANCELED, StorageException.canceled())) return
        onCanceled()
        canceledListeners.toList().forEach { it.onCanceled() }
        completeListeners.toList().forEach { it.onComplete(this) }
    }

    private fun complete(newStatus: TaskStatus, e: Exception?): Boolean {
        if (isComplete) return false
        error = e
        status = newStatus
        finalSnapshot = snapshotWith(e)
        return true
    }

    actual override fun addOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = apply {
        canceledListeners += listener
        if (isCanceled) listener.onCanceled()
    }

    actual override fun addOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = apply {
        completeListeners += listener
        if (isComplete) listener.onComplete(this)
    }

    actual override fun addOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = apply {
        failureListeners += listener
        if (status == TaskStatus.FAILURE) listener.onFailure(error!!)
    }

    actual override fun addOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = apply {
        pausedListeners += listener
        if (isPaused) listener.onPaused(snapshot)
    }

    actual override fun addOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = apply {
        progressListeners += listener
        if (status == TaskStatus.IN_PROGRESS) listener.onProgress(snapshot)
    }

    actual override fun addOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = apply {
        successListeners += listener
        if (isSuccessful) listener.onSuccess(finalSnapshot!!)
    }

    public actual fun removeOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = apply { canceledListeners -= listener }

    public actual fun removeOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = apply { completeListeners -= listener }

    public actual fun removeOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = apply { failureListeners -= listener }

    public actual fun removeOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = apply { pausedListeners -= listener }

    public actual fun removeOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = apply { progressListeners -= listener }

    public actual fun removeOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = apply { successListeners -= listener }

    actual override fun <ContinuationResultT> continueWith(continuation: Continuation<ResultT, ContinuationResultT>): Task<ContinuationResultT> {
        val source = TaskCompletionSource<ContinuationResultT>()
        addOnCompleteListener { task -> source.completeWith { continuation.then(task) } }
        return source.task
    }

    actual override fun <ContinuationResultT> continueWithTask(continuation: Continuation<ResultT, Task<ContinuationResultT>>): Task<ContinuationResultT> {
        val source = TaskCompletionSource<ContinuationResultT>()
        addOnCompleteListener { task -> source.completeWithTask { continuation.then(task) } }
        return source.task
    }

    actual override fun <ContinuationResultT> onSuccessTask(successContinuation: SuccessContinuation<ResultT, ContinuationResultT>): Task<ContinuationResultT> {
        val source = TaskCompletionSource<ContinuationResultT>()
        addOnCompleteListener { task ->
            val e = task.exception
            when {
                e != null -> source.trySetException(e)
                task.isCanceled -> source.trySetException(CancellationException("Task is already canceled"))
                else -> source.completeWithTask { successContinuation.then(task.result) }
            }
        }
        return source.task
    }

    protected actual open fun onCanceled() {}

    protected actual open fun onFailure() {}

    protected actual open fun onPaused() {}

    protected actual open fun onProgress() {}

    protected actual open fun onQueued() {}

    protected actual open fun onSuccess() {}

    public actual interface ProvideError {
        public actual val error: Exception?
    }

    public actual open inner class SnapshotBase actual constructor(error: Exception?) : ProvideError {
        private val errorValue = error
        actual override val error: Exception? get() = errorValue
        public actual val storage: StorageReference get() = reference
        public actual val task: StorageTask<ResultT> get() = this@StorageTask
    }
}

private inline fun <T> TaskCompletionSource<T>.completeWith(block: () -> T) {
    try {
        trySetResult(block())
    } catch (e: RuntimeExecutionException) {
        trySetException(e.cause as? Exception ?: e)
    } catch (e: Exception) {
        trySetException(e)
    }
}

private inline fun <T> TaskCompletionSource<T>.completeWithTask(block: () -> Task<T>) {
    val task = try {
        block()
    } catch (e: RuntimeExecutionException) {
        trySetException(e.cause as? Exception ?: e)
        return
    } catch (e: Exception) {
        trySetException(e)
        return
    }
    task.addOnCompleteListener { completed ->
        val e = completed.exception
        when {
            e != null -> trySetException(e)
            completed.isCanceled -> trySetException(CancellationException("Task is already canceled"))
            else -> trySetResult(completed.result)
        }
    }
}

/** The platform's upload task, driven by an [UploadTask]. */
internal interface UploadController {
    val bytesTransferred: Long
    val totalByteCount: Long
    val metadata: StorageMetadata?

    /** Starts observing the platform task, reporting to [task]. */
    fun start(task: UploadTask)

    fun pause(): Boolean

    fun resume(): Boolean

    fun cancel(): Boolean
}

public actual abstract class UploadTask internal constructor(
    override val reference: StorageReference,
    private val controller: UploadController,
) : StorageTask<UploadTask.TaskSnapshot>() {

    override fun snapshotWith(error: Exception?): TaskSnapshot = TaskSnapshot(error, controller.bytesTransferred, controller.totalByteCount, controller.metadata)

    override fun controlPause(): Boolean = controller.pause()

    override fun controlResume(): Boolean = controller.resume()

    override fun controlCancel(): Boolean = controller.cancel()

    protected actual open fun resetState() {}

    protected actual open fun schedule() {}

    public actual inner class TaskSnapshot internal constructor(
        error: Exception?,
        public actual val bytesTransferred: Long,
        public actual val totalByteCount: Long,
        public actual val metadata: StorageMetadata?,
    ) : SnapshotBase(error)
}

/** The [UploadTask] of the Apple and JS SDKs; [UploadTask] itself is abstract, as the expect class leaves the `Task` members inherited. */
internal class UploadTaskImpl(reference: StorageReference, controller: UploadController) : UploadTask(reference, controller) {
    override val isComplete: Boolean get() = completeState
    override val isSuccessful: Boolean get() = successState
    override val isCanceled: Boolean get() = canceledState
    override val result: TaskSnapshot get() = resultState()
    override val exception: Exception? get() = exceptionState

    init {
        ActiveTasks.uploads += this
        addOnCompleteListener { ActiveTasks.uploads -= this }
        controller.start(this)
    }
}

/** The Apple and JS SDKs do not expose the resumable upload session. */
public actual val UploadTask.TaskSnapshot.uploadSessionUri: String? get() = null

/** The tasks that have not completed, per reference; the Android SDK keeps the same registry. */
internal object ActiveTasks {
    val uploads = mutableListOf<UploadTask>()
    val downloads = mutableListOf<StorageTask<*>>()
}
