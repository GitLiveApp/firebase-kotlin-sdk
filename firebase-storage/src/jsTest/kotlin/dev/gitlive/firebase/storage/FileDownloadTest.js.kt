/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import android.net.Uri

/** A name only: the JS SDK cannot download to it. */
actual fun temporaryFile(name: String): Uri = Uri.parse(name)

actual val supportsFileDownloads: Boolean = false

actual val supportsFileUploads: Boolean = false
