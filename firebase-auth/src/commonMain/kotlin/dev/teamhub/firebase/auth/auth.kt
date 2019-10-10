@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.teamhub.firebase.auth

import dev.teamhub.firebase.FirebaseException

expect fun getFirebaseAuth(): FirebaseAuth

expect interface AuthStateListener {
    fun onAuthStateChanged(auth: FirebaseAuth)
}

expect class FirebaseAuth

expect fun FirebaseAuth.addAuthStateListener(listener: AuthStateListener)
expect fun FirebaseAuth.removeAuthStateListener(listener: AuthStateListener)
expect suspend fun FirebaseAuth.signOut()

expect val FirebaseAuth.currentUser: FirebaseUser?

expect suspend fun FirebaseAuth.awaitSignInWithCustomToken(token: String): AuthResult
expect suspend fun FirebaseAuth.awaitSignInAnonymously(): AuthResult

expect interface AuthResult

expect val AuthResult.user: FirebaseUser

expect abstract class FirebaseUser

expect val FirebaseUser.uid: String
expect val FirebaseUser.isAnonymous: Boolean

expect suspend fun FirebaseUser.awaitDelete()
expect suspend fun FirebaseUser.awaitReload()

expect open class FirebaseAuthException: FirebaseException
expect class FirebaseAuthActionCodeException: FirebaseAuthException
expect class FirebaseAuthEmailException: FirebaseAuthException
expect class FirebaseAuthInvalidCredentialsException: FirebaseAuthException
expect class FirebaseAuthInvalidUserException: FirebaseAuthException
expect class FirebaseAuthRecentLoginRequiredException: FirebaseAuthException
expect class FirebaseAuthUserCollisionException: FirebaseAuthException
expect class FirebaseAuthWebException: FirebaseAuthException

