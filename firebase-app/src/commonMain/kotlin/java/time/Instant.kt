/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.time

import dev.gitlive.firebase.JAVA_INSTANT_JVM_ONLY

/**
 * Stands in for `java.time.Instant` so that the Firebase Android SDK members taking or returning one
 * (`Timestamp(Instant)`, `Timestamp.toInstant()`) can be declared in common code and deprecated with an error naming
 * their `kotlin.time.Instant` counterparts. A value of this type cannot exist in common code: the class has no members
 * and cannot be constructed. On Android and the JVM the class is a header stub that is stripped from the output (see
 * buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(JAVA_INSTANT_JVM_ONLY, level = DeprecationLevel.ERROR)
public class Instant private constructor()
