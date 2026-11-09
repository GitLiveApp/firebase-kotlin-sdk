/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalAtomicApi::class)

package com.google.firebase.storage

import kotlinx.io.Source
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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
 * [upload] makes for them, to which the task's controls are forwarded from then on. Cancelling before the platform upload
 * starts cancels the task without one; the handoff is atomic, so a cancellation that lands while the platform upload is
 * being created cancels that upload too (Apple delivers the callback on the main queue while `cancel` may run elsewhere).
 */
internal class SourceUploadController(private val source: Source, private val upload: (ByteArray) -> UploadController) : UploadController {
    private sealed interface State {
        /** The source is being read. */
        data object Reading : State

        /** The platform upload is being created. */
        data object Starting : State

        /** The platform upload runs; [inner] drives it. */
        class Started(val inner: UploadController) : State

        /** Cancelled or failed before the platform upload started. */
        data object Done : State
    }

    private val state = AtomicReference<State>(State.Reading)
    private var task: UploadTask? = null

    private val inner: UploadController? get() = (state.load() as? State.Started)?.inner

    override val bytesTransferred: Long get() = inner?.bytesTransferred ?: 0L
    override val totalByteCount: Long get() = inner?.totalByteCount ?: 0L
    override val metadata: StorageMetadata? get() = inner?.metadata

    override fun start(task: UploadTask) {
        this.task = task
        inBackground {
            val bytes = try {
                source.use { it.readByteArray() }
            } catch (e: Exception) {
                onCallbackThread { fail(task, e) }
                return@inBackground
            }
            onCallbackThread {
                if (!state.compareAndSet(State.Reading, State.Starting)) return@onCallbackThread
                val started = try {
                    upload(bytes)
                } catch (e: Exception) {
                    fail(task, e)
                    return@onCallbackThread
                }
                // Cancelled during the handoff: the platform upload has begun and is cancelled in turn.
                if (state.compareAndSet(State.Starting, State.Started(started))) started.start(task) else started.cancel()
            }
        }
    }

    private fun fail(task: UploadTask, e: Exception) {
        if (state.compareAndSet(State.Reading, State.Done) || state.compareAndSet(State.Starting, State.Done)) task.reportFailure(StorageException.fromException(e))
    }

    override fun pause(): Boolean = inner?.pause() ?: false

    override fun resume(): Boolean = inner?.resume() ?: false

    override fun cancel(): Boolean {
        while (true) {
            when (val current = state.load()) {
                is State.Started -> return current.inner.cancel()
                State.Done -> return false
                else -> if (state.compareAndSet(current, State.Done)) {
                    task?.reportCanceled()
                    return true
                }
            }
        }
    }
}
