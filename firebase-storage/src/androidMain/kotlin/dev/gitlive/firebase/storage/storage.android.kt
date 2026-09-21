/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.storage

import android.net.Uri

/** The underlying Firebase Android SDK object. */
public val FirebaseStorage.android: com.google.firebase.storage.FirebaseStorage get() = compat

/** The underlying Firebase Android SDK object. */
public val StorageReference.android: com.google.firebase.storage.StorageReference get() = compat

/** The underlying Firebase Android SDK object. */
public val ListResult.android: com.google.firebase.storage.ListResult get() = compat

public actual class File(public val uri: Uri)

public actual class Data(public val data: ByteArray)

internal actual fun Data.toByteArray(): ByteArray = data

internal actual fun ByteArray.toData(): Data = Data(this)

internal actual val File.platformFile: Any get() = uri
