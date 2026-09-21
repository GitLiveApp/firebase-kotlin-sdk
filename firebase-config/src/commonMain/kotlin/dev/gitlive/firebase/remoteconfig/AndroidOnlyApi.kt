/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

/*
 * Messages of the `@Deprecated(level = ERROR)` members of the `com.google.firebase.remoteconfig` layer: Android SDK
 * members that only make sense on Android. They are declared so that common code using them fails to compile with a
 * message naming the multiplatform replacement, while Android code keeps binding to the real SDK member.
 */

internal const val SET_DEFAULTS_XML_ANDROID_ONLY =
    "setDefaultsAsync(Int) reads an Android XML resource and can only be called from Android code; " +
        "use setDefaultsAsync(Map) from common code"
