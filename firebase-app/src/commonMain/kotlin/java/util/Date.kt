/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.util

import dev.gitlive.firebase.DATE_JVM_ONLY

/**
 * Stands in for `java.util.Date` so that the Firebase Android SDK members taking or returning one (`Timestamp(Date)`,
 * `Timestamp.toDate()`) can be declared in common code and deprecated with an error naming their `kotlin.time.Instant`
 * counterparts. A value of this type cannot exist in common code: the class has no members and cannot be constructed.
 * On Android and the JVM the class is a header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt),
 * so code there uses the JDK's.
 */
@Deprecated(DATE_JVM_ONLY, level = DeprecationLevel.ERROR)
public class Date private constructor()
