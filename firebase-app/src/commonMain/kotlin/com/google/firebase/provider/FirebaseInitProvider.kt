/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.provider

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import dev.gitlive.firebase.CONTENT_PROVIDER_ANDROID_ONLY

/**
 * Android only: the `android.content.ContentProvider` the Firebase Android SDK registers in the manifest to initialize the
 * default [com.google.firebase.FirebaseApp] on startup. Declared so that common code referring to it fails with a message
 * saying so, rather than with an unresolved reference; on Android the real provider is used (see buildSrc
 * utils/HeaderStubs.kt). Common code initializes Firebase with `Firebase.initialize(context)` or
 * `FirebaseApp.initializeApp(context)`.
 */
@Deprecated(CONTENT_PROVIDER_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class FirebaseInitProvider {
    public fun onCreate(): Boolean = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
    public fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
    public fun getType(uri: Uri): String? = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
    public fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
    public fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
    public fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException(CONTENT_PROVIDER_ANDROID_ONLY)
}
