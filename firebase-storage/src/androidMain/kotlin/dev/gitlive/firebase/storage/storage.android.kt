/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.storage

import android.net.Uri
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.StorageMetadata as CompatStorageMetadata
import com.google.firebase.storage.StorageReference as CompatStorageReference

/** The underlying Firebase Android SDK object. */
public val FirebaseStorage.android: com.google.firebase.storage.FirebaseStorage get() = compat

/** The underlying Firebase Android SDK object. */
public val StorageReference.android: com.google.firebase.storage.StorageReference get() = compat

/** The underlying Firebase Android SDK object. */
public val ListResult.android: com.google.firebase.storage.ListResult get() = compat

public actual class File(public val uri: Uri)

public actual class Data(public val data: ByteArray)

public actual fun ByteArray.toData(): Data = Data(copyOf())

public actual fun Data.toByteArray(): ByteArray = data.copyOf()

internal actual fun CompatStorageReference.uploadFile(file: File, metadata: CompatStorageMetadata?): UploadTask = if (metadata != null) putFile(file.uri, metadata) else putFile(file.uri)
