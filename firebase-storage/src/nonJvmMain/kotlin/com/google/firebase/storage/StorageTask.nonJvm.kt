/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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

/**
 * The status of a [StorageTask] together with its listeners, replaced as a whole on every change: a listener registered
 * while the task completes is either in the list that completion dispatches to or replayed once by its registration,
 * never both, and a platform callback (the main queue on Apple) and a registration from another thread see one
 * consistent state.
 */
private data class State<ResultT>(
    val status: TaskStatus,
    val error: Exception? = null,
    val finalSnapshot: ResultT? = null,
    val canceledListeners: List<OnCanceledListener> = emptyList(),
    val completeListeners: List<OnCompleteListener<ResultT>> = emptyList(),
    val failureListeners: List<OnFailureListener> = emptyList(),
    val pausedListeners: List<OnPausedListener<ResultT>> = emptyList(),
    val progressListeners: List<OnProgressListener<ResultT>> = emptyList(),
    val successListeners: List<OnSuccessListener<in ResultT>> = emptyList(),
) {
    val isComplete: Boolean get() = status == TaskStatus.SUCCESS || status == TaskStatus.FAILURE || status == TaskStatus.CANCELED
}

/**
 * Calls [block] for every listener. One that throws does not stop the others, as on Android, where each listener runs on
 * its own executor: what they throw is collected into [thrown] (the first kept, the others suppressed) and returned,
 * for the caller to rethrow once every group of listeners has run.
 */
private inline fun <L> List<L>.dispatch(thrown: Throwable? = null, block: (L) -> Unit): Throwable? {
    var first = thrown
    for (listener in this) {
        try {
            block(listener)
        } catch (e: Throwable) {
            if (first == null) first = e else first.addSuppressed(e)
        }
    }
    return first
}

