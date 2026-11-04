/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

/** @property results The result of every aggregation by its alias. */
public actual class AggregateQuerySnapshot internal constructor(public actual val query: AggregateQuery, private val results: Map<String, Any?>) {
    public actual val count: Long get() = getLong(AggregateField.count()) ?: 0L

    public actual fun get(aggregateField: AggregateField): Any? {
        require(results.containsKey(aggregateField.alias)) { "'${aggregateField.alias}' was not requested in the aggregation query." }
        return results[aggregateField.alias]
    }

    public actual fun get(averageAggregateField: AggregateField.AverageAggregateField): Double? = getDouble(averageAggregateField)

    public actual fun get(countAggregateField: AggregateField.CountAggregateField): Long = getLong(countAggregateField) ?: 0L

    public actual fun getDouble(aggregateField: AggregateField): Double? = (get(aggregateField) as? Number)?.toDouble()

    public actual fun getLong(aggregateField: AggregateField): Long? = (get(aggregateField) as? Number)?.toLong()

    override fun equals(other: Any?): Boolean = other is AggregateQuerySnapshot && other.query == query && other.results == results

    override fun hashCode(): Int = 31 * query.hashCode() + results.hashCode()

    override fun toString(): String = "AggregateQuerySnapshot{$results}"
}

public actual class LoadBundleTaskProgress internal constructor(
    public actual val documentsLoaded: Int,
    public actual val totalDocuments: Int,
    public actual val bytesLoaded: Long,
    public actual val totalBytes: Long,
    public actual val taskState: TaskState,
    public actual val exception: Exception?,
) {
    public actual enum class TaskState {
        ERROR,
        RUNNING,
        SUCCESS,
    }

    override fun equals(other: Any?): Boolean = other is LoadBundleTaskProgress &&
        other.documentsLoaded == documentsLoaded && other.totalDocuments == totalDocuments && other.bytesLoaded == bytesLoaded &&
        other.totalBytes == totalBytes && other.taskState == taskState && other.exception == exception

    override fun hashCode(): Int = listOf(documentsLoaded, totalDocuments, bytesLoaded, totalBytes, taskState, exception).hashCode()

    override fun toString(): String = "LoadBundleTaskProgress{documentsLoaded=$documentsLoaded, totalDocuments=$totalDocuments, bytesLoaded=$bytesLoaded, totalBytes=$totalBytes, taskState=$taskState}"
}

/** Created by the platform implementations of `FirebaseFirestore.loadBundle` as a [LoadBundleTaskImpl]. */
public actual abstract class LoadBundleTask internal constructor() : Task<LoadBundleTaskProgress>() {
    public actual fun addOnProgressListener(listener: OnProgressListener<LoadBundleTaskProgress>): LoadBundleTask {
        registerProgressListener(listener)
        return this
    }

    internal abstract fun registerProgressListener(listener: OnProgressListener<LoadBundleTaskProgress>)
}

/** Completed by the platform SDK's progress updates through [update]; the final progress completes the task. */
internal class LoadBundleTaskImpl : LoadBundleTask() {
    private val source = TaskCompletionSource<LoadBundleTaskProgress>()
    private val task get() = source.task
    private val progressListeners = mutableListOf<OnProgressListener<LoadBundleTaskProgress>>()
    private var progress: LoadBundleTaskProgress? = null

    /** Reports [progress]; a `SUCCESS` or `ERROR` state completes the task. */
    fun update(progress: LoadBundleTaskProgress) {
        this.progress = progress
        progressListeners.toList().forEach { it.onProgress(progress) }
        when (progress.taskState) {
            LoadBundleTaskProgress.TaskState.SUCCESS -> source.trySetResult(progress)
            LoadBundleTaskProgress.TaskState.ERROR -> source.trySetException(progress.exception ?: FirebaseFirestoreException("Bundle loading failed", FirebaseFirestoreException.Code.UNKNOWN))
            LoadBundleTaskProgress.TaskState.RUNNING -> Unit
        }
    }

    /** Fails the task with [exception], keeping the last progress counts. */
    fun fail(exception: Exception) {
        update(LoadBundleTaskProgress(progress?.documentsLoaded ?: 0, progress?.totalDocuments ?: 0, progress?.bytesLoaded ?: 0, progress?.totalBytes ?: 0, LoadBundleTaskProgress.TaskState.ERROR, exception))
    }

    override fun registerProgressListener(listener: OnProgressListener<LoadBundleTaskProgress>) {
        progressListeners += listener
        progress?.takeIf { it.taskState != LoadBundleTaskProgress.TaskState.RUNNING }?.let(listener::onProgress)
    }

    override val isComplete: Boolean get() = task.isComplete
    override val isSuccessful: Boolean get() = task.isSuccessful
    override val isCanceled: Boolean get() = task.isCanceled
    override val result: LoadBundleTaskProgress get() = task.result
    override val exception: Exception? get() = task.exception
    override fun addOnSuccessListener(listener: OnSuccessListener<LoadBundleTaskProgress>): Task<LoadBundleTaskProgress> = task.addOnSuccessListener(listener)
    override fun addOnFailureListener(listener: OnFailureListener): Task<LoadBundleTaskProgress> = task.addOnFailureListener(listener)
    override fun addOnCompleteListener(listener: OnCompleteListener<LoadBundleTaskProgress>): Task<LoadBundleTaskProgress> = task.addOnCompleteListener(listener)
    override fun addOnCanceledListener(listener: OnCanceledListener): Task<LoadBundleTaskProgress> = task.addOnCanceledListener(listener)
    override fun <TContinuationResult> continueWith(continuation: Continuation<LoadBundleTaskProgress, TContinuationResult>): Task<TContinuationResult> = task.continueWith(continuation)
    override fun <TContinuationResult> continueWithTask(continuation: Continuation<LoadBundleTaskProgress, Task<TContinuationResult>>): Task<TContinuationResult> = task.continueWithTask(continuation)
    override fun <TContinuationResult> onSuccessTask(successContinuation: SuccessContinuation<LoadBundleTaskProgress, TContinuationResult>): Task<TContinuationResult> = task.onSuccessTask(successContinuation)
}
