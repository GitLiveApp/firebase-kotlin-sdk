/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.net

import kotlin.jvm.JvmStatic

/**
 * Stands in for `android.net.Uri` as the carrier of a URI string, so that the Firebase Android SDK members taking or
 * returning one (`StorageReference.putFile`, `RemoteMessage.Notification.getImageUrl`) keep their signatures in common
 * code and Android code creating one, `Uri.parse(path)`, compiles unchanged.
 *
 * On Android and the JVM, where firebase-java-sdk ships one, the class is a header stub stripped from the output (see
 * buildSrc utils/HeaderStubs.kt), so code there uses the platform's. Elsewhere this class is shipped: [parse] holds the
 * string as given, with none of Android's parsing, and the platform SDK receives [toString]. Only the members the SDK
 * needs are declared.
 */
public abstract class Uri private constructor() {
    abstract override fun toString(): String

    private class StringUri(private val uri: String) : Uri() {
        override fun toString(): String = uri

        override fun equals(other: Any?): Boolean = other is Uri && other.toString() == uri

        override fun hashCode(): Int = uri.hashCode()
    }

    public companion object {
        /** A [Uri] holding [uriString] as given. */
        @JvmStatic
        public fun parse(uriString: String): Uri = StringUri(uriString)
    }
}
