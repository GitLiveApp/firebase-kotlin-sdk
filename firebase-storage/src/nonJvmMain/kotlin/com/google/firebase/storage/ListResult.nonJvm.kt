/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

public actual class ListResult internal constructor(
    public actual val items: List<StorageReference>,
    public actual val prefixes: List<StorageReference>,
    public actual val pageToken: String?,
) {
    override fun toString(): String = "ListResult(items=$items, prefixes=$prefixes, pageToken=$pageToken)"
}
