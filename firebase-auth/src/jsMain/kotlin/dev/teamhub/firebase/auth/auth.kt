package dev.teamhub.firebase.auth

import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.FirebaseNetworkException
import kotlinx.coroutines.await
import kotlin.js.Promise

actual fun getFirebaseAuth() = rethrow { firebase.auth() }

actual typealias FirebaseAuth = firebase.auth.Auth

actual interface AuthStateListener {
    actual fun onAuthStateChanged(auth: FirebaseAuth)
}

actual val FirebaseAuth.currentUser: FirebaseUser?
    get() = rethrow { currentUser }

actual typealias AuthResult = firebase.auth.AuthResult

actual val AuthResult.user: FirebaseUser
    get() = rethrow { user }


actual typealias FirebaseUser = firebase.user.User

actual val FirebaseUser.uid: String
    get() = rethrow { uid }

actual suspend fun FirebaseAuth.awaitSignInWithCustomToken(token: String) = rethrow { signInWithCustomToken(token).await() }

actual suspend fun FirebaseAuth.awaitSignInAnonymously() = rethrow { Promise.resolve(signInAnonymously()).await() }

actual suspend fun FirebaseAuth.signOut() = rethrow { signOut().await() }

actual val FirebaseUser.isAnonymous: Boolean
    get() = rethrow { isAnonymous }

actual suspend fun FirebaseUser.awaitDelete() = rethrow { delete().await() }

actual suspend fun FirebaseUser.awaitReload() = rethrow { reload().await() }

actual fun FirebaseAuth.addAuthStateListener(listener: AuthStateListener)  = rethrow {
    onAuthStateChanged { listener.onAuthStateChanged(getFirebaseAuth()) }
        .let { listener.asDynamic().unsubscribe = it }
}

actual fun FirebaseAuth.removeAuthStateListener(listener: AuthStateListener) = rethrow { listener.asDynamic().unsubscribe() }

actual open class FirebaseAuthException(code: String?, message: String?): FirebaseException(code, message)
actual open class FirebaseAuthActionCodeException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthEmailException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthInvalidCredentialsException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthInvalidUserException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthRecentLoginRequiredException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthUserCollisionException(code: String?, message: String?): FirebaseAuthException(code, message)
actual open class FirebaseAuthWebException(code: String?, message: String?): FirebaseAuthException(code, message)

private inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.auth.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw errorToException(e)
    }
}

private fun errorToException(e: Throwable) = when(val code = e.asDynamic().code as String?) {
    "auth/invalid-user-token" -> FirebaseAuthInvalidUserException(code, e.message)
    "auth/requires-recent-login" -> FirebaseAuthRecentLoginRequiredException(code, e.message)
    "auth/user-disabled" -> FirebaseAuthInvalidUserException(code, e.message)
    "auth/user-token-expired" -> FirebaseAuthInvalidUserException(code, e.message)
    "auth/web-storage-unsupported" -> FirebaseAuthWebException(code, e.message)
    "auth/network-request-failed" -> FirebaseNetworkException(code, e.message)
//                "auth/app-deleted" ->
//                "auth/app-not-authorized" ->
//                "auth/argument-error" ->
//                "auth/invalid-api-key" ->
//                "auth/operation-not-allowed" ->
//                "auth/too-many-arguments" ->
//                "auth/unauthorized-domain" ->
    else -> FirebaseAuthException(code, e.message)
}