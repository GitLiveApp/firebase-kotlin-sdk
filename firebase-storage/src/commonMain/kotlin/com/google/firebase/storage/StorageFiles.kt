/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/*
 * The Android SDK uploads files given as android.net.Uri, which common code cannot name, so these take the platform's
 * file type as Any: an android.net.Uri or java.io.File on Android and the JVM, an NSURL on Apple platforms, a File or
 * Blob on JS, or a String path (a file URL string on JS) everywhere. On Android the SDK's Uri members bind for Android
 * code (a member wins over an extension) and this file is shipped for common code.
 */

/** Uploads the file [file] (see the platform file types above) to this reference. */
public expect fun StorageReference.putFile(file: Any): UploadTask

/** Uploads the file [file] to this reference with [metadata]. */
public expect fun StorageReference.putFile(file: Any, metadata: StorageMetadata): UploadTask
