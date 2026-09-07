/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import kotlin.js.json
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp
import dev.gitlive.firebase.externals.FirebaseOptions as JsFirebaseOptions
import dev.gitlive.firebase.externals.deleteApp as jsDeleteApp
import dev.gitlive.firebase.externals.getApp as jsGetApp
import dev.gitlive.firebase.externals.getApps as jsGetApps
import dev.gitlive.firebase.externals.initializeApp as jsInitializeApp

public actual open class FirebaseException : Exception {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, cause: Throwable) : super("$message: ${cause.message}", cause)
}

public actual class FirebaseNetworkException : FirebaseException {
    public actual constructor(message: String) : super(message)
    public constructor(code: String?, cause: Throwable) : super(code.toString(), cause)
}

public actual open class FirebaseTooManyRequestsException : FirebaseException {
    public actual constructor(message: String) : super(message)
    public constructor(code: String?, cause: Throwable) : super(code.toString(), cause)
}

public actual open class FirebaseApiNotAvailableException : FirebaseException {
    public actual constructor(message: String) : super(message)
    public constructor(code: String?, cause: Throwable) : super(code.toString(), cause)
}

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseApp internal constructor(public val js: JsFirebaseApp) {

    public actual val name: String get() = js.name

    public actual val options: FirebaseOptions get() = FirebaseOptions(js.options)

    public actual fun delete() {
        jsDeleteApp(js)
    }

    override fun equals(other: Any?): Boolean = other is FirebaseApp && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseApp(name=$name)"

    public actual companion object {
        public actual val DEFAULT_APP_NAME: String = "[DEFAULT]"

        public actual fun getInstance(): FirebaseApp = FirebaseApp(jsGetApp())

        public actual fun getInstance(name: String): FirebaseApp = FirebaseApp(jsGetApp(name))

        /** Platform extra: initialises like the Android SDK's `initializeApp(Context)`; the context is ignored. */
        public fun initializeApp(context: Any?): FirebaseApp? = throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

        public fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson()))

        public fun initializeApp(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(jsInitializeApp(options.toJson(), name))

        /** Platform extra: like the Android SDK's `getApps(Context)`; the context is ignored. */
        public fun getApps(context: Any?): List<FirebaseApp> = jsGetApps().map { FirebaseApp(it) }
    }
}

/** @property authDomain The auth domain (JS only). */
public actual class FirebaseOptions internal constructor(
    public actual val apiKey: String,
    public actual val applicationId: String,
    public actual val databaseUrl: String?,
    public actual val gcmSenderId: String?,
    public actual val projectId: String?,
    public actual val storageBucket: String?,
    public actual val gaTrackingId: String?,
    public val authDomain: String?,
) {
    internal constructor(js: JsFirebaseOptions) : this(
        apiKey = js.apiKey,
        applicationId = js.appId,
        databaseUrl = js.databaseURL,
        gcmSenderId = js.messagingSenderId,
        projectId = js.projectId,
        storageBucket = js.storageBucket,
        gaTrackingId = js.gaTrackingId,
        authDomain = js.authDomain,
    )

    internal fun toJson(): Any = json(
        "apiKey" to apiKey,
        "appId" to applicationId,
        "databaseURL" to (databaseUrl ?: undefined),
        "storageBucket" to (storageBucket ?: undefined),
        "projectId" to (projectId ?: undefined),
        "gaTrackingId" to (gaTrackingId ?: undefined),
        "messagingSenderId" to (gcmSenderId ?: undefined),
        "authDomain" to (authDomain ?: undefined),
    )

    override fun toString(): String = "FirebaseOptions(applicationId=$applicationId, projectId=$projectId)"

    public actual class Builder actual constructor() {
        private var apiKey: String? = null
        private var applicationId: String? = null
        private var databaseUrl: String? = null
        private var gcmSenderId: String? = null
        private var projectId: String? = null
        private var storageBucket: String? = null
        private var gaTrackingId: String? = null
        private var authDomain: String? = null

        public actual constructor(options: FirebaseOptions) : this() {
            apiKey = options.apiKey
            applicationId = options.applicationId
            databaseUrl = options.databaseUrl
            gcmSenderId = options.gcmSenderId
            projectId = options.projectId
            storageBucket = options.storageBucket
            gaTrackingId = options.gaTrackingId
            authDomain = options.authDomain
        }

        public actual fun setApiKey(apiKey: String): Builder = apply { this.apiKey = apiKey }
        public actual fun setApplicationId(applicationId: String): Builder = apply { this.applicationId = applicationId }
        public actual fun setDatabaseUrl(databaseUrl: String?): Builder = apply { this.databaseUrl = databaseUrl }
        public actual fun setGcmSenderId(gcmSenderId: String?): Builder = apply { this.gcmSenderId = gcmSenderId }
        public actual fun setProjectId(projectId: String?): Builder = apply { this.projectId = projectId }
        public actual fun setStorageBucket(storageBucket: String?): Builder = apply { this.storageBucket = storageBucket }
        public actual fun setGaTrackingId(gaTrackingId: String?): Builder = apply { this.gaTrackingId = gaTrackingId }

        /** Sets the auth domain (JS only). */
        public fun setAuthDomain(authDomain: String?): Builder = apply { this.authDomain = authDomain }

        public actual fun build(): FirebaseOptions = FirebaseOptions(
            apiKey = requireNotNull(apiKey) { "ApiKey must be set." },
            applicationId = requireNotNull(applicationId) { "ApplicationId must be set." },
            databaseUrl = databaseUrl,
            gcmSenderId = gcmSenderId,
            projectId = projectId,
            storageBucket = storageBucket,
            gaTrackingId = gaTrackingId,
            authDomain = authDomain,
        )
    }
}
