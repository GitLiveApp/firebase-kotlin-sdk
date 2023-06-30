/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageReference
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException


actual val Firebase.storage get() =
    FirebaseStorage(FIRStorage.storage())

actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage(
    FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp)
)

actual class FirebaseStorage(val ios: FIRStorage) {

    actual fun getMaxOperationRetryTimeMillis(): Long = ios.maxOperationRetryTime().toLong()

    actual fun getMaxUploadRetryTimeMillis(): Long = ios.maxUploadRetryTime().toLong()

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
        ios.setMaxOperationRetryTime(maxOperationRetryTimeMillis.toDouble())
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
        ios.setMaxUploadRetryTime(maxUploadRetryTimeMillis.toDouble())
    }

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

}

actual class StorageReference(val ios: FIRStorageReference) {
    actual val name: String get() = ios.name()
    actual val path: String get() = ios.fullPath()
    actual val bucket: String get() = ios.bucket()
    actual val parent: StorageReference? get() = ios.parent()?.let { StorageReference(it) }
    actual val root: StorageReference get() = StorageReference(ios.root())
    actual val storage: FirebaseStorage get() = FirebaseStorage(ios.storage())

    actual fun child(path: String): StorageReference = StorageReference(ios.child(path))
}

actual open class StorageException(message: String) : FirebaseException(message)
