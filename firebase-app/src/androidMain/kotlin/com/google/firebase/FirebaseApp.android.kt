/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context
import dev.gitlive.firebase.android.FirebaseApp as AndroidFirebaseApp
import dev.gitlive.firebase.android.FirebaseOptions as AndroidFirebaseOptions

public actual typealias FirebaseException = dev.gitlive.firebase.android.FirebaseException

public actual typealias FirebaseNetworkException = dev.gitlive.firebase.android.FirebaseNetworkException

public actual typealias FirebaseTooManyRequestsException = dev.gitlive.firebase.android.FirebaseTooManyRequestsException

public actual typealias FirebaseApiNotAvailableException = dev.gitlive.firebase.android.FirebaseApiNotAvailableException

/** @property android The underlying (relocated) Firebase Android SDK object. */
public actual class FirebaseApp internal constructor(public val android: AndroidFirebaseApp) {

    public actual val name: String get() = android.name

    public actual val options: FirebaseOptions get() = FirebaseOptions(android.options)

    public actual fun delete(): Unit = android.delete()

    override fun equals(other: Any?): Boolean = other is FirebaseApp && other.android == android

    override fun hashCode(): Int = android.hashCode()

    override fun toString(): String = android.toString()

    public actual companion object {
        @JvmField
        public actual val DEFAULT_APP_NAME: String = AndroidFirebaseApp.DEFAULT_APP_NAME

        @JvmStatic
        public actual fun getInstance(): FirebaseApp = FirebaseApp(AndroidFirebaseApp.getInstance())

        @JvmStatic
        public actual fun getInstance(name: String): FirebaseApp = FirebaseApp(AndroidFirebaseApp.getInstance(name))

        @JvmStatic
        public actual fun initializeApp(context: Any?): FirebaseApp? = AndroidFirebaseApp.initializeApp(context as Context)?.let { FirebaseApp(it) }

        @JvmStatic
        public actual fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(AndroidFirebaseApp.initializeApp(context as Context, options.android))

        @JvmStatic
        public actual fun initializeApp(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(AndroidFirebaseApp.initializeApp(context as Context, options.android, name))

        @JvmStatic
        public actual fun getApps(context: Any?): List<FirebaseApp> = AndroidFirebaseApp.getApps(context as Context).map { FirebaseApp(it) }
    }
}

/** @property android The underlying (relocated) Firebase Android SDK object. */
public actual class FirebaseOptions internal constructor(public val android: AndroidFirebaseOptions) {

    public actual val apiKey: String get() = android.apiKey
    public actual val applicationId: String get() = android.applicationId
    public actual val databaseUrl: String? get() = android.databaseUrl
    public actual val gcmSenderId: String? get() = android.gcmSenderId
    public actual val projectId: String? get() = android.projectId
    public actual val storageBucket: String? get() = android.storageBucket
    public actual val gaTrackingId: String? get() = android.gaTrackingId

    override fun equals(other: Any?): Boolean = other is FirebaseOptions && other.android == android

    override fun hashCode(): Int = android.hashCode()

    override fun toString(): String = android.toString()

    public actual class Builder private constructor(private val android: AndroidFirebaseOptions.Builder) {
        public actual constructor() : this(AndroidFirebaseOptions.Builder())
        public actual constructor(options: FirebaseOptions) : this(AndroidFirebaseOptions.Builder(options.android))
        public actual fun setApiKey(apiKey: String): Builder = apply { android.setApiKey(apiKey) }
        public actual fun setApplicationId(applicationId: String): Builder = apply { android.setApplicationId(applicationId) }
        public actual fun setDatabaseUrl(databaseUrl: String?): Builder = apply { android.setDatabaseUrl(databaseUrl) }
        public actual fun setGcmSenderId(gcmSenderId: String?): Builder = apply { android.setGcmSenderId(gcmSenderId) }
        public actual fun setProjectId(projectId: String?): Builder = apply { android.setProjectId(projectId) }
        public actual fun setStorageBucket(storageBucket: String?): Builder = apply { android.setStorageBucket(storageBucket) }
        public actual fun setGaTrackingId(gaTrackingId: String?): Builder = apply { android.setGaTrackingId(gaTrackingId) }
        public actual fun build(): FirebaseOptions = FirebaseOptions(android.build())
    }
}
