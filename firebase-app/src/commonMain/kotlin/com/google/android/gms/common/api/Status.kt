/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.android.gms.common.api

import dev.gitlive.firebase.STATUS_ANDROID_ONLY

/**
 * Stands in for Play Services' `com.google.android.gms.common.api.Status` so that the Firebase Android SDK members taking one
 * (`StorageException.fromErrorStatus`) can be declared in common code and deprecated with an error. A value of this type
 * cannot exist in common code: the class has no members and cannot be constructed. On Android and the JVM the class is a
 * header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses Play Services'.
 */
@Deprecated(STATUS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class Status private constructor()
