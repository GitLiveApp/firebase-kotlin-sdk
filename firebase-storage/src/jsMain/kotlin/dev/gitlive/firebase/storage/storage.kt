/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.toUint8Array
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import com.google.firebase.storage.StorageMetadata as CompatStorageMetadata
import com.google.firebase.storage.StorageReference as CompatStorageReference
import dev.gitlive.firebase.storage.externals.FirebaseStorage as JsFirebaseStorage
import dev.gitlive.firebase.storage.externals.StorageReference as JsStorageReference

/** The underlying Firebase JS SDK object. */
public val FirebaseStorage.js: JsFirebaseStorage get() = compat.js

/** The underlying Firebase JS SDK object. */
public val StorageReference.js: JsStorageReference get() = compat.js

public actual typealias File = org.w3c.files.File

public actual class Data(public val data: Uint8Array)

public actual fun ByteArray.toData(): Data = Data(copyOf().toUint8Array())

public actual fun Data.toByteArray(): ByteArray = Int8Array(data.buffer, data.byteOffset, data.length).unsafeCast<ByteArray>().copyOf()

internal actual fun CompatStorageReference.uploadFile(file: File, metadata: CompatStorageMetadata?): UploadTask = upload(file, metadata)
