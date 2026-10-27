/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.net

import dev.gitlive.firebase.functions.URL_JVM_ONLY

/**
 * Stands in for `java.net.URL` so that the Firebase Android SDK members taking one can be declared in common code and
 * deprecated with an error naming their `String` counterparts. `java.net.URL` is JVM-only: a value of this type cannot
 * exist in common code, so the class has no members and cannot be constructed. On Android and the JVM the class is a
 * header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(URL_JVM_ONLY, level = DeprecationLevel.ERROR)
public class URL private constructor()
