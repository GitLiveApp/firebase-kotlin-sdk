/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

actual val Firebase.storage get() =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance())

actual fun Firebase.storage(app: FirebaseApp) =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.android))

actual class FirebaseStorage(val android: com.google.firebase.storage.FirebaseStorage) {

    actual fun getMaxOperationRetryTimeMillis(): Long = android.maxOperationRetryTimeMillis

    actual fun getMaxUploadRetryTimeMillis(): Long = android.maxUploadRetryTimeMillis

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
        android.maxOperationRetryTimeMillis = maxOperationRetryTimeMillis
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
        android.maxUploadRetryTimeMillis = maxUploadRetryTimeMillis
    }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

}

actual open class StorageException(message: String) : FirebaseException(message)
