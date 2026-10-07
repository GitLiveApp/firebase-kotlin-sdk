/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("StorageTaskUriKt")

package com.google.firebase.storage

// Shipped (see keepClasses in the build file): the String form of the SDK's Uri member, for common code.

public actual val UploadTask.TaskSnapshot.uploadSessionUri: String? get() = uploadSessionUri?.toString()
