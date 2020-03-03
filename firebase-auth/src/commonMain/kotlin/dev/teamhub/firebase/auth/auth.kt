@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.teamhub.firebase.auth

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
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

