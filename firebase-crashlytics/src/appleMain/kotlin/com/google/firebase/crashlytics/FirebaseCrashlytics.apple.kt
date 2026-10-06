/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics
import cocoapods.FirebaseCrashlytics.FIRExceptionModel
import cocoapods.FirebaseCrashlytics.FIRStackFrame
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.experimental.ExperimentalNativeApi

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

    /**
     * Records the exception as a Crashlytics exception model: its class as the name, its message as the reason and its
     * own stack frames as code addresses, which Crashlytics symbolicates from the Kotlin framework's dSYM (see the README),
     * each cause following after a `Caused by:` frame. Recording an `NSError` instead would only capture the native stack
     * of this call.
     */
    public actual fun recordException(throwable: Throwable): Unit = ios.recordExceptionModel(throwable.asExceptionModel())

    /**
     * Only an `NSError` takes per-event keys in the iOS SDK, and it carries the native stack of this call rather than the
     * exception's own frames, so the exception is attached as `KotlinException` with its message as the description.
     */
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

/**
 * The exception's class, message and stack frames as a Crashlytics exception model. The frames are the exception's own
 * code addresses (not the native stack of the recording call), for Crashlytics to symbolicate from the framework's dSYM,
 * as the Kotlin/Native crash reporters (CrashKiOS, Sentry) submit them; the frames of each cause follow after a frame
 * naming it, as Crashlytics models a single stack.
 */
@OptIn(ExperimentalNativeApi::class)
private fun Throwable.asExceptionModel(): FIRExceptionModel {
    val frames = mutableListOf<FIRStackFrame>()
    val seen = mutableSetOf<Throwable>()
    var throwable: Throwable? = this
    while (throwable != null && seen.add(throwable)) {
        if (throwable !== this) frames += FIRStackFrame.stackFrameWithSymbol("Caused by: ${throwable.description()}", "", 0)
        throwable.getStackTraceAddresses().mapTo(frames) { FIRStackFrame.stackFrameWithAddress(it.toULong()) }
        throwable = throwable.cause
    }
    return FIRExceptionModel(name = this::class.qualifiedName ?: "KotlinException", reason = message ?: "").apply { stackTrace = frames }
}

private fun Throwable.description(): String = (this::class.qualifiedName ?: "KotlinException") + (message?.let { ": $it" } ?: "")

/** Wraps a Kotlin exception as an `NSError` carrying it as `KotlinException`, with the message as the description. */
private fun Throwable.asNSError(): NSError {
    val userInfo = mutableMapOf<Any?, Any>("KotlinException" to this)
    message?.let { userInfo[NSLocalizedDescriptionKey] = it }
    return NSError.errorWithDomain(this::class.qualifiedName, 0, userInfo)
}
