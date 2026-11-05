package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseCrashlytics] instance of the default [FirebaseApp]. */
public expect val Firebase.crashlytics: FirebaseCrashlytics

/** Returns the [FirebaseCrashlytics] instance of a given [FirebaseApp]. */
public expect fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics

/**
 * The Firebase Crashlytics API provides methods to annotate and manage fatal crashes, non-fatal
 * errors, and ANRs captured and reported to Firebase Crashlytics.
 *
 * By default, Firebase Crashlytics is automatically initialized.
 *
 * Call [Firebase.crashlytics] to get the singleton instance of
 * [FirebaseCrashlytics].
 */
public expect class FirebaseCrashlytics {
    /**
     * Records a non-fatal report to send to Crashlytics.
     *
     * @param exception a [Throwable] to be recorded as a non-fatal event.
     */
    public fun recordException(exception: Throwable)

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
    public fun recordException(exception: Throwable, customKeys: Map<String, Any>)

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
    public fun log(message: String)

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
    public fun setUserId(userId: String)

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
    public fun setCustomKey(key: String, value: String)

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
    public fun setCustomKey(key: String, value: Boolean)

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
    public fun setCustomKey(key: String, value: Double)

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
    public fun setCustomKey(key: String, value: Float)

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
    public fun setCustomKey(key: String, value: Int)

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
    public fun setCustomKey(key: String, value: Long)

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
    public fun setCustomKeys(customKeys: Map<String, Any>)

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
    public fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    /**
     * Checks whether the app crashed on its previous run.
     *
     * @return true if a crash was recorded during the previous run of the app.
     */
    public fun didCrashOnPreviousExecution(): Boolean

    /**
     * If automatic data collection is disabled, this method queues up all the reports on a device to
     * send to Crashlytics. Otherwise, this method is a no-op.
     */
    public fun sendUnsentReports()

    /**
     * If automatic data collection is disabled, this method queues up all the reports on a device for
     * deletion. Otherwise, this method is a no-op.
     */
    public fun deleteUnsentReports()
}

/**
 * Exception that gets thrown when an operation on Firebase Crashlytics fails.
 */
public expect open class FirebaseCrashlyticsException : FirebaseException
