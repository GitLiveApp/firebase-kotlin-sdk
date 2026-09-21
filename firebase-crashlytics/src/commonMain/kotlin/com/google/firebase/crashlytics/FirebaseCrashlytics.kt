/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase

/**
 * The Firebase Crashlytics API, mirroring `com.google.firebase.crashlytics.FirebaseCrashlytics` from the Firebase
 * Android SDK: annotates and manages the fatal crashes, non-fatal errors and ANRs reported to Crashlytics.
 *
 * Crashlytics is initialized automatically on every platform; [getInstance] (or [Firebase.crashlytics]) returns the
 * singleton, which always belongs to the default [com.google.firebase.FirebaseApp].
 */
public expect class FirebaseCrashlytics {
    /**
     * Whether there are unsent reports on the device. Only meaningful when automatic data collection is disabled: with
     * it enabled the reports are sent as they are created and the task completes with `false`.
     */
    public fun checkForUnsentReports(): Task<Boolean>

    /** If automatic data collection is disabled, queues all the reports on the device for deletion; otherwise a no-op. */
    public fun deleteUnsentReports()

    /** Whether the app crashed during its previous run. */
    public fun didCrashOnPreviousExecution(): Boolean

    /** Whether automatic data collection is enabled. */
    public val isCrashlyticsCollectionEnabled: Boolean

    /**
     * Logs a message that is included in the next fatal, non-fatal or ANR report. Newlines are stripped and long
     * messages are truncated; the log is capped at 64 KB and rolls from the oldest message.
     */
    public fun log(message: String)

    /** Records a non-fatal report. */
    public fun recordException(throwable: Throwable)

    /**
     * Records a non-fatal report with [keysAndValues] attached to the event, in addition to the app level custom keys;
     * the event keys override app level keys of the same name.
     */
    public fun recordException(throwable: Throwable, keysAndValues: CustomKeysAndValues)

    /** If automatic data collection is disabled, queues all the reports on the device to be sent; otherwise a no-op. */
    public fun sendUnsentReports()

    /**
     * Enables or disables automatic data collection, overriding the manifest / plist settings and the Firebase-wide
     * setting. Disabling it keeps reports on the device until [sendUnsentReports] or [deleteUnsentReports] is called.
     */
    public fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    /**
     * Like [setCrashlyticsCollectionEnabled] with a `Boolean`, where `null` clears the override and falls back to the
     * manifest and Firebase-wide settings on Android. Apple platforms have no override to clear, so `null` leaves the
     * current setting unchanged there.
     */
    public fun setCrashlyticsCollectionEnabled(enabled: Boolean?)

    /**
     * Sets a custom key and value that are attached to subsequent reports; calling it again with the same key updates
     * the value. At most 64 keys are kept, and keys or values over 1024 characters are truncated.
     */
    public fun setCustomKey(key: String, value: Boolean)

    /** See [setCustomKey]. */
    public fun setCustomKey(key: String, value: Double)

    /** See [setCustomKey]. */
    public fun setCustomKey(key: String, value: Float)

    /** See [setCustomKey]. */
    public fun setCustomKey(key: String, value: Int)

    /** See [setCustomKey]. */
    public fun setCustomKey(key: String, value: Long)

    /** See [setCustomKey]. */
    public fun setCustomKey(key: String, value: String)

    /** Sets several custom keys at once; see [setCustomKey] for the limits. */
    public fun setCustomKeys(keysAndValues: CustomKeysAndValues)

    /** Records a user identifier that is attached to subsequent reports; identifiers over 1024 characters are truncated. */
    public fun setUserId(identifier: String)

    public companion object {
        /** The Crashlytics singleton of the default app. */
        public fun getInstance(): FirebaseCrashlytics
    }
}

// The Kotlin extensions of the Android SDK's firebase-crashlytics (FirebaseCrashlyticsKt), as plain common code: on
// Android and the JVM the facade is a header stub that is stripped, so the SDK's own facade binds.

/** The Crashlytics singleton of the default app; the Android SDK's `Firebase.crashlytics`. */
public val Firebase.crashlytics: FirebaseCrashlytics
    get() = FirebaseCrashlytics.getInstance()

/** Records a non-fatal report with the custom keys set in [init] attached to the event; see [KeyValueBuilder]. */
public fun FirebaseCrashlytics.recordException(throwable: Throwable, init: KeyValueBuilder.() -> Unit) {
    recordException(throwable, KeyValueBuilder().apply(init).build())
}

/** Sets the custom keys given in [init] at once; see [KeyValueBuilder] and [FirebaseCrashlytics.setCustomKeys]. */
public fun FirebaseCrashlytics.setCustomKeys(init: KeyValueBuilder.() -> Unit) {
    setCustomKeys(KeyValueBuilder().apply(init).build())
}
