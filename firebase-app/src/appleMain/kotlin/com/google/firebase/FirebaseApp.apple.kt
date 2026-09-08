/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseCore.FIROptions
import dev.gitlive.firebase.APPLICATION_CONTEXT_ANDROID_ONLY
import dev.gitlive.firebase.FROM_RESOURCE_ANDROID_ONLY
import dev.gitlive.firebase.GET_APPS_ANDROID_ONLY
import dev.gitlive.firebase.INITIALIZE_APP_ANDROID_ONLY

public actual open class FirebaseException : Exception {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, cause: Throwable) : super(message, cause)
}

public actual class FirebaseNetworkException actual constructor(message: String) : FirebaseException(message)

public actual open class FirebaseTooManyRequestsException actual constructor(message: String) : FirebaseException(message)

public actual open class FirebaseApiNotAvailableException actual constructor(message: String) : FirebaseException(message)

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseApp internal constructor(public val ios: FIRApp) {

    public actual val name: String get() = ios.name

    public actual val options: FirebaseOptions get() = FirebaseOptions(ios.options)

    public actual fun delete() {
        ios.deleteApp { }
    }

    /** No-op: automatic resource management is an Android-only behaviour. */
    public actual fun setAutomaticResourceManagementEnabled(enabled: Boolean) {}

    @Deprecated(APPLICATION_CONTEXT_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun getApplicationContext(): Any = throw UnsupportedOperationException(APPLICATION_CONTEXT_ANDROID_ONLY)

    override fun equals(other: Any?): Boolean = other is FirebaseApp && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseApp(name=$name)"

    public actual companion object {
        public actual val DEFAULT_APP_NAME: String = "__FIRAPP_DEFAULT"

        public actual fun getInstance(): FirebaseApp = FirebaseApp(FIRApp.defaultApp() ?: throw IllegalStateException("Default FirebaseApp is not initialized. Call FirebaseApp.initializeApp first."))

        public actual fun getInstance(name: String): FirebaseApp = FirebaseApp(FIRApp.appNamed(name) ?: throw IllegalStateException("FirebaseApp with name $name does not exist."))

        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public actual fun initializeApp(context: Any?): FirebaseApp? = Firebase.initialize(context)

        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context, options)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public actual fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp = Firebase.initialize(context, options)

        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context, options, name)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public actual fun initializeApp(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = Firebase.initialize(context, options, name)

        @Deprecated(GET_APPS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
        public actual fun getApps(context: Any?): List<FirebaseApp> = getApps()

        /** All initialized apps (the Android SDK's `getApps(Context)`, which needs no context here). */
        internal fun getApps(): List<FirebaseApp> = FIRApp.allApps().orEmpty().values.map { FirebaseApp(it as FIRApp) }
    }
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseOptions internal constructor(public val ios: FIROptions) {

    public actual val apiKey: String get() = ios.APIKey.orEmpty()
    public actual val applicationId: String get() = ios.googleAppID
    public actual val databaseUrl: String? get() = ios.databaseURL
    public actual val gcmSenderId: String? get() = ios.GCMSenderID
    public actual val projectId: String? get() = ios.projectID
    public actual val storageBucket: String? get() = ios.storageBucket
    public actual val gaTrackingId: String? get() = ios.trackingID

    override fun toString(): String = "FirebaseOptions(applicationId=$applicationId, projectId=$projectId)"

    public actual class Builder actual constructor() {
        private var apiKey: String? = null
        private var applicationId: String? = null
        private var databaseUrl: String? = null
        private var gcmSenderId: String? = null
        private var projectId: String? = null
        private var storageBucket: String? = null
        private var gaTrackingId: String? = null

        public actual constructor(options: FirebaseOptions) : this() {
            apiKey = options.apiKey
            applicationId = options.applicationId
            databaseUrl = options.databaseUrl
            gcmSenderId = options.gcmSenderId
            projectId = options.projectId
            storageBucket = options.storageBucket
            gaTrackingId = options.gaTrackingId
        }

        public actual fun setApiKey(apiKey: String): Builder = apply { this.apiKey = apiKey }
        public actual fun setApplicationId(applicationId: String): Builder = apply { this.applicationId = applicationId }
        public actual fun setDatabaseUrl(databaseUrl: String?): Builder = apply { this.databaseUrl = databaseUrl }
        public actual fun setGcmSenderId(gcmSenderId: String?): Builder = apply { this.gcmSenderId = gcmSenderId }
        public actual fun setProjectId(projectId: String?): Builder = apply { this.projectId = projectId }
        public actual fun setStorageBucket(storageBucket: String?): Builder = apply { this.storageBucket = storageBucket }
        public actual fun setGaTrackingId(gaTrackingId: String?): Builder = apply { this.gaTrackingId = gaTrackingId }

        public actual fun build(): FirebaseOptions = FirebaseOptions(
            FIROptions(requireNotNull(applicationId) { "ApplicationId must be set." }, gcmSenderId ?: "").apply {
                APIKey = this@Builder.apiKey
                databaseURL = this@Builder.databaseUrl
                projectID = this@Builder.projectId
                storageBucket = this@Builder.storageBucket
                trackingID = this@Builder.gaTrackingId
            },
        )
    }

    public actual companion object {
        @Deprecated(FROM_RESOURCE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
        public actual fun fromResource(context: Any?): FirebaseOptions? = throw UnsupportedOperationException(FROM_RESOURCE_ANDROID_ONLY)
    }
}
