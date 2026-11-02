/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

import dev.gitlive.firebase.FILE_JVM_ONLY

/**
 * Stands in for `java.io.File` so that the Firebase Android SDK members taking one (`StorageReference.getFile(File)`)
 * can be declared in common code and deprecated with an error naming their multiplatform counterparts: a browser has no
 * files to download to. A value of this type cannot exist in common code: the class has no members and cannot be
 * constructed. On Android and the JVM the class is a header stub that is stripped from the output (see buildSrc
 * utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(FILE_JVM_ONLY, level = DeprecationLevel.ERROR)
public class File private constructor()
