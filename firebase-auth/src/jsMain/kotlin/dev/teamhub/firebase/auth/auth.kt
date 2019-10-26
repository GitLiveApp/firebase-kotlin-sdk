package dev.teamhub.firebase.auth

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.FirebaseNetworkException
import dev.teamhub.firebase.common.firebase
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

actual val Firebase.auth
    get() = rethrow { dev.teamhub.firebase.common.auth; FirebaseAuth(firebase.auth()) }

actual fun Firebase.auth(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.common.auth; FirebaseAuth(firebase.auth(app.js)) }

actual class FirebaseAuth internal constructor(val js: firebase.auth.Auth) {

    actual val currentUser: FirebaseUser?
        get() = rethrow { js.currentUser?.let { FirebaseUser(it) } }

    actual suspend fun signInWithCustomToken(token: String)
            = rethrow { AuthResult(js.signInWithCustomToken(token).await()) }

    actual suspend fun signInAnonymously()
            = rethrow { AuthResult(js.signInAnonymously().await()) }

    actual suspend fun signOut() = rethrow { js.signOut().await() }

    actual val authStateChanged get() = callbackFlow {
        val unsubscribe = js.onAuthStateChanged {
            offer(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }
}

actual class AuthResult internal constructor(val js: firebase.auth.AuthResult) {
    actual val user: FirebaseUser?
        get() = rethrow { js.user?.let { FirebaseUser(it) } }
}

actual class FirebaseUser internal constructor(val js: firebase.user.User) {
    actual val uid: String
        get() = rethrow { js.uid }
    actual val isAnonymous: Boolean
        get() = rethrow { js.isAnonymous }
    actual suspend fun delete() = rethrow { js.delete().await() }
    actual suspend fun reload() = rethrow { js.reload().await() }
}

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