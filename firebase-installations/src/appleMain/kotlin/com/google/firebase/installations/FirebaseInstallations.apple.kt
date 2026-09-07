/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import cocoapods.FirebaseInstallations.FIRInstallations
import cocoapods.FirebaseInstallations.FIRInstallationsAuthTokenResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseInstallations internal constructor(public val ios: FIRInstallations) {

    public actual fun getId(): Task<String> = task { completion ->
        ios.installationIDWithCompletion { id, error -> completion(id, error) }
    }

    public actual fun getToken(forceRefresh: Boolean): Task<InstallationTokenResult> = task { completion ->
        ios.authTokenForcingRefresh(forceRefresh) { result, error -> completion(result?.let { IosInstallationTokenResult(it) }, error) }
    }

    public actual fun delete(): Task<Nothing?> = task { completion ->
        ios.deleteWithCompletion { error -> completion(null, error) }
    }

    public actual companion object {
        public actual fun getInstance(): FirebaseInstallations = FirebaseInstallations(FIRInstallations.installations())

        public actual fun getInstance(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(FIRInstallations.installationsWithApp(app.ios as objcnames.classes.FIRApp))
    }
}

public actual abstract class InstallationTokenResult actual constructor() {
    public actual abstract val token: String
    public actual abstract val tokenExpirationTimestamp: Long
}

private class IosInstallationTokenResult(val ios: FIRInstallationsAuthTokenResult) : InstallationTokenResult() {
    override val token: String get() = ios.authToken
    override val tokenExpirationTimestamp: Long get() = (ios.expirationDate.timeIntervalSince1970 * 1000).toLong()
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

private inline fun <T> task(crossinline start: ((T?, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error ->
        if (error == null) {
            source.setResult(result)
        } else {
            source.setException(error.toInstallationsException())
        }
    }
    return source.task
}

// FIRInstallationsErrorCode: Unknown = 0, Keychain = 1, ServerUnreachable = 2, InvalidConfiguration = 3
private fun NSError.toInstallationsException(): FirebaseInstallationsException = FirebaseInstallationsException(
    localizedDescription,
    when (code.toInt()) {
        2 -> FirebaseInstallationsException.Status.UNAVAILABLE
        3 -> FirebaseInstallationsException.Status.BAD_CONFIG
        else -> FirebaseInstallationsException.Status.UNAVAILABLE
    },
)
