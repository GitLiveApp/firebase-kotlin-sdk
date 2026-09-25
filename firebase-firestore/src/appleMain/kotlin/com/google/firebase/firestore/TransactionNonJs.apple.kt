/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

public actual fun Transaction.get(documentRef: DocumentReference): DocumentSnapshot = getDocument(documentRef)
