/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.content

import dev.gitlive.firebase.CONTENT_PROVIDER_ANDROID_ONLY

/**
 * Stands in for `android.content.ContentValues`, which `FirebaseInitProvider.insert` and `update` take, so that the
 * provider's `ContentProvider` members can be declared in common code and deprecated with an error. A value of this type
 * cannot exist in common code: the class has no members and cannot be constructed. On Android the class is a header stub that
 * is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(CONTENT_PROVIDER_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class ContentValues private constructor()
