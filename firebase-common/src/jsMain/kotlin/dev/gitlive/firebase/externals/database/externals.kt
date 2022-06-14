/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.externals.database

typealias ChangeSnapshotCallback = (data: DataSnapshot, previousChildName: String?) -> Unit
typealias ValueSnapshotCallback = (data: DataSnapshot) -> Unit
typealias CancelCallback = (error: Throwable) -> Unit
