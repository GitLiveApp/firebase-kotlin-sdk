/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context
import dev.gitlive.firebase.stub

/*
 * Header stubs for the Firebase Android SDK core classes (see buildSrc utils/HeaderStubs.kt): they only satisfy the
 * `expect` declarations at compile time and are deleted from the compilation output, so the real classes from
 * com.google.firebase:firebase-common bind at runtime. Every member must exist on the real class with the same JVM
 * signature (verified by the build); members that only exist here (Context overloads) are for this module's own use.
 */

public actual open class FirebaseException : Exception {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, cause: Throwable) : super(message, cause)
}

public actual class FirebaseNetworkException actual constructor(message: String) : FirebaseException(message)

public actual open class FirebaseTooManyRequestsException actual constructor(message: String) : FirebaseException(message)

public actual open class FirebaseApiNotAvailableException actual constructor(message: String) : FirebaseException(message)

public actual class FirebaseApp private constructor() {
    public actual val name: String get() = stub()
    public actual val options: FirebaseOptions get() = stub()
    public actual fun delete(): Unit = stub()
    public actual fun setAutomaticResourceManagementEnabled(enabled: Boolean): Unit = stub()

    public actual companion object {
        @JvmField
        public actual val DEFAULT_APP_NAME: String = "[DEFAULT]"

        @JvmStatic
        public actual fun getInstance(): FirebaseApp = stub()

        @JvmStatic
        public actual fun getInstance(name: String): FirebaseApp = stub()

        @JvmStatic
        public fun initializeApp(context: Context): FirebaseApp? = stub()

        @JvmStatic
        public fun initializeApp(context: Context, options: FirebaseOptions): FirebaseApp = stub()

        @JvmStatic
        public fun initializeApp(context: Context, options: FirebaseOptions, name: String): FirebaseApp = stub()

        @JvmStatic
        public fun getApps(context: Context): List<FirebaseApp> = stub()
    }
}

public actual class FirebaseOptions private constructor() {
    public actual val apiKey: String get() = stub()
    public actual val applicationId: String get() = stub()
    public actual val databaseUrl: String? get() = stub()
    public actual val gcmSenderId: String? get() = stub()
    public actual val projectId: String? get() = stub()
    public actual val storageBucket: String? get() = stub()
    public actual val gaTrackingId: String? get() = stub()

    public actual class Builder actual constructor() {
        public actual constructor(options: FirebaseOptions) : this()
        public actual fun setApiKey(apiKey: String): Builder = stub()
        public actual fun setApplicationId(applicationId: String): Builder = stub()
        public actual fun setDatabaseUrl(databaseUrl: String?): Builder = stub()
        public actual fun setGcmSenderId(gcmSenderId: String?): Builder = stub()
        public actual fun setProjectId(projectId: String?): Builder = stub()
        public actual fun setStorageBucket(storageBucket: String?): Builder = stub()
        public actual fun setGaTrackingId(gaTrackingId: String?): Builder = stub()
        public actual fun build(): FirebaseOptions = stub()
    }
}
