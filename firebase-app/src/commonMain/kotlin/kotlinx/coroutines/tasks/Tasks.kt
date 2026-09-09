/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.tasks

import com.google.android.gms.tasks.Task

/**
 * Awaits the completion of the task without blocking a thread, returning its result or throwing its exception,
 * mirroring `kotlinx.coroutines.tasks.await` from `kotlinx-coroutines-play-services` so that Android code keeps
 * its import unchanged on every platform.
 *
 * On Android and the JVM this is the real `kotlinx-coroutines-play-services` function; on Apple and JS platforms
 * a Kotlin implementation over the shared [Task] is provided.
 */
public expect suspend fun <T> Task<T>.await(): T
