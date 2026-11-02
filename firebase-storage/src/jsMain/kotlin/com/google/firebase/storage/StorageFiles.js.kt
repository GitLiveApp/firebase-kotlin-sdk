/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

internal const val FILE_UPLOADS_UNSUPPORTED_ON_JS = "The Firebase JS SDK cannot read a file by its URL: use putBytes, or the dev.gitlive wrapper's putFile with a browser File"

/** An upload that fails at once: the JS SDK cannot read a file by its URL. */
internal class FailedUploadController(private val error: Exception) : UploadController {
    override val bytesTransferred: Long get() = 0L
    override val totalByteCount: Long get() = 0L
    override val metadata: StorageMetadata? get() = null

    override fun start(task: UploadTask) {
        task.reportFailure(error)
    }

    override fun pause(): Boolean = false

    override fun resume(): Boolean = false

    override fun cancel(): Boolean = false
}

internal fun StorageReference.failedFileUpload(): UploadTask = UploadTaskImpl(this, FailedUploadController(UnsupportedOperationException(FILE_UPLOADS_UNSUPPORTED_ON_JS)))
