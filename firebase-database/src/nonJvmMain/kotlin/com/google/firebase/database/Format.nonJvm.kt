/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.serialization.FirebaseFormat
import com.google.firebase.serialization.MappingAnnotations

/**
 * The format through which the layer exchanges values with the platform SDK where the Android SDK would map a custom
 * class by reflection: a value written with `setValue(Any?)`, `updateChildren` or `MutableData.value` goes through
 * [FirebaseFormat.convertToPlainValue] and the reified `getValue` reads through [FirebaseFormat.decodeFromPlainValue],
 * with the Android SDK's Realtime Database rules and this module's mapping annotations (see [FirebaseFormat.Database]).
 */
@PublishedApi
internal val databaseFormat: FirebaseFormat.Database = FirebaseFormat.Database(
    annotations = MappingAnnotations(
        propertyName = { (it as? PropertyName)?.value },
        exclude = Exclude::class,
        ignoreExtraProperties = IgnoreExtraProperties::class,
        throwOnExtraProperties = ThrowOnExtraProperties::class,
    ),
    exception = { DatabaseException(it) },
)
