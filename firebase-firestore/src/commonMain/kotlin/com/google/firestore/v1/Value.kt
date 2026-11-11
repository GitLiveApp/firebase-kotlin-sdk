/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firestore.v1

import dev.gitlive.firebase.firestore.PROTO_VALUE_ANDROID_ONLY

/**
 * Stands in for the Android SDK's protobuf `com.google.firestore.v1.Value` so that the pipeline API members taking or
 * returning one (`AbstractOptions.with(String, Value)`, `Stage.withOption(String, Value)`, `Ordering.Direction.proto`) can be
 * declared in common code and deprecated with an error naming the typed overloads. A value of this type cannot exist in
 * common code: the class has no members and cannot be constructed. On Android and the JVM the class is a header stub that is
 * stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the SDK's.
 */
@Deprecated(PROTO_VALUE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class Value private constructor()
