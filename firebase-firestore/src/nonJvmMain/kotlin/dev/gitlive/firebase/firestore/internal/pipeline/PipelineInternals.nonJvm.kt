/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal.pipeline

import com.google.firebase.firestore.FieldPath

internal actual fun FieldPath.canonicalPath(): String = canonicalString
