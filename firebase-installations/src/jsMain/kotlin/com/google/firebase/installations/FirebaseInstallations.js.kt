/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.installations.internal.FidListener
import com.google.firebase.installations.internal.FidListenerHandle
import dev.gitlive.firebase.installations.externals.Installations
import kotlin.js.Promise
import dev.gitlive.firebase.installations.externals.delete as jsDelete
import dev.gitlive.firebase.installations.externals.getId as jsGetId
import dev.gitlive.firebase.installations.externals.getInstallations as jsGetInstallations
import dev.gitlive.firebase.installations.externals.getToken as jsGetToken
import dev.gitlive.firebase.installations.externals.onIdChange as jsOnIdChange

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseInstallations internal constructor(public val js: Installations) {

    public actual fun getId(): Task<String> = task { jsGetId(js) }

    public actual fun getToken(forceRefresh: Boolean): Task<InstallationTokenResult> = task { jsGetToken(js, forceRefresh).then { JsInstallationTokenResult(it) } }

    public actual fun delete(): Task<Nothing?> = task { jsDelete(js).then { null } }

    public actual fun registerFidListener(listener: FidListener): FidListenerHandle {
        val unsubscribe = rethrow { jsOnIdChange(js) { listener.onFidChanged(it) } }
        return object : FidListenerHandle {
            override fun unregister() {
                unsubscribe()
            }
        }
    }

    public actual companion object {
        public actual fun getInstance(): FirebaseInstallations = rethrow { FirebaseInstallations(jsGetInstallations()) }

        public actual fun getInstance(app: FirebaseApp): FirebaseInstallations = rethrow { FirebaseInstallations(jsGetInstallations(app.js)) }
    }
}

public actual abstract class InstallationTokenResult actual constructor() {
    public actual abstract val token: String
    public actual abstract val tokenExpirationTimestamp: Long
}

/** The JS SDK only exposes the token string, so [tokenExpirationTimestamp] is always `0`. */
private class JsInstallationTokenResult(override val token: String) : InstallationTokenResult() {
    override val tokenExpirationTimestamp: Long get() = 0
}

public actual class FirebaseInstallationsException : FirebaseException {
    public actual val status: Status

    public actual constructor(status: Status) : super(status.name) {
        this.status = status
    }

    public actual constructor(message: String, status: Status) : super(message) {
        this.status = status
    }

    public actual constructor(message: String, status: Status, cause: Throwable) : super(message, cause) {
        this.status = status
    }

    public actual enum class Status {
        BAD_CONFIG,
        UNAVAILABLE,
        TOO_MANY_REQUESTS,
    }
}

private inline fun <T> task(start: () -> Promise<T>): Task<T> {
    val source = TaskCompletionSource<T>()
    try {
        start().then({ source.setResult(it) }, { source.setException(it.toInstallationsException()) })
    } catch (e: Throwable) {
        source.setException(e.toInstallationsException())
    }
    return source.task
}

private inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toInstallationsException()
}

private fun Throwable.toInstallationsException(): FirebaseInstallationsException {
    if (this is FirebaseInstallationsException) return this
    val code = asDynamic().code.unsafeCast<String?>()
    val status = when (code) {
        "installations/request-failed", "installations/not-registered" -> FirebaseInstallationsException.Status.UNAVAILABLE
        "installations/missing-app-config-values" -> FirebaseInstallationsException.Status.BAD_CONFIG
        else -> FirebaseInstallationsException.Status.UNAVAILABLE
    }
    return FirebaseInstallationsException("$code: $message", status, this)
}
