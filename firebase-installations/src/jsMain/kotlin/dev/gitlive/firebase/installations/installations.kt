package dev.gitlive.firebase.installations

import dev.gitlive.firebase.*
import kotlinx.coroutines.await

actual val Firebase.installations
    get() = rethrow {
        dev.gitlive.firebase.installations
        FirebaseInstallations(firebase.installations())
    }

actual fun Firebase.installations(app: FirebaseApp) =
    rethrow {
        dev.gitlive.firebase.installations
        FirebaseInstallations(firebase.installations(app.js))
    }

actual class FirebaseInstallations internal constructor(val js: firebase.installations.Installations) {

    actual suspend fun delete() = rethrow { js.delete().await() }

    actual suspend fun getId(): String = rethrow { js.getId().await() }

    actual suspend fun getToken(forceRefresh: Boolean): String =
        rethrow { js.getToken(forceRefresh).await() }
}

actual open class FirebaseInstallationsException(code: String?, cause: Throwable): FirebaseException(code, cause)

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.installations.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw FirebaseInstallationsException(e.code as String?, e)
    }
}
