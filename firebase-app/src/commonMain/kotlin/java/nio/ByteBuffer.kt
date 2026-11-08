/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.nio

import dev.gitlive.firebase.BYTE_BUFFER_JVM_ONLY

/**
 * Stands in for `java.nio.ByteBuffer` so that the Firebase Android SDK members taking one (`FirebaseFirestore.loadBundle(ByteBuffer)`)
 * can be declared in common code and deprecated with an error naming their multiplatform counterparts. A value of this
 * type cannot exist in common code: the class has no members and cannot be constructed. On Android and the JVM the class
 * is a header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(BYTE_BUFFER_JVM_ONLY, level = DeprecationLevel.ERROR)
public class ByteBuffer private constructor()
