/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import swiftPMImport.dev.gitlive.firebase.firestore.__FIRPipelineResultBridge

/** A row of a pipeline's results, over the iOS SDK's bridge. */
public actual class PipelineResult internal constructor(public val ios: __FIRPipelineResultBridge) {
    public actual val ref: DocumentReference? get() = ios.reference?.let { DocumentReference(it) }
    public actual fun getId(): String? = ios.documentID
    public actual val createTime: Timestamp? get() = ios.create_time?.toCompat() as Timestamp?
    public actual val updateTime: Timestamp? get() = ios.update_time?.toCompat() as Timestamp?

    public actual fun getData(): Map<String, Any?> = ios.data().toCompatData().orEmpty()

    public actual fun get(field: String): Any? = ios.get(field).toCompat()

    public actual fun get(fieldPath: FieldPath): Any? = ios.get(fieldPath.toIos()).toCompat()
}
