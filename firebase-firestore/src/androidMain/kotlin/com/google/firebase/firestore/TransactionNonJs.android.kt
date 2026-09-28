/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("TransactionNonJsKt")

package com.google.firebase.firestore

// Shipped (see keepClasses in the build file): binds to the SDK's Transaction.get, which the stub declares as an extra member.
public actual fun Transaction.get(documentRef: DocumentReference): DocumentSnapshot = get(documentRef)
