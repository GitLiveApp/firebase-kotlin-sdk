package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.externals.awaitUnit
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.installations.externals.*
import dev.gitlive.firebase.js
import kotlin.js.JsException

public actual val Firebase.installations: FirebaseInstallations
    get() = rethrow { FirebaseInstallations(getInstallations()) }

public actual fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = rethrow { FirebaseInstallations(getInstallations(app.js)) }

public val FirebaseInstallations.js: Installations get() = js

public actual class FirebaseInstallations internal constructor(internal val js: Installations) {

    public actual suspend fun delete(): Unit = rethrow { delete(js).awaitUnit() }

    public actual suspend fun getId(): String = rethrow { getId(js).awaitValue().toKotlinString() }

    public actual suspend fun getToken(forceRefresh: Boolean): String = rethrow { getToken(js, forceRefresh).awaitValue().toKotlinString() }
}

public actual open class FirebaseInstallationsException(code: String?, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.installations.rethrow { function() }

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: JsException) {
        throw FirebaseInstallationsException(e.errorCode(), e)
    }
}
