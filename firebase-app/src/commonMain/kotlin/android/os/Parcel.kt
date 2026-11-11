/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.os

import dev.gitlive.firebase.PARCEL_ANDROID_ONLY

/**
 * Stands in for `android.os.Parcel` so that the Firebase Android SDK's `Parcelable` implementations (`Timestamp`, `Trace`,
 * `RemoteMessage`) can declare `writeToParcel(Parcel, Int)` in common code, deprecated with an error. A value of this type
 * cannot exist in common code: the class has no members and cannot be constructed. On Android the class is a header stub that
 * is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(PARCEL_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class Parcel private constructor()
