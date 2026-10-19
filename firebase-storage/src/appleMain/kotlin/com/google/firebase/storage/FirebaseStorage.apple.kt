/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.getApps

private const val MILLIS_PER_SECOND = 1000.0

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseStorage internal constructor(public val ios: FIRStorage) {
    public actual val app: FirebaseApp
        get() = Firebase.getApps(null).firstOrNull { it.ios == ios.app() } ?: FirebaseApp.getInstance()

    public actual var maxOperationRetryTimeMillis: Long
        get() = (ios.maxOperationRetryTime() * MILLIS_PER_SECOND).toLong()
        set(value) {
            ios.setMaxOperationRetryTime(value / MILLIS_PER_SECOND)
        }

    public actual var maxUploadRetryTimeMillis: Long
        get() = (ios.maxUploadRetryTime() * MILLIS_PER_SECOND).toLong()
        set(value) {
            ios.setMaxUploadRetryTime(value / MILLIS_PER_SECOND)
        }

    /** The maximum time to retry downloads, in milliseconds. */
    internal var maxDownloadRetryTimeMillisValue: Long
        get() = (ios.maxDownloadRetryTime() * MILLIS_PER_SECOND).toLong()
        set(value) {
            ios.setMaxDownloadRetryTime(value / MILLIS_PER_SECOND)
        }

    public actual val reference: StorageReference get() = StorageReference(ios.reference())

    public actual fun getReference(location: String): StorageReference = StorageReference(ios.referenceWithPath(location))

    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference = StorageReference(ios.referenceForURL(fullUrl))

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    override fun equals(other: Any?): Boolean = other is FirebaseStorage && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseStorage($ios)"

    public actual companion object {
        public actual fun getInstance(): FirebaseStorage = FirebaseStorage(FIRStorage.storage())

        public actual fun getInstance(url: String): FirebaseStorage = FirebaseStorage(FIRStorage.storageWithURL(url))

        public actual fun getInstance(app: FirebaseApp): FirebaseStorage = FirebaseStorage(FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp))

        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseStorage = FirebaseStorage(FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp, url))
    }
}
