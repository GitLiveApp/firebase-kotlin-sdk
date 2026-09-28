/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL

actual fun temporaryFile(name: String): Any = NSURL.fileURLWithPath(NSTemporaryDirectory() + name)
