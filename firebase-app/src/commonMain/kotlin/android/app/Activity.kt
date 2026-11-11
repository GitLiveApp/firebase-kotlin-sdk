/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.app

import dev.gitlive.firebase.ACTIVITY_ANDROID_ONLY

/**
 * Stands in for `android.app.Activity` so that the Firebase Android SDK members taking one (the `Activity` overloads of the
 * `Task` listeners and of `addSnapshotListener`, `PhoneAuthOptions.Builder.setActivity`, `startActivityForSignInWithProvider`)
 * can be declared in common code and deprecated with an error naming their multiplatform counterparts. A value of this type
 * cannot exist in common code: the class has no members and cannot be constructed. On Android the class is a header stub that
 * is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(ACTIVITY_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class Activity private constructor()
