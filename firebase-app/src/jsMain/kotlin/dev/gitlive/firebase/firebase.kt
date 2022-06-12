/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.app.getApp
import dev.gitlive.firebase.externals.app.getApps
import dev.gitlive.firebase.externals.app.initializeApp
import kotlin.js.json
import dev.gitlive.firebase.externals.app.FirebaseApp as JsFirebaseApp

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(getApp())

actual fun Firebase.app(name: String): FirebaseApp =
    FirebaseApp(getApp(name))

actual fun Firebase.initialize(context: Any?): FirebaseApp? =
    throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp =
    FirebaseApp(initializeApp(options.toJson(), name))

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions) =
    FirebaseApp(initializeApp(options.toJson()))

actual class FirebaseApp internal constructor(val js: JsFirebaseApp) {
    actual val name: String
        get() = js.name
    actual val options: FirebaseOptions
        get() = js.options.run {
            FirebaseOptions(appId, apiKey, databaseURL, gaTrackingId, storageBucket, projectId, messagingSenderId, authDomain)
        }
}

actual fun Firebase.apps(context: Any?) = getApps().map { FirebaseApp(it) }

private fun FirebaseOptions.toJson() = json(
    "apiKey" to apiKey,
    "appId" to applicationId,
    "databaseURL" to (databaseUrl ?: undefined),
    "storageBucket" to (storageBucket ?: undefined),
    "projectId" to (projectId ?: undefined),
    "gaTrackingId" to (gaTrackingId ?: undefined),
    "messagingSenderId" to (gcmSenderId ?: undefined),
    "authDomain" to (authDomain ?: undefined)
)

actual open class FirebaseException(code: String?, cause: Throwable) : Exception("$code: ${cause.message}", cause)
actual open class FirebaseNetworkException(code: String?, cause: Throwable) : FirebaseException(code, cause)
actual open class FirebaseTooManyRequestsException(code: String?, cause: Throwable) : FirebaseException(code, cause)
actual open class FirebaseApiNotAvailableException(code: String?, cause: Throwable) : FirebaseException(code, cause)
