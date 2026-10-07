/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.storage.storage as compatStorage

// The Android-SDK-shaped entry points are reached through the `Firebase.storage` extensions (StorageKt), which are
// real static methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
public val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage(com.google.firebase.Firebase.compatStorage)

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp] for the bucket of [url]. */
public fun Firebase.storage(url: String): FirebaseStorage = FirebaseStorage(com.google.firebase.Firebase.compatStorage(url))

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
public fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage(com.google.firebase.Firebase.compatStorage(app.compat))

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp] for the bucket of [url]. */
public fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage = FirebaseStorage(com.google.firebase.Firebase.compatStorage(app.compat, url))
