/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.database.DataSnapshot
import dev.gitlive.firebase.externals.functions.HttpsCallable
import dev.gitlive.firebase.externals.functions.HttpsCallableResult
import kotlin.js.Promise

typealias ChangeSnapshotCallback = (data: DataSnapshot, previousChildName: String?) -> Unit
typealias ValueSnapshotCallback = (data: DataSnapshot) -> Unit
typealias CancelCallback = (error: Throwable) -> Unit
typealias Unsubscribe = () -> Unit

operator fun HttpsCallable.invoke() = asDynamic()() as Promise<HttpsCallableResult>
operator fun HttpsCallable.invoke(data: Any?) = asDynamic()(data) as Promise<HttpsCallableResult>
