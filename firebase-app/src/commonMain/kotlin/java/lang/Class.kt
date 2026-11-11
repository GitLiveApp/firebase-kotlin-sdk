/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.lang

import dev.gitlive.firebase.CLASS_JVM_ONLY

/**
 * Stands in for `java.lang.Class` so that the Firebase Android SDK members taking one (`DocumentSnapshot.toObject(Class)`,
 * `DataSnapshot.getValue(Class)`, `Task.getResult(Class)`) can be declared in common code and deprecated with an error naming
 * their multiplatform counterparts, the reified reads in `com.google.firebase.serialization`. A value of this type cannot
 * exist in common code: the class has no members and cannot be constructed, and `Foo::class.java` and `javaClass`, the usual
 * ways to obtain one, are error-deprecated stand-ins too (kotlin/JvmClassMapping.kt). On Android and the JVM the class is a header stub that is
 * stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@kotlin.Deprecated(CLASS_JVM_ONLY, level = kotlin.DeprecationLevel.ERROR) // qualified: java.lang.Deprecated shadows it in this package
public class Class<T> private constructor()
