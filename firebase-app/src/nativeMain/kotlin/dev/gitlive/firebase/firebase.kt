/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import cocoapods.FirebaseCore.*

actual open class FirebaseException(message: String) : Exception(message)
actual open class FirebaseNetworkException(message: String) : FirebaseException(message)
actual open class FirebaseTooManyRequestsException(message: String) : FirebaseException(message)
actual open class FirebaseApiNotAvailableException(message: String) : FirebaseException(message)

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(FIRApp.defaultApp()!!)

actual fun Firebase.app(name: String): FirebaseApp =
    FirebaseApp(FIRApp.appNamed(name)!!)

actual fun Firebase.initialize(context: Any?): FirebaseApp? =
    FIRApp.configure().let { app }

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp =
    FIRApp.configureWithName(name, options.toNative()).let { app(name) }

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions) =
    FIRApp.configureWithOptions(options.toNative()).let { app }

actual class FirebaseApp internal constructor(val native: FIRApp) {
    actual val name: String
        get() = native.name
    actual val options: FirebaseOptions
        get() = native.options.run { FirebaseOptions(bundleID, APIKey!!, databaseURL!!, trackingID, storageBucket, projectID) }
}

actual fun Firebase.apps(context: Any?) = FIRApp.allApps()
    .orEmpty()
    .values
    .map { FirebaseApp(it as FIRApp) }

private fun FirebaseOptions.toNative() = FIROptions(this@toNative.applicationId, this@toNative.gcmSenderId ?: "").apply {
        APIKey = this@toNative.apiKey
        databaseURL = this@toNative.databaseUrl
        trackingID = this@toNative.gaTrackingId
        storageBucket = this@toNative.storageBucket
        projectID = this@toNative.projectId
    }
