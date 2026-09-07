/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.android.installations.FirebaseInstallations as AndroidFirebaseInstallations
import dev.gitlive.firebase.android.installations.FirebaseInstallationsException as AndroidFirebaseInstallationsException
import dev.gitlive.firebase.android.installations.InstallationTokenResult as AndroidInstallationTokenResult

/** @property android The underlying (relocated) Firebase Android SDK object. */
public actual class FirebaseInstallations internal constructor(public val android: AndroidFirebaseInstallations) {

    public actual fun getId(): Task<String> = android.id.mapResult { it }

    public actual fun getToken(forceRefresh: Boolean): Task<InstallationTokenResult> = android.getToken(forceRefresh).mapResult { InstallationTokenResultWrapper(it) }

    public actual fun delete(): Task<Nothing?> = android.delete().mapResult { null }

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseInstallations = FirebaseInstallations(AndroidFirebaseInstallations.getInstance())

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(AndroidFirebaseInstallations.getInstance(app.android))
    }
}

public actual abstract class InstallationTokenResult actual constructor() {
    public actual abstract val token: String
    public actual abstract val tokenExpirationTimestamp: Long
}

private class InstallationTokenResultWrapper(val android: AndroidInstallationTokenResult) : InstallationTokenResult() {
    override val token: String get() = android.token
    override val tokenExpirationTimestamp: Long get() = android.tokenExpirationTimestamp
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

/** Maps the result of a relocated SDK task and converts its exception to the `com.google.firebase` type. */
private fun <T, R> Task<T>.mapResult(transform: (T) -> R): Task<R> = continueWithTask { task ->
    val exception = task.exception
    when {
        exception != null -> Tasks.forException(exception.toCompat())
        task.isCanceled -> Tasks.forCanceled()
        else -> Tasks.forResult(transform(task.result))
    }
}

private fun Exception.toCompat(): Exception = when (this) {
    is AndroidFirebaseInstallationsException -> FirebaseInstallationsException(
        message ?: status.name,
        when (status) {
            AndroidFirebaseInstallationsException.Status.BAD_CONFIG -> FirebaseInstallationsException.Status.BAD_CONFIG
            AndroidFirebaseInstallationsException.Status.UNAVAILABLE -> FirebaseInstallationsException.Status.UNAVAILABLE
            AndroidFirebaseInstallationsException.Status.TOO_MANY_REQUESTS -> FirebaseInstallationsException.Status.TOO_MANY_REQUESTS
            else -> FirebaseInstallationsException.Status.UNAVAILABLE
        },
        this,
    )
    else -> this
}
