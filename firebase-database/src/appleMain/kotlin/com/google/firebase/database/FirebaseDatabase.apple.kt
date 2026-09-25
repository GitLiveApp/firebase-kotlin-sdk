/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import cocoapods.FirebaseDatabase.FIRDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.getApps

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseDatabase internal constructor(public val ios: FIRDatabase) {
    public actual val app: FirebaseApp
        get() = Firebase.getApps(null).firstOrNull { (it.ios as Any?) == ios.app } ?: FirebaseApp.getInstance()

    public actual val reference: DatabaseReference get() = DatabaseReference(ios.reference())

    public actual fun getReference(path: String): DatabaseReference = DatabaseReference(ios.referenceWithPath(path))

    public actual fun getReferenceFromUrl(url: String): DatabaseReference = DatabaseReference(ios.referenceFromURL(url))

    public actual fun goOffline() {
        ios.goOffline()
    }

    public actual fun goOnline() {
        ios.goOnline()
    }

    public actual fun purgeOutstandingWrites() {
        ios.purgeOutstandingWrites()
    }

    /** The iOS SDK only has a switch: [Logger.Level.DEBUG] enables its logging, every other level disables it. */
    public actual fun setLogLevel(logLevel: Logger.Level) {
        FIRDatabase.setLoggingEnabled(logLevel == Logger.Level.DEBUG)
    }

    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        ios.setPersistenceCacheSizeBytes(cacheSizeInBytes.toULong())
    }

    public actual fun setPersistenceEnabled(isEnabled: Boolean) {
        ios.setPersistenceEnabled(isEnabled)
    }

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    override fun equals(other: Any?): Boolean = other is FirebaseDatabase && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseDatabase($ios)"

    public actual companion object {
        public actual fun getInstance(): FirebaseDatabase = FirebaseDatabase(FIRDatabase.database())

        public actual fun getInstance(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase(FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp))

        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase(FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp, url))

        public actual fun getInstance(url: String): FirebaseDatabase = FirebaseDatabase(FIRDatabase.databaseWithURL(url))

        public actual fun getSdkVersion(): String = FIRDatabase.sdkVersion()
    }
}
