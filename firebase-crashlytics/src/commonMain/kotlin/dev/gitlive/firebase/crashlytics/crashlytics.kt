/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("CrashlyticsKt")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.crashlytics

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

import com.google.firebase.crashlytics.CustomKeysAndValues
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import com.google.firebase.Firebase as CompatFirebase
import com.google.firebase.crashlytics.FirebaseCrashlytics as CompatFirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics as compatCrashlytics

// The Android-SDK-shaped entry point is reached through the `Firebase.crashlytics` extension (FirebaseCrashlyticsKt),
// which is a real static method on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseCrashlytics] instance of the default [FirebaseApp]. */
/** Message of the deprecated `dev.gitlive` members that only delegate to the `com.google.firebase` layer. */
internal const val DELEGATES_TO_ANDROID_SDK_API =
    "Only delegates to the com.google.firebase layer, which common code can use directly; see the ReplaceWith"

@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.crashlytics", "com.google.firebase.crashlytics.crashlytics"))
public val Firebase.crashlytics: FirebaseCrashlytics
    get() = FirebaseCrashlytics(CompatFirebase.compatCrashlytics)

/**
 * Returns the [FirebaseCrashlytics] instance of a given [FirebaseApp]. Crashlytics only reports for the default app on
 * every platform, so this is the same instance as [Firebase.crashlytics].
 */
@Suppress("UNUSED_PARAMETER")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.crashlytics", "com.google.firebase.crashlytics.crashlytics"))
public fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = crashlytics

/**
 * The Firebase Crashlytics API provides methods to annotate and manage fatal crashes, non-fatal
 * errors, and ANRs captured and reported to Firebase Crashlytics.
 *
 * By default, Firebase Crashlytics is automatically initialized.
 *
 * Call [Firebase.crashlytics] to get the singleton instance of
 * [FirebaseCrashlytics].
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.crashlytics.FirebaseCrashlytics] this wraps.
 */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.crashlytics.FirebaseCrashlytics"))
public class FirebaseCrashlytics internal constructor(public val compat: CompatFirebaseCrashlytics) {

