/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.crashlytics

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.crashlytics.stub

/*
 * Header stubs for com.google.firebase:firebase-crashlytics (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime.
 */

public actual class FirebaseCrashlytics private constructor() {
    public actual fun checkForUnsentReports(): Task<Boolean> = stub()
    public actual fun deleteUnsentReports(): Unit = stub()
    public actual fun didCrashOnPreviousExecution(): Boolean = stub()
    public actual val isCrashlyticsCollectionEnabled: Boolean get() = stub()
    public actual fun log(message: String): Unit = stub()
    public actual fun recordException(throwable: Throwable): Unit = stub()
    public actual fun recordException(throwable: Throwable, keysAndValues: CustomKeysAndValues): Unit = stub()
    public actual fun sendUnsentReports(): Unit = stub()
    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean): Unit = stub()
    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean?): Unit = stub()
    public actual fun setCustomKey(key: String, value: Boolean): Unit = stub()
    public actual fun setCustomKey(key: String, value: Double): Unit = stub()
    public actual fun setCustomKey(key: String, value: Float): Unit = stub()
    public actual fun setCustomKey(key: String, value: Int): Unit = stub()
    public actual fun setCustomKey(key: String, value: Long): Unit = stub()
    public actual fun setCustomKey(key: String, value: String): Unit = stub()
    public actual fun setCustomKeys(keysAndValues: CustomKeysAndValues): Unit = stub()
    public actual fun setUserId(identifier: String): Unit = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseCrashlytics = stub()
    }
}

public actual class CustomKeysAndValues private constructor() {
    public actual class Builder actual constructor() {
        public actual fun putString(key: String, value: String): Builder = stub()
        public actual fun putBoolean(key: String, value: Boolean): Builder = stub()
        public actual fun putDouble(key: String, value: Double): Builder = stub()
        public actual fun putFloat(key: String, value: Float): Builder = stub()
        public actual fun putLong(key: String, value: Long): Builder = stub()
        public actual fun putInt(key: String, value: Int): Builder = stub()
        public actual fun build(): CustomKeysAndValues = stub()
    }
}

public actual class KeyValueBuilder internal actual constructor() {
    public actual fun key(key: String, value: Boolean): Unit = stub()
    public actual fun key(key: String, value: Double): Unit = stub()
    public actual fun key(key: String, value: Float): Unit = stub()
    public actual fun key(key: String, value: Int): Unit = stub()
    public actual fun key(key: String, value: Long): Unit = stub()
    public actual fun key(key: String, value: String): Unit = stub()

    // The SDK declares build() internal too; Kotlin mangles internal names with the module name, so use the SDK's.
    @JvmName("build\$com_google_firebase_firebase_crashlytics")
    internal actual fun build(): CustomKeysAndValues = stub()
}
