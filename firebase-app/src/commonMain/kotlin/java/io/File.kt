/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

/**
 * Stands in for `java.io.File` as the carrier of a file path, so that the Firebase Android SDK members taking one
 * (`StorageReference.getFile(File)`) keep their signatures in common code and Android code creating one, `File(path)`,
 * compiles unchanged.
 *
 * On Android and the JVM the class is a header stub stripped from the output (see buildSrc utils/HeaderStubs.kt), so
 * code there uses the JDK's. Elsewhere this class is shipped: it holds the path as given, with none of the JDK's
 * file-system access, and the platform SDK receives [path]. Only the members the SDK needs are declared.
 */
public class File(pathname: String) {
    /** The file [child] of the directory at [parent]. */
    public constructor(parent: String, child: String) : this(if (parent.isEmpty()) child else parent.trimEnd('/') + "/" + child)

    /** The file [child] of the directory [parent]. */
    public constructor(parent: File, child: String) : this(parent.path, child)

    /** The path as given. */
    public val path: String = pathname

    /** The last segment of [path]. */
    public val name: String get() = path.substringAfterLast('/')

    override fun toString(): String = path

    override fun equals(other: Any?): Boolean = other is File && other.path == path

    override fun hashCode(): Int = path.hashCode()
}
