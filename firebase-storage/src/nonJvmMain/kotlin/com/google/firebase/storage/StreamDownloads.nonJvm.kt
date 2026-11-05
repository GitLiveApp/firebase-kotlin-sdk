/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import java.io.ByteArrayInputStream
import java.io.InputStream

/*
 * Streams on the Apple and JS SDKs, which upload and download whole byte buffers: an uploaded stream is read to its end
 * before the platform upload starts, and a stream download is the download of the whole object, read from memory.
 */

/** Runs [block] off the caller's thread where the platform has one (a global dispatch queue on Apple); at once on JS. */
internal expect fun inBackground(block: () -> Unit)

/** Runs [block] where the platform SDK delivers its callbacks (the main queue on Apple); at once on JS. */
internal expect fun onCallbackThread(block: () -> Unit)

/** Every remaining byte of the stream, which is closed. */
internal fun InputStream.readAllAndClose(): ByteArray = use { stream ->
    var result = ByteArray(0)
    val buffer = ByteArray(16 * 1024)
    var count = stream.read(buffer)
    while (count >= 0) {
        result += buffer.copyOf(count)
        count = stream.read(buffer)
    }
    result
}

/**
 * An [UploadController] that reads [stream] to its end in the background, then uploads the bytes through the controller
 * [upload] makes for them, to which the task's controls are forwarded from then on. Cancelling before the read completes
 * cancels the task without an upload.
 */
internal class StreamUploadController(private val stream: InputStream, private val upload: (ByteArray) -> UploadController) : UploadController {
    private var inner: UploadController? = null
    private var task: UploadTask? = null
    private var canceled = false

    override val bytesTransferred: Long get() = inner?.bytesTransferred ?: 0L
    override val totalByteCount: Long get() = inner?.totalByteCount ?: 0L
    override val metadata: StorageMetadata? get() = inner?.metadata

    override fun start(task: UploadTask) {
        this.task = task
        inBackground {
            val bytes = try {
                stream.readAllAndClose()
            } catch (e: Exception) {
                onCallbackThread { task.reportFailure(StorageException.fromException(e)) }
                return@inBackground
            }
            onCallbackThread {
                if (!canceled) inner = upload(bytes).also { it.start(task) }
            }
        }
    }

    override fun pause(): Boolean = inner?.pause() ?: false

    override fun resume(): Boolean = inner?.resume() ?: false

    override fun cancel(): Boolean = inner?.cancel() ?: run {
        canceled = true
        task?.reportCanceled()
        true
    }
}

public actual abstract class StreamDownloadTask internal constructor(
    override val reference: StorageReference,
    private val processor: StreamProcessor?,
) : StorageTask<StreamDownloadTask.TaskSnapshot>() {
    private var bytes: ByteArray? = null
    private var canceled = false

    internal fun start() {
        ActiveTasks.downloads += this
        addOnCompleteListener { ActiveTasks.downloads -= this }
        reference.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { downloaded ->
                if (canceled) return@addOnSuccessListener
                bytes = downloaded
                val processor = processor
                if (processor == null) {
                    reportSuccess()
                } else {
                    inBackground {
                        val failure = try {
                            processor.doInBackground(snapshotWith(null), stream())
                            null
                        } catch (e: Exception) {
                            e
                        }
                        onCallbackThread { if (failure == null) reportSuccess() else reportFailure(StorageException.fromException(failure)) }
                    }
                }
            }
            .addOnFailureListener { if (!canceled) reportFailure(StorageException.fromException(it)) }
    }

    private fun stream(): InputStream = ByteArrayInputStream(bytes ?: ByteArray(0))

    override fun snapshotWith(error: Exception?): TaskSnapshot {
        val size = bytes?.size?.toLong() ?: 0L
        return TaskSnapshot(error, size, size, stream())
    }

    /** False: the Apple and JS SDKs' byte downloads cannot be paused. */
    override fun controlPause(): Boolean = false

    override fun controlResume(): Boolean = false

    /** Cancels the task; the platform download itself runs to completion and is discarded. */
    override fun controlCancel(): Boolean {
        canceled = true
        reportCanceled()
        return true
    }

    public actual fun interface StreamProcessor {
        public actual fun doInBackground(state: TaskSnapshot, stream: InputStream)
    }

    public actual inner class TaskSnapshot internal constructor(
        error: Exception?,
        public actual val bytesTransferred: Long,
        public actual val totalByteCount: Long,
        public actual val stream: InputStream,
    ) : SnapshotBase(error)
}

internal class StreamDownloadTaskImpl(reference: StorageReference, processor: StreamDownloadTask.StreamProcessor?) : StreamDownloadTask(reference, processor) {
    override val isComplete: Boolean get() = completeState
    override val isSuccessful: Boolean get() = successState
    override val isCanceled: Boolean get() = canceledState
    override val result: TaskSnapshot get() = resultState()
    override val exception: Exception? get() = exceptionState

    init {
        start()
    }
}
