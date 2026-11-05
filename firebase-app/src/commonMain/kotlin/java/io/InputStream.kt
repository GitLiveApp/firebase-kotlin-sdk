/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

import dev.gitlive.firebase.INPUT_STREAM_JVM_ONLY

/**
 * Stands in for `java.io.InputStream` so that the Firebase Android SDK members taking one (`FirebaseFirestore.loadBundle(InputStream)`)
 * can be declared in common code and deprecated with an error naming their multiplatform counterparts, which take a
 * kotlinx-io `RawSource` (`loadBundle(RawSource)`, `putStream(RawSource)`). A value of this type cannot exist in common
 * code: the class has no members and cannot be constructed. On Android and the JVM the class
 * is a header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(INPUT_STREAM_JVM_ONLY, level = DeprecationLevel.ERROR)
public class InputStream private constructor()
