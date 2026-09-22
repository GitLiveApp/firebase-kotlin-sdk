/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseCrashlytics internal constructor(public val ios: FIRCrashlytics) {

    /**
     * The iOS SDK only invokes the completion when automatic data collection is disabled (with it enabled, reports are
     * sent at startup and there is nothing to check), so the task completes with false then, as on Android.
     */
    public actual fun checkForUnsentReports(): Task<Boolean> {
        val source = TaskCompletionSource<Boolean>()
        if (ios.isCrashlyticsCollectionEnabled()) {
            source.setResult(false)
        } else {
            ios.checkForUnsentReportsWithCompletion { hasUnsentReports -> source.setResult(hasUnsentReports) }
        }
        return source.task
    }

    public actual fun deleteUnsentReports(): Unit = ios.deleteUnsentReports()

    public actual fun didCrashOnPreviousExecution(): Boolean = ios.didCrashDuringPreviousExecution()

    public actual val isCrashlyticsCollectionEnabled: Boolean get() = ios.isCrashlyticsCollectionEnabled()

    public actual fun log(message: String): Unit = ios.log(message)

    public actual fun recordException(throwable: Throwable): Unit = ios.recordError(throwable.asNSError())

    public actual fun recordException(throwable: Throwable, keysAndValues: CustomKeysAndValues): Unit = ios.recordError(throwable.asNSError(), keysAndValues.asNSDictionary())

    public actual fun sendUnsentReports(): Unit = ios.sendUnsentReports()

    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean): Unit = ios.setCrashlyticsCollectionEnabled(enabled)

    /** `null` leaves the setting unchanged: the iOS SDK has no override to clear. */
    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean?) {
        if (enabled != null) ios.setCrashlyticsCollectionEnabled(enabled)
    }

    public actual fun setCustomKey(key: String, value: Boolean): Unit = ios.setCustomValue(value, key)
    public actual fun setCustomKey(key: String, value: Double): Unit = ios.setCustomValue(value, key)
    public actual fun setCustomKey(key: String, value: Float): Unit = ios.setCustomValue(value, key)
    public actual fun setCustomKey(key: String, value: Int): Unit = ios.setCustomValue(value, key)
    public actual fun setCustomKey(key: String, value: Long): Unit = ios.setCustomValue(value, key)
    public actual fun setCustomKey(key: String, value: String): Unit = ios.setCustomValue(value, key)

    public actual fun setCustomKeys(keysAndValues: CustomKeysAndValues): Unit = ios.setCustomKeysAndValues(keysAndValues.asNSDictionary())

    public actual fun setUserId(identifier: String): Unit = ios.setUserID(identifier)

    override fun equals(other: Any?): Boolean = other is FirebaseCrashlytics && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseCrashlytics"

    public actual companion object {
        public actual fun getInstance(): FirebaseCrashlytics = FirebaseCrashlytics(FIRCrashlytics.crashlytics())
    }
}

/** The keys and their values as Kotlin types, which the iOS SDK receives as `NSString` and `NSNumber` values. */
public actual class CustomKeysAndValues internal constructor(internal val keysAndValues: Map<String, Any>) {

    override fun toString(): String = "CustomKeysAndValues($keysAndValues)"

    public actual class Builder actual constructor() {
        private val keysAndValues = mutableMapOf<String, Any>()

        public actual fun putString(key: String, value: String): Builder = apply { keysAndValues[key] = value }
        public actual fun putBoolean(key: String, value: Boolean): Builder = apply { keysAndValues[key] = value }
        public actual fun putDouble(key: String, value: Double): Builder = apply { keysAndValues[key] = value }
        public actual fun putFloat(key: String, value: Float): Builder = apply { keysAndValues[key] = value }
        public actual fun putLong(key: String, value: Long): Builder = apply { keysAndValues[key] = value }
        public actual fun putInt(key: String, value: Int): Builder = apply { keysAndValues[key] = value }
        public actual fun build(): CustomKeysAndValues = CustomKeysAndValues(keysAndValues.toMap())
    }
}

public actual class KeyValueBuilder internal actual constructor() {
    private val builder = CustomKeysAndValues.Builder()

    public actual fun key(key: String, value: Boolean) {
        builder.putBoolean(key, value)
    }
    public actual fun key(key: String, value: Double) {
        builder.putDouble(key, value)
    }
    public actual fun key(key: String, value: Float) {
        builder.putFloat(key, value)
    }
    public actual fun key(key: String, value: Int) {
        builder.putInt(key, value)
    }
    public actual fun key(key: String, value: Long) {
        builder.putLong(key, value)
    }
    public actual fun key(key: String, value: String) {
        builder.putString(key, value)
    }

    internal actual fun build(): CustomKeysAndValues = builder.build()
}

@Suppress("UNCHECKED_CAST")
private fun CustomKeysAndValues.asNSDictionary(): Map<Any?, *> = keysAndValues as Map<Any?, *>

/** Wraps a Kotlin exception as an `NSError` carrying it as `KotlinException`, with the message as the description. */
private fun Throwable.asNSError(): NSError {
    val userInfo = mutableMapOf<Any?, Any>("KotlinException" to this)
    message?.let { userInfo[NSLocalizedDescriptionKey] = it }
    return NSError.errorWithDomain(this::class.qualifiedName, 0, userInfo)
}
