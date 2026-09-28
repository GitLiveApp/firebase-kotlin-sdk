/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/*
 * Keeping a query synchronized while offline, which the JS SDK does not have. On Android the SDK's member binds for
 * Android code (a member wins over an extension) and this file's facade is shipped for common code.
 */

/** Keeps the data of this query synchronized in the persistent cache, even while it has no listener. */
public expect fun Query.keepSynced(keepSynced: Boolean)
