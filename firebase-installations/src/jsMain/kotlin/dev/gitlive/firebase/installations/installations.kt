package dev.gitlive.firebase.installations

import dev.gitlive.firebase.*
import dev.gitlive.firebase.externals.installations.*
import kotlinx.coroutines.await

actual val Firebase.installations
    get() = rethrow { FirebaseInstallations(getInstallations()) }

actual fun Firebase.installations(app: FirebaseApp) =
    rethrow { FirebaseInstallations(getInstallations(app.js)) }

actual class FirebaseInstallations internal constructor(val js: Installations) {

    actual suspend fun delete() = rethrow { delete(js).await() }

    actual suspend fun getId(): String = rethrow { getId(js).await() }

    actual suspend fun getToken(forceRefresh: Boolean): String =
        rethrow { getToken(js, forceRefresh).await() }
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
