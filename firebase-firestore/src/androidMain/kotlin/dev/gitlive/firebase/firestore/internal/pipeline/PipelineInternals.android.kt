/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal.pipeline

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Pipeline
import dev.gitlive.firebase.firestore.stub

// Never called: on Android and the JVM the pipeline classes are header stubs, and the SDK's own classes run the pipeline.

internal actual fun executePipeline(pipeline: Pipeline, options: Pipeline.ExecuteOptions?): Task<Pipeline.Snapshot> = stub()

internal actual fun FieldPath.canonicalPath(): String = stub()
