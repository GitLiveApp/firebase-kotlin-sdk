/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("QueryNonJsKt")

package com.google.firebase.database

// Shipped (see keepClasses in the build file): binds common code to the SDK's keepSynced member.

public actual fun Query.keepSynced(keepSynced: Boolean) {
    keepSynced(keepSynced)
}
