/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import cocoapods.FirebaseFunctions.FIRFunctions
import cocoapods.FirebaseFunctions.FIRHTTPSCallable
import cocoapods.FirebaseFunctions.FIRHTTPSCallableOptions
import cocoapods.FirebaseFunctions.FIRHTTPSCallableResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.functions.SET_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.WITH_TIMEOUT_JVM_ONLY
import platform.Foundation.NSError
import platform.Foundation.NSURL

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseFunctions internal constructor(public val ios: FIRFunctions) {

    public actual fun getHttpsCallable(name: String): HttpsCallableReference = HttpsCallableReference { ios.HTTPSCallableWithName(name) }

    public actual fun getHttpsCallable(name: String, options: HttpsCallableOptions): HttpsCallableReference = HttpsCallableReference { ios.HTTPSCallableWithName(name, options.toIos()) }

    internal fun httpsCallableFromUrl(url: NSURL, options: HttpsCallableOptions?): HttpsCallableReference = HttpsCallableReference {
        if (options == null) ios.HTTPSCallableWithURL(url) else ios.HTTPSCallableWithURL(url, options.toIos())
    }

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    override fun equals(other: Any?): Boolean = other is FirebaseFunctions && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseFunctions($ios)"

    public actual companion object {
        public actual fun getInstance(): FirebaseFunctions = FirebaseFunctions(FIRFunctions.functions())

        public actual fun getInstance(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions(FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp))

        public actual fun getInstance(app: FirebaseApp, regionOrCustomDomain: String): FirebaseFunctions = FirebaseFunctions(
            if (regionOrCustomDomain.isCustomDomain) {
                FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp, customDomain = regionOrCustomDomain)
            } else {
                FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp, region = regionOrCustomDomain)
            },
        )

        public actual fun getInstance(regionOrCustomDomain: String): FirebaseFunctions = FirebaseFunctions(
            if (regionOrCustomDomain.isCustomDomain) FIRFunctions.functionsForCustomDomain(regionOrCustomDomain) else FIRFunctions.functionsForRegion(regionOrCustomDomain),
        )

        /** As on Android, a "region or custom domain" that starts with a scheme is a custom domain. */
        private val String.isCustomDomain: Boolean get() = startsWith("http://") || startsWith("https://")
    }
}

/**
 * @property ios The underlying Firebase iOS SDK object; [withTimeout] creates a new one, so it is kept per reference.
 */
public actual class HttpsCallableReference internal constructor(private val create: () -> FIRHTTPSCallable) {
    public val ios: FIRHTTPSCallable = create()

    public actual fun call(): Task<HttpsCallableResult> = task { completion -> ios.callWithCompletion { result, error -> completion(result, error) } }

    public actual fun call(data: Any?): Task<HttpsCallableResult> = task { completion -> ios.callWithObject(data) { result, error -> completion(result, error) } }

    public actual val timeout: Long get() = (ios.timeoutInterval() * MILLIS_PER_SECOND).toLong()

    @Deprecated(SET_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setTimeout(timeout: Long, units: Any): Unit = throw UnsupportedOperationException(SET_TIMEOUT_JVM_ONLY)

    @Deprecated(WITH_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun withTimeout(timeout: Long, units: Any): HttpsCallableReference = throw UnsupportedOperationException(WITH_TIMEOUT_JVM_ONLY)

    internal fun setTimeoutSeconds(seconds: Double) {
        ios.setTimeoutInterval(seconds)
    }

    internal fun withTimeoutSeconds(seconds: Double): HttpsCallableReference = HttpsCallableReference(create).apply { ios.setTimeoutInterval(seconds) }

    override fun toString(): String = "HttpsCallableReference($ios)"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class HttpsCallableResult internal constructor(public val ios: FIRHTTPSCallableResult) {
    public actual val data: Any? get() = ios.data()

    override fun equals(other: Any?): Boolean = other is HttpsCallableResult && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "HttpsCallableResult($data)"
}

private const val MILLIS_PER_SECOND = 1000.0
private const val FUNCTIONS_ERROR_DOMAIN = "com.firebase.functions"
private const val FUNCTIONS_ERROR_DETAILS_KEY = "details"

internal fun HttpsCallableOptions.toIos(): FIRHTTPSCallableOptions = FIRHTTPSCallableOptions(requireLimitedUseAppCheckTokens = limitedUseAppCheckTokens)

private inline fun task(crossinline start: ((FIRHTTPSCallableResult?, NSError?) -> Unit) -> Unit): Task<HttpsCallableResult> {
    val source = TaskCompletionSource<HttpsCallableResult>()
    start { result, error ->
        if (error == null) {
            source.setResult(HttpsCallableResult(requireNotNull(result) { "The iOS SDK completed a call with neither a result nor an error" }))
        } else {
            source.setException(error.toFunctionsException())
        }
    }
    return source.task
}

/** The iOS SDK reports the canonical error code as the `NSError` code (the same values as [FirebaseFunctionsException.Code]). */
internal fun NSError.toFunctionsException(): FirebaseFunctionsException = FirebaseFunctionsException(
    localizedDescription,
    if (domain == FUNCTIONS_ERROR_DOMAIN) FirebaseFunctionsException.Code.fromValue(code.toInt()) else FirebaseFunctionsException.Code.UNKNOWN,
    userInfo[FUNCTIONS_ERROR_DETAILS_KEY],
)
