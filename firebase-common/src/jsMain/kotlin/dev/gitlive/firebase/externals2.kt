/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlin.js.Promise

@JsModule("firebase/compat/auth")
@JsName("default")
external object auth

@JsModule("firebase/compat/database")
@JsName("default")
external object database

@JsModule("firebase/compat/firestore")
@JsName("default")
external object firestore

@JsModule("firebase/compat/functions")
@JsName("default")
external object functions

external object installations

@JsModule("firebase/compat/remote-config")
@JsName("default")
external object remoteConfig

typealias SnapshotCallback = (data: firebase.database.DataSnapshot, b: String?) -> Unit

operator fun firebase.functions.HttpsCallable.invoke() = asDynamic()() as Promise<firebase.functions.HttpsCallableResult>
operator fun firebase.functions.HttpsCallable.invoke(data: Any?) = asDynamic()(data) as Promise<firebase.functions.HttpsCallableResult>

