/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

internal actual fun FirebaseUser.getMultiFactor(): MultiFactor = MultiFactor(ios.multiFactor())

internal actual suspend fun FirebaseUser.updatePhoneNumberInternal(credential: PhoneAuthCredential): Unit = ios.await {
    updatePhoneNumberCredential(credential.ios, it)
}
