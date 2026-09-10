/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRMultiFactorResolver
import dev.gitlive.firebase.FirebaseException
import platform.Foundation.NSError

public actual open class FirebaseAuthMultiFactorException(message: String, public val resolver: FIRMultiFactorResolver?, code: String? = null) : FirebaseAuthException(message, code)

internal actual fun NSError.toMultiFactorException(): FirebaseException? {
    val resolver = userInfo["FIRAuthErrorUserInfoMultiFactorResolverKey"] as? FIRMultiFactorResolver
    return FirebaseAuthMultiFactorException(toString(), resolver, code.toString())
}
