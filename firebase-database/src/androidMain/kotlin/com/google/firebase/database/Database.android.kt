/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("DatabaseKt")
@file:JvmMultifileClass

package com.google.firebase.database

// The SDK's own reified getValue, inlined into its callers: the anonymous GenericTypeIndicator captures T for the SDK's
// reflective mapper (so the facade part is only a header stub).

public actual inline fun <reified T> DataSnapshot.getValue(): T? = getValue(object : GenericTypeIndicator<T>() {})

public actual inline fun <reified T> MutableData.getValue(): T? = getValue(object : GenericTypeIndicator<T>() {})
