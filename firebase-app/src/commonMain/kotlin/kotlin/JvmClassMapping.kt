/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

package kotlin

import dev.gitlive.firebase.KCLASS_JAVA_JVM_ONLY
import kotlin.reflect.KClass

/*
 * Stand-ins for the JVM standard library's `KClass.java` and `Any.javaClass`, the usual way Android code obtains the
 * `java.lang.Class` it passes to the Firebase Android SDK's reflective reads (`toObject(Book::class.java)`). Declared so that
 * such a call in common code fails with a message naming the reified reads, rather than with an unresolved `java`. On Android
 * and the JVM these are header stubs that are stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses
 * the standard library's.
 */

@Deprecated(KCLASS_JAVA_JVM_ONLY, level = DeprecationLevel.ERROR)
public val <T : Any> KClass<T>.java: java.lang.Class<T> get() = throw UnsupportedOperationException(KCLASS_JAVA_JVM_ONLY)

@Deprecated(KCLASS_JAVA_JVM_ONLY, level = DeprecationLevel.ERROR)
public val <T : Any> T.javaClass: java.lang.Class<T> get() = throw UnsupportedOperationException(KCLASS_JAVA_JVM_ONLY)
