/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.time.temporal

import dev.gitlive.firebase.CHRONO_UNIT_JVM_ONLY

/**
 * Stands in for `java.time.temporal.ChronoUnit` so that the Firebase Android SDK's `convertUnit` (in the Firestore pipeline
 * evaluation helpers) can be declared in common code and deprecated with an error. A value of this type cannot exist in
 * common code: the class has no members and cannot be constructed. On Android and the JVM the class is a header stub that is
 * stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's enum.
 */
@Deprecated(CHRONO_UNIT_JVM_ONLY, level = DeprecationLevel.ERROR)
public class ChronoUnit private constructor()
