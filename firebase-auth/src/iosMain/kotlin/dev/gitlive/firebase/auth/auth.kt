/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRAuthErrorDomain
import cocoapods.FirebaseAuth.FIRMultiFactorResolver
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import platform.Foundation.NSError

public actual open class FirebaseAuthMultiFactorException(message: String, public val resolver: FIRMultiFactorResolver?) : FirebaseAuthException(message)

internal actual fun NSError.toAuthException() = when (domain) {
    // codes from AuthErrors.swift: https://github.com/firebase/firebase-ios-sdk/blob/
    // 2f6ac4c2c61cd57c7ea727009e187b7e1163d613/FirebaseAuth/Sources/Swift/Utilities/
    // AuthErrors.swift#L51
    FIRAuthErrorDomain -> when (code) {
        17030L, // AuthErrorCode.invalidActionCode
        17029L, // AuthErrorCode.expiredActionCode
        -> FirebaseAuthActionCodeException(toString())

        17008L, // AuthErrorCode.invalidEmail
        -> FirebaseAuthEmailException(toString())

        17056L, // AuthErrorCode.captchaCheckFailed
        17042L, // AuthErrorCode.invalidPhoneNumber
        17041L, // AuthErrorCode.missingPhoneNumber
        17046L, // AuthErrorCode.invalidVerificationID
        17044L, // AuthErrorCode.invalidVerificationCode
        17045L, // AuthErrorCode.missingVerificationID
        17043L, // AuthErrorCode.missingVerificationCode
        17021L, // AuthErrorCode.userTokenExpired
        17004L, // AuthErrorCode.invalidCredential
        -> FirebaseAuthInvalidCredentialsException(toString())

        17026L, // AuthErrorCode.weakPassword
        -> FirebaseAuthWeakPasswordException(toString())

        17017L, // AuthErrorCode.invalidUserToken
        -> FirebaseAuthInvalidUserException(toString())

        17014L, // AuthErrorCode.requiresRecentLogin
        -> FirebaseAuthRecentLoginRequiredException(toString())

        17087L, // AuthErrorCode.secondFactorAlreadyEnrolled
        17078L, // AuthErrorCode.secondFactorRequired
        17088L, // AuthErrorCode.maximumSecondFactorCountExceeded
        17084L, // AuthErrorCode.multiFactorInfoNotFound
        -> {
            val resolver = userInfo["FIRAuthErrorUserInfoMultiFactorResolverKey"] as? FIRMultiFactorResolver
            FirebaseAuthMultiFactorException(toString(), resolver)
        }

        17052L, // AuthErrorCode.quotaExceeded
        -> FirebaseTooManyRequestsException(toString())

        17007L, // AuthErrorCode.emailAlreadyInUse
        17012L, // AuthErrorCode.accountExistsWithDifferentCredential
        17025L, // AuthErrorCode.credentialAlreadyInUse
        -> FirebaseAuthUserCollisionException(toString())

        17057L, // AuthErrorCode.webContextAlreadyPresented
        17058L, // AuthErrorCode.webContextCancelled
        17062L, // AuthErrorCode.webInternalError
        -> FirebaseAuthWebException(toString())

        17020L, // AuthErrorCode.networkError
        -> FirebaseNetworkException(toString())

        else -> FirebaseAuthException(toString())
    }
    else -> FirebaseAuthException(toString())
}
