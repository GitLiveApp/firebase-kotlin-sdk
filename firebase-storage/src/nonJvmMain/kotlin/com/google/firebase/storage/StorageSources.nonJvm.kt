/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import kotlinx.io.Source
import kotlinx.io.readByteArray

/*
 * Sources on the Apple and JS SDKs, which upload whole byte buffers: an uploaded source is read to its end before the
 * platform upload starts.
 */

/** Runs [block] off the caller's thread where the platform has one (a global dispatch queue on Apple); at once on JS. */
internal expect fun inBackground(block: () -> Unit)

/** Runs [block] where the platform SDK delivers its callbacks (the main queue on Apple); at once on JS. */
internal expect fun onCallbackThread(block: () -> Unit)

/**
 * An [UploadController] that reads [source] to its end in the background, then uploads the bytes through the controller
 * [upload] makes for them, to which the task's controls are forwarded from then on. Cancelling before the read completes
 * cancels the task without an upload.
 */
internal class SourceUploadController(private val source: Source, private val upload: (ByteArray) -> UploadController) : UploadController {
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
                source.use { it.readByteArray() }
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
