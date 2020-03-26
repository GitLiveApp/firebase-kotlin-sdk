/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow

expect val Firebase.auth: FirebaseAuth

expect fun Firebase.auth(app: FirebaseApp): FirebaseAuth

expect class FirebaseAuth {
    val currentUser: FirebaseUser?
    val authStateChanged: Flow<FirebaseUser?>
    suspend fun signInWithCustomToken(token: String): AuthResult
    suspend fun signInAnonymously(): AuthResult
    suspend fun signOut()
}

expect class AuthResult {
    val user: FirebaseUser?
}

expect class FirebaseUser {
    val uid: String
    val isAnonymous: Boolean
    suspend fun delete()
    suspend fun reload()
}

expect open class FirebaseAuthException: FirebaseException
expect class FirebaseAuthActionCodeException: FirebaseAuthException
expect class FirebaseAuthEmailException: FirebaseAuthException
expect class FirebaseAuthInvalidCredentialsException: FirebaseAuthException
expect class FirebaseAuthInvalidUserException: FirebaseAuthException
expect class FirebaseAuthRecentLoginRequiredException: FirebaseAuthException
expect class FirebaseAuthUserCollisionException: FirebaseAuthException
expect class FirebaseAuthWebException: FirebaseAuthException

