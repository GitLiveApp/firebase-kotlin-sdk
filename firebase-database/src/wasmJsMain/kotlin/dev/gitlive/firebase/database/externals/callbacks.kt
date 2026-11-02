/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database.externals

public typealias ChangeSnapshotCallback = (data: DataSnapshot, previousChildName: String?) -> Unit
public typealias ValueSnapshotCallback = (data: DataSnapshot) -> Unit

// Unlike the `js` target, a JS error caught in wasm is an opaque [JsAny], not a Kotlin [Throwable].
public typealias CancelCallback = (error: JsAny) -> Unit
