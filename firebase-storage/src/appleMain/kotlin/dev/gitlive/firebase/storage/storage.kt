/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import swiftPMImport.dev.gitlive.firebase.storage.FIRStorage
import swiftPMImport.dev.gitlive.firebase.storage.FIRStorageListResult
import swiftPMImport.dev.gitlive.firebase.storage.FIRStorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.putFileUrl
import com.google.firebase.storage.toByteArray
import com.google.firebase.storage.toNSData
import platform.Foundation.NSData
import platform.Foundation.NSURL
import com.google.firebase.storage.StorageMetadata as CompatStorageMetadata
import com.google.firebase.storage.StorageReference as CompatStorageReference

/** The underlying Firebase iOS SDK object. */
public val FirebaseStorage.ios: FIRStorage get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val StorageReference.ios: FIRStorageReference get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val ListResult.ios: FIRStorageListResult get() = compat.native as FIRStorageListResult

public actual class File(public val url: NSURL)

public actual class Data(public val data: NSData)

public actual fun ByteArray.toData(): Data = Data(toNSData())

public actual fun Data.toByteArray(): ByteArray = data.toByteArray()

internal actual fun CompatStorageReference.uploadFile(file: File, metadata: CompatStorageMetadata?): UploadTask = putFileUrl(file.url, metadata)
