package dev.teamhub.firebase.auth

import dev.teamhub.firebase.*
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

actual val Firebase.auth
    get() = rethrow { dev.teamhub.firebase.auth; FirebaseAuth(firebase.auth()) }

actual fun Firebase.auth(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.auth; FirebaseAuth(firebase.auth(app.js)) }

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

actual open class FirebaseAuthException(code: String?, cause: Throwable): FirebaseException(code, cause)
actual open class FirebaseAuthActionCodeException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthEmailException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthInvalidCredentialsException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthInvalidUserException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthRecentLoginRequiredException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthUserCollisionException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthWebException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)

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

private fun errorToException(cause: Throwable) = when(val code = cause.asDynamic().code as String?) {
    "auth/invalid-user-token" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/requires-recent-login" -> FirebaseAuthRecentLoginRequiredException(code, cause)
    "auth/user-disabled" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/user-token-expired" -> FirebaseAuthInvalidUserException(code, cause)
    "auth/web-storage-unsupported" -> FirebaseAuthWebException(code, cause)
    "auth/network-request-failed" -> FirebaseNetworkException(code, cause)
//                "auth/app-deleted" ->
//                "auth/app-not-authorized" ->
//                "auth/argument-error" ->
//                "auth/invalid-api-key" ->
//                "auth/operation-not-allowed" ->
//                "auth/too-many-arguments" ->
//                "auth/unauthorized-domain" ->
    else -> FirebaseAuthException(code, cause)
}