@OptIn(ExperimentalAtomicApi::class)
public actual abstract class StorageTask<ResultT : StorageTask.ProvideError> protected actual constructor() : ControllableTask<ResultT>() {
    private val state = AtomicReference(State<ResultT>(TaskStatus.QUEUED))

    /** Replaces the state with what [transform] makes of the current one; returns the state replaced and its replacement. */
    private inline fun swap(transform: (State<ResultT>) -> State<ResultT>): Pair<State<ResultT>, State<ResultT>> {
        while (true) {
            val current = state.load()
            val next = transform(current)
            if (state.compareAndSet(current, next)) return current to next
        }
    }

    /** The reference the task operates on. */
    internal abstract val reference: StorageReference

    /** A snapshot of the platform task's current state, carrying [error]. */
    internal abstract fun snapshotWith(error: Exception?): ResultT

    internal abstract fun controlPause(): Boolean

    internal abstract fun controlResume(): Boolean

    internal abstract fun controlCancel(): Boolean

    public actual val snapshot: ResultT get() = state.load().let { it.finalSnapshot ?: snapshotWith(it.error) }

    private val status: TaskStatus get() = state.load().status

    // The Task members are implemented by the concrete subclasses (the expect classes leave them inherited): the state.
    internal val completeState: Boolean get() = state.load().isComplete
    internal val successState: Boolean get() = status == TaskStatus.SUCCESS
    internal val canceledState: Boolean get() = status == TaskStatus.CANCELED
    internal val exceptionState: Exception? get() = state.load().error

    internal fun resultState(): ResultT = state.load().let {
        when (it.status) {
            TaskStatus.SUCCESS -> it.finalSnapshot!!
            TaskStatus.FAILURE -> throw RuntimeExecutionException(it.error!!)
            TaskStatus.CANCELED -> throw CancellationException("Task is already canceled")
            else -> throw IllegalStateException("Task is not yet complete")
        }
    }

    actual override val isInProgress: Boolean get() = status == TaskStatus.QUEUED || status == TaskStatus.IN_PROGRESS
    actual override val isPaused: Boolean get() = status == TaskStatus.PAUSED

    actual override fun cancel(): Boolean = !isComplete && controlCancel()

    actual override fun pause(): Boolean = isInProgress && controlPause()

    actual override fun resume(): Boolean = isPaused && controlResume()

    /** The platform task made progress (or resumed). */
    internal fun reportProgress() {
        val (previous, current) = swap { if (it.isComplete) it else it.copy(status = TaskStatus.IN_PROGRESS) }
        if (previous.isComplete) return
        onProgress()
        val snapshot = snapshot
        current.progressListeners.dispatch { it.onProgress(snapshot) }?.let { throw it }
    }

    /** The platform task was paused. */
    internal fun reportPaused() {
        val (previous, current) = swap { if (it.isComplete) it else it.copy(status = TaskStatus.PAUSED) }
        if (previous.isComplete) return
        onPaused()
        val snapshot = snapshot
        current.pausedListeners.dispatch { it.onPaused(snapshot) }?.let { throw it }
    }

    /** The platform task completed successfully. */
    internal fun reportSuccess() {
        val current = complete(TaskStatus.SUCCESS, null) ?: return
        onSuccess()
        val final = current.finalSnapshot!!
        val thrown = current.successListeners.dispatch { it.onSuccess(final) }
        current.completeListeners.dispatch(thrown) { it.onComplete(this) }?.let { throw it }
    }

    /** The platform task failed with [e]. */
    internal fun reportFailure(e: Exception) {
        val current = complete(TaskStatus.FAILURE, e) ?: return
        onFailure()
        val thrown = current.failureListeners.dispatch { it.onFailure(e) }
        current.completeListeners.dispatch(thrown) { it.onComplete(this) }?.let { throw it }
    }

    /** The platform task was cancelled. */
    internal fun reportCanceled() {
        val current = complete(TaskStatus.CANCELED, StorageException.canceled()) ?: return
        onCanceled()
        val thrown = current.canceledListeners.dispatch { it.onCanceled() }
        current.completeListeners.dispatch(thrown) { it.onComplete(this) }?.let { throw it }
    }

    /**
     * Moves to the terminal [newStatus] and returns the state to dispatch to, or null when the task had already completed.
     * The status is final before any listener runs, so a listener that throws cannot leave the task incomplete.
     */
    private fun complete(newStatus: TaskStatus, e: Exception?): State<ResultT>? {
        val (previous, current) = swap { if (it.isComplete) it else it.copy(status = newStatus, error = e, finalSnapshot = snapshotWith(e)) }
        return current.takeUnless { previous.isComplete }
    }

    // A listener added after completion is replayed at once and not queued: the terminal dispatch is over (or, when the
    // listener is added from inside that dispatch, still iterating its own snapshot), so queueing it would run it twice.
    actual override fun addOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(canceledListeners = it.canceledListeners + listener) }
        if (previous.status == TaskStatus.CANCELED) listener.onCanceled()
    }

    actual override fun addOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(completeListeners = it.completeListeners + listener) }
        if (previous.isComplete) listener.onComplete(this)
    }

    actual override fun addOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(failureListeners = it.failureListeners + listener) }
        if (previous.status == TaskStatus.FAILURE) listener.onFailure(previous.error!!)
    }

    actual override fun addOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(pausedListeners = it.pausedListeners + listener) }
        if (previous.status == TaskStatus.PAUSED) listener.onPaused(snapshot)
    }

    actual override fun addOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(progressListeners = it.progressListeners + listener) }
        if (previous.status == TaskStatus.IN_PROGRESS) listener.onProgress(snapshot)
    }

    actual override fun addOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = apply {
        val (previous, _) = swap { if (it.isComplete) it else it.copy(successListeners = it.successListeners + listener) }
        if (previous.status == TaskStatus.SUCCESS) listener.onSuccess(previous.finalSnapshot!!)
    }

    public actual fun removeOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = apply { swap { it.copy(canceledListeners = it.canceledListeners - listener) } }

    public actual fun removeOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = apply { swap { it.copy(completeListeners = it.completeListeners - listener) } }

    public actual fun removeOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = apply { swap { it.copy(failureListeners = it.failureListeners - listener) } }

    public actual fun removeOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = apply { swap { it.copy(pausedListeners = it.pausedListeners - listener) } }

    public actual fun removeOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = apply { swap { it.copy(progressListeners = it.progressListeners - listener) } }

    public actual fun removeOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = apply { swap { it.copy(successListeners = it.successListeners - listener) } }

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
    ) : SnapshotBase(error) {
        /** Null: the Apple and JS SDKs do not expose the resumable upload session. */
        public actual val uploadSessionUri: Uri? get() = null
    }
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

/** Whether this reference is [parent] or under it, as the Android SDK's registry matches active tasks by URL prefix. */
internal fun StorageReference.isUnder(parent: StorageReference): Boolean = bucket == parent.bucket && (path == parent.path || path.startsWith(parent.path.trimEnd('/') + "/"))

/** The tasks that have not completed, per reference; the Android SDK keeps the same registry. */
internal object ActiveTasks {
    val uploads = mutableListOf<UploadTask>()
    val downloads = mutableListOf<StorageTask<*>>()
}
