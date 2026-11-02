/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

internal const val FILE_DOWNLOADS_UNSUPPORTED_ON_JS = "The Firebase JS SDK downloads through URLs, not to files: use getBytes or getDownloadUrl"

/** A download to a file on JS, which the SDK cannot do: the task fails at once with [UnsupportedOperationException]. */
public actual abstract class FileDownloadTask internal constructor(override val reference: StorageReference) : StorageTask<FileDownloadTask.TaskSnapshot>() {
    override fun snapshotWith(error: Exception?): TaskSnapshot = TaskSnapshot(error)

    override fun controlPause(): Boolean = false

    override fun controlResume(): Boolean = false

    override fun controlCancel(): Boolean = false

    public actual inner class TaskSnapshot internal constructor(error: Exception?) : SnapshotBase(error) {
        public actual val bytesTransferred: Long get() = 0L
        public actual val totalByteCount: Long get() = 0L
    }
}

internal class FileDownloadTaskImpl(reference: StorageReference) : FileDownloadTask(reference) {
    override val isComplete: Boolean get() = completeState
    override val isSuccessful: Boolean get() = successState
    override val isCanceled: Boolean get() = canceledState
    override val result: TaskSnapshot get() = resultState()
    override val exception: Exception? get() = exceptionState

    init {
        reportFailure(UnsupportedOperationException(FILE_DOWNLOADS_UNSUPPORTED_ON_JS))
    }
}
