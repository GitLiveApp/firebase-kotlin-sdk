/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task

/** A [Task] that can be cancelled and reports progress, as the Android SDK's `CancellableTask`. */
public expect abstract class CancellableTask<StateT>() : Task<StateT> {
    /** Cancels the task; true if it was running. */
    public abstract fun cancel(): Boolean

    /** Whether the task is running (not complete, cancelled or paused). */
    public abstract val isInProgress: Boolean

    /** Adds a listener called with a snapshot whenever progress is made. */
    public abstract fun addOnProgressListener(listener: OnProgressListener<StateT>): CancellableTask<StateT>
}

/** A [CancellableTask] that can also be paused and resumed, as the Android SDK's `ControllableTask`. */
public expect abstract class ControllableTask<StateT>() : CancellableTask<StateT> {
    /** Whether the task is paused. */
    public abstract val isPaused: Boolean

    /** Pauses the task; true if it was running. */
    public abstract fun pause(): Boolean

    /** Resumes the task; true if it was paused. */
    public abstract fun resume(): Boolean

    /** Adds a listener called with a snapshot when the task is paused. */
    public abstract fun addOnPausedListener(listener: OnPausedListener<StateT>): ControllableTask<StateT>
}

/**
 * A long-running Cloud Storage operation with progress, pause, resume and cancel, as the Android SDK's `StorageTask`.
 * Listeners are called on the thread that completes or progresses the task on Apple platforms and JS. The `Task`
 * members (`isComplete`, `result`, `exception`, ...) are inherited; the task subclasses are abstract, as instances only
 * come from a [StorageReference].
 */
public expect abstract class StorageTask<ResultT : StorageTask.ProvideError> protected constructor() : ControllableTask<ResultT> {
    /** The current state of the task, whether or not it is complete. */
    public val snapshot: ResultT

    override val isInProgress: Boolean
    override val isPaused: Boolean
    override fun cancel(): Boolean
    override fun pause(): Boolean
    override fun resume(): Boolean

    override fun addOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT>
    override fun addOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT>
    override fun addOnFailureListener(listener: OnFailureListener): StorageTask<ResultT>
    override fun addOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT>
    override fun addOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT>
    override fun addOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT>
    public fun removeOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT>
    public fun removeOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT>
    public fun removeOnFailureListener(listener: OnFailureListener): StorageTask<ResultT>
    public fun removeOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT>
    public fun removeOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT>
    public fun removeOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT>

    override fun <ContinuationResultT> continueWith(continuation: Continuation<ResultT, ContinuationResultT>): Task<ContinuationResultT>
    override fun <ContinuationResultT> continueWithTask(continuation: Continuation<ResultT, Task<ContinuationResultT>>): Task<ContinuationResultT>
    override fun <ContinuationResultT> onSuccessTask(successContinuation: SuccessContinuation<ResultT, ContinuationResultT>): Task<ContinuationResultT>

    /** Called when the task is cancelled. */
    protected open fun onCanceled()

    /** Called when the task fails. */
    protected open fun onFailure()

    /** Called when the task is paused. */
    protected open fun onPaused()

    /** Called when the task makes progress. */
    protected open fun onProgress()

    /** Called when the task is queued. */
    protected open fun onQueued()

    /** Called when the task succeeds. */
    protected open fun onSuccess()

    /** What a task's result provides: the error it failed with, if any. */
    public interface ProvideError {
        public val error: Exception?
    }

    /** The base of a task's snapshots: the error, if any, and the task and reference they belong to. */
    public open inner class SnapshotBase(error: Exception?) : ProvideError {
        /** The error the task failed with, if any. */
        override val error: Exception?

        /** The reference the task operates on. */
        public val storage: StorageReference

        /** The task. */
        public val task: StorageTask<ResultT>
    }
}

/** An upload to a [StorageReference], as the Android SDK's `UploadTask`; [TaskSnapshot] reports its progress. */
public expect abstract class UploadTask : StorageTask<UploadTask.TaskSnapshot> {
    /** Resets the task's state before a retry. */
    protected open fun resetState()

    /** Schedules the upload to run. */
    protected open fun schedule()

    /** The state of an upload; the upload session URL is the `uploadSessionUri` extension. */
    public inner class TaskSnapshot : StorageTask<TaskSnapshot>.SnapshotBase {
        /** The bytes uploaded so far. */
        public val bytesTransferred: Long

        /** The metadata of the uploaded object, once the upload has completed. */
        public val metadata: StorageMetadata?

        /** The total size of the upload. */
        public val totalByteCount: Long
    }
}

/**
 * The URL of the resumable upload session, if the SDK exposes it (the Android SDK returns an `android.net.Uri`, which
 * common code cannot name; on Android the SDK's member binds for Android code and this String form is shipped).
 */
public expect val UploadTask.TaskSnapshot.uploadSessionUri: String?

/** Receives the snapshots of a paused task; the Android SDK's `OnPausedListener`. */
public fun interface OnPausedListener<in ProgressT> {
    public fun onPaused(snapshot: ProgressT)
}

/** Receives the snapshots of a task as it makes progress; the Android SDK's `OnProgressListener`. */
public fun interface OnProgressListener<in ProgressT> {
    public fun onProgress(snapshot: ProgressT)
}

/** The state of a [StorageTask] as emitted by [taskState]. */
public abstract class TaskState<T> private constructor() {
    /** The task is running and made progress; [snapshot] is its current state. */
    public class InProgress<T>(public val snapshot: T) : TaskState<T>()

    /** The task is paused; [snapshot] is its state at the pause. */
    public class Paused<T>(public val snapshot: T) : TaskState<T>()
}
