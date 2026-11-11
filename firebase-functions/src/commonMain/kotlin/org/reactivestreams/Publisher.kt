/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package org.reactivestreams

import dev.gitlive.firebase.functions.PUBLISHER_JVM_ONLY

/**
 * Stands in for `org.reactivestreams.Publisher` so that the Android SDK's `HttpsCallableReference.stream()` can be declared
 * in common code and deprecated with an error saying that streaming is Android/JVM only. A value of this type cannot exist in
 * common code: the interface has no members and nothing implements it. On Android and the JVM the interface is a header stub
 * that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses Reactive Streams'.
 */
@Deprecated(PUBLISHER_JVM_ONLY, level = DeprecationLevel.ERROR)
public interface Publisher<T>