    /**
     * Records a non-fatal report to send to Crashlytics.
     *
     * @param exception a [Throwable] to be recorded as a non-fatal event.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.recordException(exception)"))
    public fun recordException(exception: Throwable) {
        compat.recordException(exception)
    }

    /**
     * Records a non-fatal report to send to Crashlytics.
     *
     * Combined with app level custom keys, the event is restricted to a maximum of 64 key/value
     * pairs. New keys beyond that limit are ignored. Keys or values that exceed 1024 characters are
     * truncated.
     *
     * The values of event keys override the values of app level custom keys if they're identical.
     *
     * @param exception a [Throwable] to be recorded as a non-fatal event.
     * @param customKeys A dictionary of keys and the values to associate with the non fatal
     *                      exception, in addition to the app level custom keys.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.recordException(exception, customKeys)", "dev.gitlive.firebase.crashlytics.recordException"))
    public fun recordException(exception: Throwable, customKeys: Map<String, Any>) {
        compat.recordException(exception, customKeys.toCustomKeysAndValues())
    }

    /**
     * Logs a message that's included in the next fatal, non-fatal, or ANR report.
     *
     * Logs are visible in the session view on the Firebase Crashlytics console.
     *
     * Newline characters are stripped and extremely long messages are truncated. The maximum log
     * size is 64k. If exceeded, the log rolls such that messages are removed, starting from the
     * oldest.
     *
     * @param message the message to be logged
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.log(message)"))
    public fun log(message: String) {
        compat.log(message)
    }

    /**
     * Records a user ID (identifier) that's associated with subsequent fatal, non-fatal, and ANR
     * reports.
     *
     * The user ID is visible in the session view on the Firebase Crashlytics console.
     *
     * Identifiers longer than 1024 characters will be truncated.
     *
     * @param userId a unique identifier for the current user
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setUserId(userId)"))
    public fun setUserId(userId: String) {
        compat.setUserId(userId)
    }

    /**
     * Sets a custom key and value that are associated with subsequent fatal, non-fatal, and ANR
     * reports.
     *
     * Multiple calls to this method with the same key update the value for that key.
     *
     * The value of any key at the time of a fatal, non-fatal, or ANR event is associated with that
     * event.
     *
     * Keys and associated values are visible in the session view on the Firebase Crashlytics
     * console.
     *
     * Accepts a maximum of 64 key/value pairs. New keys beyond that limit are ignored. Keys or
     * values that exceed 1024 characters are truncated.
     *
     * @param key A unique key
     * @param value A value to be associated with the given key
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: String) {
        compat.setCustomKey(key, value)
    }

    /** See [setCustomKey]. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: Boolean) {
        compat.setCustomKey(key, value)
    }

    /** See [setCustomKey]. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: Double) {
        compat.setCustomKey(key, value)
    }

    /** See [setCustomKey]. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: Float) {
        compat.setCustomKey(key, value)
    }

    /** See [setCustomKey]. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: Int) {
        compat.setCustomKey(key, value)
    }

    /** See [setCustomKey]. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKey(key, value)"))
    public fun setCustomKey(key: String, value: Long) {
        compat.setCustomKey(key, value)
    }

    /**
     * Sets multiple custom keys and values that are associated with subsequent fatal, non-fatal, and
     * ANR reports. This method is intended as an alternative to [setCustomKey] in order to
     * reduce the computational load of writing out multiple key/value pairs at the same time.
     *
     * Multiple calls to this method with the same key update the value for that key.
     *
     * The value of any key at the time of a fatal, non-fatal, or ANR event is associated with that
     * event.
     *
     * Keys and associated values are visible in the session view on the Firebase Crashlytics
     * console.
     *
     * Accepts a maximum of 64 key/value pairs. If calling this method results in the number of
     * custom keys exceeding this limit, only some of the keys will be logged (however many are needed
     * to get to 64). Which keys are logged versus dropped is unpredictable as there is no intrinsic
     * sorting of keys. Keys or values that exceed 1024 characters are truncated.
     *
     * @param customKeys A dictionary of keys and the values to associate with each key
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCustomKeys(customKeys)", "dev.gitlive.firebase.crashlytics.setCustomKeys"))
    public fun setCustomKeys(customKeys: Map<String, Any>) {
        compat.setCustomKeys(customKeys.toCustomKeysAndValues())
    }

    /**
     * Enables or disables the automatic data collection configuration for Crashlytics.
     *
     * If this is set, it overrides any automatic data collection settings configured in the
     * AndroidManifest.xml as well as any Firebase-wide settings.
     *
     * If automatic data collection is disabled for Crashlytics, crash reports are stored on the
     * device. Use [sendUnsentReports] to upload existing reports even when automatic data collection is
     * disabled. Use [deleteUnsentReports] to delete any reports stored on the device without
     * sending them to Crashlytics.
     *
     * @param enabled whether to enable automatic data collection. When set to `false`, the new
     *     value does not apply until the next run of the app. To disable data collection by default
     *     for all app runs, add the `firebase_crashlytics_collection_enabled` flag to your
     *     app's AndroidManifest.xml.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.setCrashlyticsCollectionEnabled(enabled)"))
    public fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        compat.setCrashlyticsCollectionEnabled(enabled)
    }

    /**
     * Checks whether the app crashed on its previous run.
     *
     * @return true if a crash was recorded during the previous run of the app.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.didCrashOnPreviousExecution()"))
    public fun didCrashOnPreviousExecution(): Boolean = compat.didCrashOnPreviousExecution()

    /**
     * If automatic data collection is disabled, this method queues up all the reports on a device to
     * send to Crashlytics. Otherwise, this method is a no-op.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.sendUnsentReports()"))
    public fun sendUnsentReports() {
        compat.sendUnsentReports()
    }

    /**
     * If automatic data collection is disabled, this method queues up all the reports on a device for
     * deletion. Otherwise, this method is a no-op.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.deleteUnsentReports()"))
    public fun deleteUnsentReports() {
        compat.deleteUnsentReports()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseCrashlytics && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseCrashlytics"
}

/**
 * Exception that gets thrown when an operation on Firebase Crashlytics fails.
 */
public open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)

/** Records a non-fatal report with [customKeys] attached to the event; see [CompatFirebaseCrashlytics.recordException]. */
public fun CompatFirebaseCrashlytics.recordException(throwable: Throwable, customKeys: Map<String, Any>) {
    recordException(throwable, customKeys.toCustomKeysAndValues())
}

/**
 * Sets [customKeys] (`String`, `Boolean`, `Double`, `Float`, `Int` or `Long` values) that are attached to subsequent
 * reports; see [CompatFirebaseCrashlytics.setCustomKeys].
 */
public fun CompatFirebaseCrashlytics.setCustomKeys(customKeys: Map<String, Any>) {
    setCustomKeys(customKeys.toCustomKeysAndValues())
}

/** Values of other types are ignored, as before the compatibility layer. */
private fun Map<String, Any>.toCustomKeysAndValues(): CustomKeysAndValues = CustomKeysAndValues.Builder().apply {
    forEach { (key, value) ->
        when (value) {
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
        }
    }
}.build()
