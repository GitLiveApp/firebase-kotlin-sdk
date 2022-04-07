/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

val FirebaseUser.multiFactor: MultiFactor
    get() = MultiFactor(native.multiFactor)

suspend fun FirebaseUser.updatePhoneNumber(credential: PhoneAuthCredential) = native.await { updatePhoneNumberCredential(credential.ios, it) }.run { Unit }
