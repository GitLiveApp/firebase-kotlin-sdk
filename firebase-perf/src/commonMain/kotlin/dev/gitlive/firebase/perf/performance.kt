/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("PerformanceKt")
@file:JvmMultifileClass

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.perf.FirebasePerformance as CompatFirebasePerformance
import com.google.firebase.perf.performance as compatPerformance
import com.google.firebase.perf.performanceOf as compatPerformanceOf

// The Android-SDK-shaped singleton is reached through the `Firebase.performance` extension (PerformanceKt), a real
// static method on every platform, rather than the companion object of the header stub.

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
public val Firebase.performance: FirebasePerformance
    get() = FirebasePerformance(com.google.firebase.Firebase.compatPerformance)

/** Returns the [FirebasePerformance] instance of the given [FirebaseApp] (the Android SDK keeps one per app). */
public fun Firebase.performance(app: FirebaseApp): FirebasePerformance = FirebasePerformance(compatPerformanceOf(app.compat))

/**
 * Firebase Performance Monitoring.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.perf.FirebasePerformance] this wraps.
 */
public class FirebasePerformance internal constructor(public val compat: CompatFirebasePerformance) {
    /**
     * Creates a Trace object with given name.
     *
     * @param traceName name of the trace, requires no leading or trailing whitespace, no leading
     *     underscore '_' character.
     * @return the new Trace object.
     */
    public fun newTrace(traceName: String): Trace = Trace(compat.newTrace(traceName))

    /**
     * Determines whether performance monitoring is enabled or disabled. This respects the Firebase
     * Performance specific values first, and if these aren't set, uses the Firebase wide data
     * collection switch.
     *
     * @return true if performance monitoring is enabled and false if performance monitoring is
     *     disabled. This is for dynamic enable/disable state. This does not reflect whether
     *     instrumentation is enabled/disabled in Gradle properties.
     */
    public fun isPerformanceCollectionEnabled(): Boolean = compat.isPerformanceCollectionEnabled

    /**
     * Enables or disables performance monitoring. This setting is persisted and applied on future
     * invocations of your application. By default, performance monitoring is enabled. If you need to
     * change the default (for example, because you want to prompt the user before collecting
     * performance stats), add:
     *
     * `<meta-data android:name=firebase_performance_collection_enabled android:value=false />`
     *
     * to your application’s manifest. Changing the value during runtime will override the manifest
     * value.
     *
     * If you want to permanently disable sending performance metrics, add
     *
     * `<meta-data android:name="firebase_performance_collection_deactivated" android:value="true" />`
     *
     * to your application's manifest. Changing the value during runtime will not override the
     * manifest value.
     *
     * This is separate from enabling/disabling instrumentation in Gradle properties.
     *
     * @param enable Should performance monitoring be enabled
     */
    public fun setPerformanceCollectionEnabled(enable: Boolean) {
        compat.isPerformanceCollectionEnabled = enable
    }

    override fun equals(other: Any?): Boolean = other is FirebasePerformance && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebasePerformance($compat)"
}

/** An error reported by the underlying Performance Monitoring SDK (on JS, where the SDK throws). */
public open class FirebasePerformanceException : FirebaseException {
    public constructor(message: String) : super(message)
    public constructor(message: String, cause: Throwable) : super(message, cause)
}
