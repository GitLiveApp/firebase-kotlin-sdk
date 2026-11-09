/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import dev.gitlive.firebase.firestore.externals.PipelineResult as JsPipelineResult

/** A row of a pipeline's results, over the JS SDK's. */
public actual class PipelineResult internal constructor(public val js: JsPipelineResult) {
    public actual val ref: DocumentReference? get() = js.ref?.let { DocumentReference(it) }
    public actual fun getId(): String? = js.id
    public actual val createTime: Timestamp? get() = js.createTime?.toCompat() as Timestamp?
    public actual val updateTime: Timestamp? get() = js.updateTime?.toCompat() as Timestamp?

    @Suppress("UNCHECKED_CAST")
    public actual fun getData(): Map<String, Any?> = rethrow { js.data() }.toCompat() as Map<String, Any?>

    public actual fun get(field: String): Any? = rethrow { js.get(field.toJsString()) }.toCompat()

    public actual fun get(fieldPath: FieldPath): Any? = rethrow { js.get(fieldPath.toJs()) }.toCompat()
}
