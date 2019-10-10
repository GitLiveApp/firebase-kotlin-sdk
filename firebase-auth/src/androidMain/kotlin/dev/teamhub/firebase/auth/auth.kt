package dev.teamhub.firebase.auth

import kotlinx.coroutines.tasks.await

actual fun getFirebaseAuth() = FirebaseAuth.getInstance()

actual typealias FirebaseAuth = com.google.firebase.auth.FirebaseAuth

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseAuth.currentUser: FirebaseUser?
    get() = TODO("not implemented")

actual suspend fun FirebaseAuth.awaitSignInWithCustomToken(token: String) = signInWithCustomToken(token).await()

actual typealias AuthStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener

actual typealias AuthResult = com.google.firebase.auth.AuthResult

actual val AuthResult.user: FirebaseUser
    get() = user

actual typealias FirebaseUser = com.google.firebase.auth.FirebaseUser

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseUser.uid: String
    get() = TODO("not implemented")

actual suspend fun FirebaseAuth.awaitSignInAnonymously() = signInAnonymously().await()

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseUser.isAnonymous: Boolean
    get() = TODO("not implemented")

actual suspend fun FirebaseUser.awaitDelete() = delete().await().run { Unit }

actual suspend fun FirebaseUser.awaitReload() = reload().await().run { Unit }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseAuth.addAuthStateListener(listener: AuthStateListener) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseAuth.removeAuthStateListener(listener: AuthStateListener) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual suspend fun FirebaseAuth.signOut() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException

