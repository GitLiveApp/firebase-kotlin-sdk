/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.serialization.decodeFromPlainValue

// Reads through the serializer of T with the Android SDK's rules (see databaseFormat); `Any` stands for the value as it is.

public actual inline fun <reified T> DataSnapshot.getValue(): T? = databaseFormat.decodeFromPlainValue<T?>(value)

public actual inline fun <reified T> MutableData.getValue(): T? = databaseFormat.decodeFromPlainValue<T?>(value)
