/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.os

import dev.gitlive.firebase.PARCEL_ANDROID_ONLY

/**
 * Stands in for `android.os.Parcelable` so that the `CREATOR` fields of the Firebase Android SDK's `Parcelable`
 * implementations (`Timestamp`, `Trace`) can be declared in common code with their `Parcelable.Creator` type, deprecated with
 * an error. Nothing implements it in common code. On Android the interface is a header stub that is stripped from the output
 * (see buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(PARCEL_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public interface Parcelable {
    /** The type of the `CREATOR` field of a `Parcelable` class. */
    @Suppress("DEPRECATION_ERROR")
    public interface Creator<T>
}
