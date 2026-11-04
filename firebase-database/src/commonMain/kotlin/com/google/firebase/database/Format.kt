/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.serialization.FirebaseFormat

/**
 * The format that maps a `@Serializable` class to and from the database's values on every platform, with the Android
 * SDK's Realtime Database rules and the format's own mapping annotations (see [FirebaseFormat.Database]): the
 * `setData`/`updateData` extensions write through [FirebaseFormat.convertToPlainValue] and the reified `getValue` reads
 * through [FirebaseFormat.decodeFromPlainValue] (all in `com.google.firebase.serialization`, see DatabaseMapping.kt). It
 * takes the place of the Android SDK's reflective mapping, whose members are deprecated with an error.
 */
@PublishedApi
internal val databaseFormat: FirebaseFormat.Database = FirebaseFormat.Database(exception = { DatabaseException(it) })
