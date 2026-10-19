/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/** A page of the items and prefixes (folders) under a reference, as the Android SDK's `ListResult`. */
public expect class ListResult {
    /** The objects in this page. */
    public val items: List<StorageReference>

    /** The token of the next page, or null on the last page. */
    public val pageToken: String?

    /** The prefixes (folders) in this page. */
    public val prefixes: List<StorageReference>
}
