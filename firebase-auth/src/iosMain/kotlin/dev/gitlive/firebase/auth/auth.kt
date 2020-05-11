/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.*


actual val Firebase.auth
    get() = FirebaseAuth(FIRAuth.auth())

actual fun Firebase.auth(app: FirebaseApp) =
    FirebaseAuth(FIRAuth.authWithApp(app.ios))

actual class FirebaseAuth internal constructor(val ios: FIRAuth) {

    actual val currentUser: FirebaseUser?
        get() = ios.currentUser?.let { FirebaseUser(it) }

    actual suspend fun sendPasswordResetEmail(email: String) {
        ios.await { sendPasswordResetWithEmail(email = email, completion = it) }
    }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        AuthResult(ios.awaitResult { signInWithEmail(email = email, password = password, completion = it) })

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        AuthResult(ios.awaitResult { createUserWithEmail(email = email, password = password, completion = it) })

    actual suspend fun signInWithCustomToken(token: String) =
        AuthResult(ios.awaitResult { signInWithCustomToken(token, it) })

    actual suspend fun signInAnonymously() =
        AuthResult(ios.awaitResult { signInAnonymouslyWithCompletion(it) })

    actual suspend fun signOut() = ios.throwError { signOut(it) }.run { Unit }

    actual val authStateChanged get() = callbackFlow {
        val handle = ios.addAuthStateDidChangeListener { _, user -> offer(user?.let { FirebaseUser(it) }) }
        awaitClose { ios.removeAuthStateDidChangeListener(handle) }
    }
}

actual class AuthResult internal constructor(val ios: FIRAuthDataResult) {
    actual val user: FirebaseUser?
        get() = FirebaseUser(ios.user)
}

actual class FirebaseUser internal constructor(val ios: FIRUser) {
    actual val uid: String
        get() = ios.uid
    actual val displayName: String?
        get() = ios.displayName
    actual val email: String?
        get() = ios.email
    actual val phoneNumber: String?
        get() = ios.phoneNumber
    actual val isAnonymous: Boolean
        get() = ios.isAnonymous()
    actual suspend fun delete() = ios.await { deleteWithCompletion(it) }.run { Unit }
    actual suspend fun reload() = ios.await { reloadWithCompletion(it) }.run { Unit }
}

actual open class FirebaseAuthException(message: String): FirebaseException(message)
actual open class FirebaseAuthActionCodeException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthEmailException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthInvalidCredentialsException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthInvalidUserException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthRecentLoginRequiredException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthUserCollisionException(message: String): FirebaseAuthException(message)
actual open class FirebaseAuthWebException(message: String): FirebaseAuthException(message)


private fun <T, R> T.throwError(block: T.(errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
    memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val result = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value
        if (error != null) {
            throw error.toException()
        }
        return result
    }
}

private suspend fun <T, R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R>()
    function { result, error ->
        if(result != null) {
            job.complete(result)
        } else if(error != null) {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await()
}

private suspend fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
}

private fun NSError.toException() = when(domain) {
    FIRAuthErrorDomain -> when(code) {
        FIRAuthErrorCodeInvalidActionCode,
        FIRAuthErrorCodeExpiredActionCode -> FirebaseAuthActionCodeException(toString())

        FIRAuthErrorCodeInvalidEmail,
        FIRAuthErrorCodeEmailAlreadyInUse -> FirebaseAuthEmailException(toString())

        FIRAuthErrorCodeInvalidCredential -> FirebaseAuthInvalidCredentialsException(toString())

        FIRAuthErrorCodeInvalidUserToken -> FirebaseAuthInvalidUserException(toString())

        FIRAuthErrorCodeRequiresRecentLogin -> FirebaseAuthRecentLoginRequiredException(toString())

        FIRAuthErrorCodeEmailAlreadyInUse,
        FIRAuthErrorCodeAccountExistsWithDifferentCredential,
        FIRAuthErrorCodeCredentialAlreadyInUse -> FirebaseAuthUserCollisionException(toString())

        FIRAuthErrorCodeWebContextAlreadyPresented,
        FIRAuthErrorCodeWebContextCancelled,
        FIRAuthErrorCodeWebInternalError -> FirebaseAuthWebException(toString())

        else -> FirebaseAuthException(toString())
    }
    else -> FirebaseAuthException(toString())
}
