/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlin.js.Json

actual interface EncodedObject {
    actual val raw: Map<String, Any?>
    val json: Json
}
