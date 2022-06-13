/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.database.DataSnapshot
import kotlin.js.Promise

@JsModule("firebase/compat/functions")
@JsName("default")
external object functions

external object installations

typealias ChangeSnapshotCallback = (data: DataSnapshot, previousChildName: String?) -> Unit
typealias ValueSnapshotCallback = (data: DataSnapshot) -> Unit
typealias CancelCallback = (error: Throwable) -> Unit
typealias Unsubscribe = () -> Unit

operator fun firebase.functions.HttpsCallable.invoke() = asDynamic()() as Promise<firebase.functions.HttpsCallableResult>
operator fun firebase.functions.HttpsCallable.invoke(data: Any?) = asDynamic()(data) as Promise<firebase.functions.HttpsCallableResult>
