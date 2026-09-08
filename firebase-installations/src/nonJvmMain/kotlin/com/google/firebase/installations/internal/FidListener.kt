/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations.internal

public actual fun interface FidListener {
    public actual fun onFidChanged(fid: String)
}

public actual interface FidListenerHandle {
    public actual fun unregister()
}
