/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.database

import dev.gitlive.firebase.CONTENT_PROVIDER_ANDROID_ONLY

/**
 * Stands in for `android.database.Cursor`, which `FirebaseInitProvider.query` returns, so that the provider's
 * `ContentProvider` members can be declared in common code and deprecated with an error. A value of this type cannot exist
 * in common code: the interface has no members and nothing implements it. On Android the interface is a header stub that is
 * stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the platform's.
 */
@Deprecated(CONTENT_PROVIDER_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public interface Cursor
