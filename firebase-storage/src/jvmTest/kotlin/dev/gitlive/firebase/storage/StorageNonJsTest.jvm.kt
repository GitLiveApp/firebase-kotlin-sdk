/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

actual fun temporaryFile(name: String): Any = java.io.File.createTempFile(name, null)
