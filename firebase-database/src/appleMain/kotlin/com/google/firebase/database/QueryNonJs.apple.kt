/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

public actual fun Query.keepSynced(keepSynced: Boolean) {
    keepSyncedValue(keepSynced)
}
