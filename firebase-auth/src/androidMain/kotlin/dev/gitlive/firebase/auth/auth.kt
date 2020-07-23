/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual val Firebase.auth
    get() = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance())

actual fun Firebase.auth(app: FirebaseApp) =
    FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance(app.android))

actual class FirebaseAuth internal constructor(val android: com.google.firebase.auth.FirebaseAuth) {
    actual val currentUser: FirebaseUser?
        get() = android.currentUser?.let { FirebaseUser(it) }

    actual suspend fun sendPasswordResetEmail(email: String) {
        android.sendPasswordResetEmail(email).await()
    }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        AuthResult(android.signInWithEmailAndPassword(email, password).await())

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        AuthResult(android.createUserWithEmailAndPassword(email, password).await())

    actual suspend fun signInWithCustomToken(token: String) =
        AuthResult(android.signInWithCustomToken(token).await())

    actual suspend fun signInAnonymously() = AuthResult(android.signInAnonymously().await())

    actual val authStateChanged get() = callbackFlow {
        val listener = object : AuthStateListener {
            override fun onAuthStateChanged(auth: com.google.firebase.auth.FirebaseAuth) {
                offer(auth.currentUser?.let { FirebaseUser(it) })
            }
        }
        android.addAuthStateListener(listener)
        awaitClose { android.removeAuthStateListener(listener) }
    }
    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        AuthResult(android.signInWithCredential(authCredential.android).await())

    actual suspend fun signOut() = android.signOut()
}

actual class AuthCredential(val android: com.google.firebase.auth.AuthCredential)

actual class AuthResult internal constructor(val android: com.google.firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = android.user?.let { FirebaseUser(it) }
}

actual class FirebaseUser internal constructor(val android: com.google.firebase.auth.FirebaseUser) {
    actual val uid: String
        get() = android.uid
    actual val displayName: String?
        get() = android.displayName
    actual val email: String?
        get() = android.email
    actual val phoneNumber: String?
        get() = android.phoneNumber
    actual val isAnonymous: Boolean
        get() = android.isAnonymous
    actual suspend fun delete() = android.delete().await().run { Unit }
    actual suspend fun reload() = android.reload().await().run { Unit }
    actual suspend fun sendEmailVerification() = android.sendEmailVerification().await().run { Unit }
}

actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException

actual object EmailAuthProvider {
    actual fun credentialWithEmail(
        email: String,
        password: String
    ): AuthCredential = AuthCredential(EmailAuthProvider.getCredential(email, password))
}