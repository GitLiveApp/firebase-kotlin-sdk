/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")

package dev.gitlive.firebase.firestore

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = ""

@Suppress("UNCHECKED_CAST")
actual fun encodedAsMap(encoded: Any?): Map<String, Any?> = encoded as Map<String, Any?>
actual fun Map<String, Any?>.asEncoded(): Any = this
