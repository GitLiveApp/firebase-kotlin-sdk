/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.functions.SET_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.WITH_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.stub

/*
 * Header stubs for com.google.firebase:firebase-functions (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime. The SDK members
 * with JVM-only parameter types (java.net.URL, TimeUnit) are not declared here: the shipped facades reach them through
 * FunctionsJvmApi.java, which compiles after the stubs are stripped.
 */

public actual class FirebaseFunctions private constructor() {
    public actual fun getHttpsCallable(name: String): HttpsCallableReference = stub()
    public actual fun getHttpsCallable(name: String, options: HttpsCallableOptions): HttpsCallableReference = stub()
    public actual fun useEmulator(host: String, port: Int): Unit = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseFunctions = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseFunctions = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp, regionOrCustomDomain: String): FirebaseFunctions = stub()

        @JvmStatic
        public actual fun getInstance(regionOrCustomDomain: String): FirebaseFunctions = stub()
    }
}

public actual class HttpsCallableReference private constructor() {
    public actual fun call(): Task<HttpsCallableResult> = stub()
    public actual fun call(data: Any?): Task<HttpsCallableResult> = stub()
    public actual val timeout: Long get() = stub()

    @Deprecated(SET_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setTimeout(timeout: Long, units: Any): Unit = stub()

    @Deprecated(WITH_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun withTimeout(timeout: Long, units: Any): HttpsCallableReference = stub()
}

public actual class HttpsCallableResult private constructor() {
    public actual val data: Any? get() = stub()
}
