/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

// The Android, JVM and iOS Cloud Functions SDKs wrap a Long as a protobuf Int64Value themselves.
internal actual val functionsSdkEncodesLongs: Boolean = true
