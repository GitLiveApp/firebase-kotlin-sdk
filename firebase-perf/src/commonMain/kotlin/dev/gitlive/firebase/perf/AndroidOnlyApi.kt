/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

// Deprecation messages of the com.google.firebase.perf members that only Android can provide.
internal const val TRACE_PARCELABLE_ANDROID_ONLY =
    "describeContents, writeToParcel and CREATOR implement android.os.Parcelable, which is Android-only; a Trace is not parcelled on the other platforms"
