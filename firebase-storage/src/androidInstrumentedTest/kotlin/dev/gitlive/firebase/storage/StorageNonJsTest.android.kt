/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import androidx.test.platform.app.InstrumentationRegistry

actual fun temporaryFile(name: String): Any = java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, name)
