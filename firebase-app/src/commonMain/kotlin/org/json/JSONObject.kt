/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package org.json

import dev.gitlive.firebase.JSON_OBJECT_ANDROID_ONLY

/**
 * Stands in for `org.json.JSONObject` so that the Firebase Android SDK members returning one (`MultiFactorInfo.toJson`) can
 * be declared in common code and deprecated with an error. A value of this type cannot exist in common code: the class has no
 * members and cannot be constructed. On Android and the JVM the class is a header stub that is stripped from the output (see
 * buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(JSON_OBJECT_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class JSONObject private constructor()
