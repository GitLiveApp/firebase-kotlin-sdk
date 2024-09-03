package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

/** Returns the [FirebasePerformance] instance of the default [FirebaseApp]. */
public expect val Firebase.performance: FirebasePerformance

/** Returns the [FirebasePerformance] instance of a given [FirebaseApp]. */
public expect fun Firebase.performance(app: FirebaseApp): FirebasePerformance

/**
 * The Firebase Performance Monitoring API.
 *
 * It is automatically initialized by FirebaseApp.
 *
 * This SDK uses FirebaseInstallations to identify the app instance and periodically sends data
 * to the Firebase backend. To stop sending performance events, call [setPerformanceCollectionEnabled] with value [false].
 */
public expect class FirebasePerformance {
    /**
     * Creates a Trace object with given name.
     *
     * @param traceName name of the trace, requires no leading or trailing whitespace, no leading
     *     underscore '_' character.
     * @return the new Trace object.
     */
    public fun newTrace(traceName: String): Trace

    /**
     * Determines whether performance monitoring is enabled or disabled. This respects the Firebase
     * Performance specific values first, and if these aren't set, uses the Firebase wide data
     * collection switch.
     *
     * @return true if performance monitoring is enabled and false if performance monitoring is
     *     disabled. This is for dynamic enable/disable state. This does not reflect whether
     *     instrumentation is enabled/disabled in Gradle properties.
     */
    public fun isPerformanceCollectionEnabled(): Boolean

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
    public fun setPerformanceCollectionEnabled(enable: Boolean)
}

/**
 * Exception that gets thrown when an operation on Firebase Performance fails.
 */
public expect open class FirebasePerformanceException : FirebaseException
