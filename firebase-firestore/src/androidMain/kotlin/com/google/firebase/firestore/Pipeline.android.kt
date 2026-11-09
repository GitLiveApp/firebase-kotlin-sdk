/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import dev.gitlive.firebase.firestore.stub

/*
 * Header stubs of the pipeline API's platform parts (see buildSrc utils/HeaderStubs.kt): the pipeline classes
 * themselves are plain common code, compiled, verified against the SDK's classes and stripped like these.
 */

public actual class PipelineResult private constructor() {
    public actual val ref: DocumentReference? get() = stub()
    public actual fun getId(): String? = stub()
    public actual val createTime: Timestamp? get() = stub()
    public actual val updateTime: Timestamp? get() = stub()
    public actual fun getData(): Map<String, Any?> = stub()
    public actual fun get(field: String): Any? = stub()
    public actual fun get(fieldPath: FieldPath): Any? = stub()
}
