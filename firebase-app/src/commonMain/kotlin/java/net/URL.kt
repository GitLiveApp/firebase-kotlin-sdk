/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.net

/**
 * Stands in for `java.net.URL` as the carrier of a URL string, so that the Firebase Android SDK members taking one
 * (`FirebaseFunctions.getHttpsCallableFromUrl`, `FirebasePerformance.newHttpMetric`) keep their signatures in common
 * code and Android code creating one inline, `URL("https://…")`, compiles unchanged.
 *
 * On Android and the JVM the class is a header stub stripped from the output (see buildSrc utils/HeaderStubs.kt), so
 * code there uses the JDK's. Elsewhere this class is shipped: it holds the string as given, with none of the JDK's
 * parsing or validation, and the platform SDK receives [toString]. Only the members the SDK needs are declared.
 */
public class URL(private val spec: String) {
    /** The URL string as given. */
    public fun toExternalForm(): String = spec

    override fun toString(): String = spec

    override fun equals(other: Any?): Boolean = other is URL && other.spec == spec

    override fun hashCode(): Int = spec.hashCode()
}
