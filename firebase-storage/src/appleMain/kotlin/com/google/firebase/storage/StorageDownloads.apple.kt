/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import cocoapods.FirebaseStorage.FIRStorageDownloadTask
import cocoapods.FirebaseStorage.FIRStorageTaskSnapshot
import cocoapods.FirebaseStorage.FIRStorageTaskStatusFailure
import cocoapods.FirebaseStorage.FIRStorageTaskStatusPause
import cocoapods.FirebaseStorage.FIRStorageTaskStatusProgress
import cocoapods.FirebaseStorage.FIRStorageTaskStatusResume
import cocoapods.FirebaseStorage.FIRStorageTaskStatusSuccess

public actual abstract class FileDownloadTask internal constructor(
    override val reference: StorageReference,
    private val ios: FIRStorageDownloadTask,
) : StorageTask<FileDownloadTask.TaskSnapshot>() {
    private var bytesTransferred = 0L
    private var totalByteCount = 0L

    internal fun start() {
        ActiveTasks.downloads += this
        addOnCompleteListener { ActiveTasks.downloads -= this }
        ios.observeStatus(FIRStorageTaskStatusProgress) { snapshot ->
            update(snapshot)
            reportProgress()
        }
        ios.observeStatus(FIRStorageTaskStatusResume) { snapshot ->
            update(snapshot)
            reportProgress()
        }
        ios.observeStatus(FIRStorageTaskStatusPause) { snapshot ->
            update(snapshot)
            reportPaused()
        }
        ios.observeStatus(FIRStorageTaskStatusSuccess) { snapshot ->
            update(snapshot)
            ios.removeAllObservers()
            reportSuccess()
        }
        ios.observeStatus(FIRStorageTaskStatusFailure) { snapshot ->
            update(snapshot)
            ios.removeAllObservers()
            val error = snapshot?.error()
            if (error == null || error.code == StorageException.ERROR_CANCELED.toLong()) reportCanceled() else reportFailure(error.toStorageException())
        }
    }

    private fun update(snapshot: FIRStorageTaskSnapshot?) {
        snapshot?.progress()?.let {
            bytesTransferred = it.completedUnitCount
            totalByteCount = it.totalUnitCount
        }
    }

    override fun snapshotWith(error: Exception?): TaskSnapshot = TaskSnapshot(error, bytesTransferred, totalByteCount)

    override fun controlPause(): Boolean {
        ios.pause()
        return true
    }

    override fun controlResume(): Boolean {
        ios.resume()
        return true
    }

    override fun controlCancel(): Boolean {
        ios.cancel()
        return true
    }

    public actual inner class TaskSnapshot internal constructor(
        error: Exception?,
        public actual val bytesTransferred: Long,
        public actual val totalByteCount: Long,
    ) : SnapshotBase(error)
}

/** The [FileDownloadTask] of the iOS SDK; [FileDownloadTask] itself is abstract, as the expect class leaves the `Task` members inherited. */
internal class FileDownloadTaskImpl(reference: StorageReference, ios: FIRStorageDownloadTask) : FileDownloadTask(reference, ios) {
    override val isComplete: Boolean get() = completeState
    override val isSuccessful: Boolean get() = successState
    override val isCanceled: Boolean get() = canceledState
    override val result: TaskSnapshot get() = resultState()
    override val exception: Exception? get() = exceptionState

    init {
        start()
    }
}

/** [destination] is an `NSURL` or a `String` path. */
public actual fun StorageReference.getFile(destination: Any): FileDownloadTask = FileDownloadTaskImpl(this, ios.writeToFile(destination.toFileUrl()))

public actual val StorageReference.activeDownloadTasks: List<FileDownloadTask> get() = ActiveTasks.downloads.filterIsInstance<FileDownloadTask>().filter { it.reference == this }

public actual var FirebaseStorage.maxDownloadRetryTimeMillis: Long
    get() = maxDownloadRetryTimeMillisValue
    set(value) {
        maxDownloadRetryTimeMillisValue = value
    }
