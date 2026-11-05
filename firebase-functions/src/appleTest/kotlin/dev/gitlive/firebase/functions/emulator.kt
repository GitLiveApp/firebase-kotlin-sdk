/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

actual val emulatorHost: String = "localhost"
actual val context: Any = Unit

@Suppress("UNCHECKED_CAST")
actual fun detailValue(details: Any?, key: String): Any? = (details as Map<Any?, Any?>)[key]
